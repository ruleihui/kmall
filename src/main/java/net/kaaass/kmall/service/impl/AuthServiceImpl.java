package net.kaaass.kmall.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.kaaass.kmall.KmallApplication;
import net.kaaass.kmall.controller.response.LoginResponse;
import net.kaaass.kmall.dao.entity.UserAuthEntity;
import net.kaaass.kmall.dao.entity.UserInfoEntity;
import net.kaaass.kmall.dao.repository.UserAuthRepository;
import net.kaaass.kmall.dao.repository.UserInfoRepository;
import net.kaaass.kmall.dao.repository.UserMetadataRepository;
import net.kaaass.kmall.dto.AuthTokenDto;
import net.kaaass.kmall.dto.UserAuthDto;
import net.kaaass.kmall.event.UserRegisterEvent;
import net.kaaass.kmall.exception.NotFoundException;
import net.kaaass.kmall.security.JwtTokenUtil;
import net.kaaass.kmall.service.AuthService;
import net.kaaass.kmall.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Qualifier("jwtUserDetailsServiceImpl")
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserAuthRepository authRepository;

    @Autowired
    private UserInfoRepository infoRepository;

    @Autowired
    private UserMetadataRepository userMetadataRepository;

    @Override
    public Optional<UserAuthDto> register(UserAuthDto userToAdd) {
        // 登录信息
        UserAuthEntity authEntity = new UserAuthEntity();
        authEntity.setPhone(userToAdd.getPhone());
        authEntity.setPassword(jwtTokenUtil.encryptPassword(userToAdd.getPassword()));
        authEntity.setRoles(Constants.ROLE_USER);
        try {
            authEntity = authRepository.save(authEntity);
        } catch (Exception e) {
            return Optional.empty();
        }
        // 用户信息
        UserInfoEntity infoEntity = new UserInfoEntity();
        infoEntity.setAuth(authEntity);
        infoRepository.save(infoEntity);
        // 拼接结果
        UserAuthDto result = new UserAuthDto();
        result.setId(authEntity.getId());
        result.setPhone(authEntity.getPhone());
        // 触发事件
        KmallApplication.EVENT_BUS.post(new UserRegisterEvent(result));
        return Optional.of(result);
    }

    @Override
    public Optional<LoginResponse> login(String phone, String password) {
        try {
            var uid = authRepository.findByPhone(phone)
                    .map(UserAuthEntity::getId)
                    .orElseThrow();
            var upToken = new UsernamePasswordAuthenticationToken(uid, password);
            // 登录验证
            Authentication auth = authenticationManager.authenticate(upToken);
            SecurityContextHolder.getContext().setAuthentication(auth);

            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            // 更新时间
            authRepository.findById(uid)
                    .ifPresent(userAuthEntity -> {
                        userAuthEntity.setLastLoginTime(Timestamp.valueOf(LocalDateTime.now()));
                        authRepository.save(userAuthEntity);
                    });

            // 拼接凭据
            return Optional.of(userDetails)
                    .map(jwtUser -> {
                        var adminRole = new SimpleGrantedAuthority("ROLE_ADMIN");
                        var authTokenDto = jwtTokenUtil.generateToken(jwtUser);

                        return new LoginResponse(authTokenDto, phone, jwtUser.getAuthorities().contains(adminRole));
                    });
        } catch (AuthenticationException e) {
            log.info("登录失败", e);
            return Optional.empty();
        } catch (NoSuchElementException e) {
            log.info("账户 {} 不存在", phone);
            return Optional.empty();
        }
    }

    @Override
    public Optional<AuthTokenDto> refresh(String oldToken) {
        return Optional.of(oldToken)
                .filter(this::validateTokenViaDatabase)
                .flatMap(jwtTokenUtil::refreshToken);
    }

    @Override
    public boolean validate(UserAuthDto user) {
        // TODO 短信验证
        return false;
    }

    @Override
    public void remove(String id) throws NotFoundException {
        var entity = authRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("未找到该用户！"));
        infoRepository.deleteAllByAuth(entity);
        authRepository.delete(entity);
        // Metadata 不是外键约束
        userMetadataRepository.deleteAllByUid(id);
    }

    private boolean validateTokenViaDatabase(String oldToken) {
        var username = jwtTokenUtil.getUsernameFromToken(oldToken); // 顺便校验了格式
        var jwtUser = username.map(userDetailsService::loadUserByUsername);
        return jwtUser.filter(userDetails -> jwtTokenUtil.validateToken(oldToken, userDetails))
                .isPresent();
    }
}
