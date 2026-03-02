package com.test.mapper;

import com.test.dto.OrderDto;

import java.util.List;

public interface OrderMapper {

    void insertOrder(OrderDto order);

    void insertOrderItem(Long orderId, Long productId, int quantity, int price);

    List<OrderDto> findByMemberId(Long memberId);

    OrderDto findById(Long id);

    List<OrderDto.OrderItemResultDto> findItemsByOrderId(Long orderId);
}
