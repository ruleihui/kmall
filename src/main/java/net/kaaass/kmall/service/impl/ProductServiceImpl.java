package net.kaaass.kmall.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import net.kaaass.kmall.controller.request.ProductAddRequest;
import net.kaaass.kmall.dao.entity.ProductEntity;
import net.kaaass.kmall.dao.entity.ProductStorageEntity;
import net.kaaass.kmall.dao.repository.CategoryRepository;
import net.kaaass.kmall.dao.repository.ProductRepository;
import net.kaaass.kmall.dto.ProductDto;
import net.kaaass.kmall.exception.NotFoundException;
import net.kaaass.kmall.mapper.ProductMapper;
import net.kaaass.kmall.service.CategoryService;
import net.kaaass.kmall.service.ProductService;
import net.kaaass.kmall.service.PromoteService;
import net.kaaass.kmall.service.UserService;
import net.kaaass.kmall.service.metadata.MetadataManager;
import net.kaaass.kmall.service.metadata.ResourceManager;
import net.kaaass.kmall.util.Constants;
import net.kaaass.kmall.vo.ProductExtraVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ResourceManager resourceManager;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private MetadataManager metadataManager;

    @Autowired
    private PromoteService promoteService;

    @Autowired
    private UserService userService;

    /**
     * 增加商品
     *
     * @param productToAdd
     * @return
     */
    @Override
    public Optional<ProductDto> addProduct(ProductAddRequest productToAdd) {
        var entity = new ProductEntity();
        entity.setName(productToAdd.getName());
        entity.setPrice(productToAdd.getPrice());
        entity.setMailPrice(productToAdd.getMailPrice());
        entity.setBuyLimit(productToAdd.getBuyLimit());
        var storage = new ProductStorageEntity();
        storage.setRest(productToAdd.getRest());
        entity.setStorage(storage);
        try {
            var thumbnail = resourceManager.getEntity(productToAdd.getThumbnailId()).orElseThrow();
            entity.setThumbnail(thumbnail);
            var category = categoryRepository.findById(productToAdd.getCategoryId()).orElseThrow();
            entity.setCategory(category);
            return Optional.of(productRepository.save(entity))
                    .map(ProductMapper.INSTANCE::productEntityToDto);
        } catch (Exception e) {
            log.info("插入时发生错误", e);
            return Optional.empty();
        }
    }

    /**
     * 从id获取商品
     *
     * @param id
     * @return
     */
    @Override
    public ProductDto getById(String id) throws NotFoundException {
        return ProductMapper.INSTANCE.productEntityToDto(getEntityById(id));
    }

    @Override
    public ProductEntity getEntityById(String id) throws NotFoundException {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("未找到此商品！"));
    }

    @Override
    public ProductExtraVo getExtraById(String id, int count, String uid) throws NotFoundException {
        var extra = new ProductExtraVo();
        extra.setDetail(metadataManager.getForProduct(id, Constants.KEY_DETAIL));
        var defaultAddress = userService.getDefaultAddressEntityById(uid).getId();
        extra.setPromotes(promoteService.getForSingleProduct(id, count, uid, defaultAddress));
        return extra;
    }

    /**
     * 获取全部商品
     *
     * @param pageable
     * @return
     */
    @Override
    public List<ProductDto> getAll(Pageable pageable) {
        return productRepository.findAll(pageable)
                .stream()
                .map(ProductMapper.INSTANCE::productEntityToDto)
                .collect(Collectors.toList());
    }

    /**
     * 通过分类获得商品
     * @param categoryId
     * @param pageable
     * @return
     */
    @Override
    public List<ProductDto> getAllByCategory(String categoryId, Pageable pageable) throws NotFoundException {
        var root = categoryService.getEntityById(categoryId);
        var categories = categoryService.getAllSubs(root);
        log.debug("子分类：{}", categories);
        return productRepository.findAllByCategoryIn(categories, pageable)
                    .stream()
                    .map(ProductMapper.INSTANCE::productEntityToDto)
                    .collect(Collectors.toList());
    }
}