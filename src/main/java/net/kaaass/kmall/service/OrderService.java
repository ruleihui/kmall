package net.kaaass.kmall.service;

import net.kaaass.kmall.controller.request.OrderCreateRequest;
import net.kaaass.kmall.controller.response.OrderRequestResponse;
import net.kaaass.kmall.dao.entity.OrderEntity;
import net.kaaass.kmall.dto.OrderDto;
import net.kaaass.kmall.exception.ForbiddenException;
import net.kaaass.kmall.exception.InternalErrorExeption;
import net.kaaass.kmall.exception.NotFoundException;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    OrderEntity getEntityById(String id) throws NotFoundException;

    OrderEntity getEntityByIdAndCheck(String id, String uid) throws NotFoundException, ForbiddenException;

    boolean checkRequest(String requestId);

    OrderDto getById(String id, String uid) throws NotFoundException, ForbiddenException;

    List<OrderDto> getAllByUid(String uid, Pageable pageable);

    OrderRequestResponse createToQueue(String uid, OrderCreateRequest request) throws InternalErrorExeption, NotFoundException;

    void doCreate(OrderRequestContext context) throws NotFoundException;
}