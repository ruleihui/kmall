package net.kaaass.kmall.controller;

import net.kaaass.kmall.controller.request.OrderCreateRequest;
import net.kaaass.kmall.controller.response.OrderRequestResponse;
import net.kaaass.kmall.dto.OrderDto;
import net.kaaass.kmall.exception.ForbiddenException;
import net.kaaass.kmall.exception.InternalErrorExeption;
import net.kaaass.kmall.exception.NotFoundException;
import net.kaaass.kmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@PreAuthorize("hasRole('USER')")
public class OrderController extends BaseController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/request/{requestId}/")
    boolean checkRequest(@PathVariable String requestId) {
        return orderService.checkRequest(requestId);
    }

    @GetMapping("/{id}/")
    OrderDto getById(@PathVariable String id) throws NotFoundException, ForbiddenException {
        return orderService.getById(id, getUid());
    }

    @GetMapping("/")
    List<OrderDto> getAllByUid(Pageable pageable) {
        return orderService.getAllByUid(getUid(), pageable);
    }

    @PostMapping("/")
    OrderRequestResponse createToQueue(@RequestBody OrderCreateRequest request) throws NotFoundException, InternalErrorExeption {
        // TODO 参数检查
        return orderService.createToQueue(getUid(), request);
    }
}