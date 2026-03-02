package com.test.service;

import com.test.config.exception.BusinessException;
import com.test.config.exception.NotFoundException;
import com.test.dto.OrderDto;
import com.test.dto.PointHistoryDto;
import com.test.dto.ProductDto;
import com.test.mapper.MemberMapper;
import com.test.mapper.OrderMapper;
import com.test.mapper.PointMapper;
import com.test.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final MemberMapper memberMapper;
    private final PointMapper pointMapper;

    public OrderService(OrderMapper orderMapper,
                        ProductMapper productMapper,
                        MemberMapper memberMapper,
                        PointMapper pointMapper) {
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
        this.memberMapper = memberMapper;
        this.pointMapper = pointMapper;
    }

    /**
     * 주문 생성
     * 1. 재고 확인
     * 2. 포인트 사용 검증
     * 3. orders INSERT
     * 4. order_item INSERT + 재고 차감
     * 5. 포인트 사용 기록 + 잔액 차감
     * 6. 포인트 적립 (결제금액 1%)
     */
    @Transactional
    public Long createOrder(Long memberId, List<OrderDto.OrderItemDto> items, int pointUsed) {
        // 포인트 사용량 검증 (보유 포인트는 DB에서 delta 방식으로 관리하므로 음수가 되지 않도록 SQL에서 보호)
        if (pointUsed < 0) {
            throw new BusinessException("INVALID_POINT", "포인트 사용 금액은 0 이상이어야 합니다.");
        }

        // 상품별 재고 확인 및 총액 계산
        int totalAmount = 0;
        for (OrderDto.OrderItemDto item : items) {
            ProductDto product = productMapper.findById(item.getProductId());
            if (product == null) {
                throw new NotFoundException("존재하지 않는 상품입니다. id=" + item.getProductId());
            }
            if (product.getStock() < item.getQuantity()) {
                throw new BusinessException("OUT_OF_STOCK",
                        "재고가 부족합니다. 상품: " + product.getName()
                        + " (재고: " + product.getStock() + ", 요청: " + item.getQuantity() + ")");
            }
            totalAmount += product.getPrice() * item.getQuantity();
        }

        // 결제 금액 = 총액 - 사용 포인트
        int paymentAmount = totalAmount - pointUsed;
        if (paymentAmount < 0) {
            throw new BusinessException("INVALID_POINT", "포인트 사용 금액이 주문 금액을 초과합니다.");
        }

        // orders INSERT
        OrderDto order = new OrderDto();
        order.setMemberId(memberId);
        order.setTotalAmount(totalAmount);
        order.setPointUsed(pointUsed);
        order.setPaymentAmount(paymentAmount);
        orderMapper.insertOrder(order);
        Long orderId = order.getId();

        // order_item INSERT + 재고 차감
        for (OrderDto.OrderItemDto item : items) {
            ProductDto product = productMapper.findById(item.getProductId());
            orderMapper.insertOrderItem(orderId, item.getProductId(), item.getQuantity(), product.getPrice());
            productMapper.decreaseStock(item.getProductId(), item.getQuantity());
        }

        // 포인트 사용 기록
        if (pointUsed > 0) {
            PointHistoryDto useHistory = new PointHistoryDto();
            useHistory.setMemberId(memberId);
            useHistory.setOrderId(orderId);
            useHistory.setAmount(-pointUsed);
            useHistory.setType("USE");
            pointMapper.insertHistory(useHistory);
        }

        // 포인트 적립 (결제금액의 1%, 소수점 버림)
        int earnPoint = (int) (paymentAmount * 0.01);
        if (earnPoint > 0) {
            PointHistoryDto earnHistory = new PointHistoryDto();
            earnHistory.setMemberId(memberId);
            earnHistory.setOrderId(orderId);
            earnHistory.setAmount(earnPoint);
            earnHistory.setType("EARN");
            pointMapper.insertHistory(earnHistory);
        }

        // member.point_balance 업데이트 (현재 잔액 - 사용 + 적립)
        // MemberMapper에서 현재 잔액을 먼저 읽어야 하므로 Delta 방식으로 처리
        // updatePointBalance(memberId, currentBalance + delta)
        // 여기서는 member 조회 없이 상대 업데이트를 SQL로 처리하도록 mapper 확장
        int pointDelta = earnPoint - pointUsed;
        if (pointDelta != 0) {
            memberMapper.addPointBalance(memberId, pointDelta);
        }

        return orderId;
    }

    public List<OrderDto> getOrderList(Long memberId) {
        return orderMapper.findByMemberId(memberId);
    }

    public OrderDto getOrder(Long orderId, Long memberId) {
        OrderDto order = orderMapper.findById(orderId);
        if (order == null) {
            throw new NotFoundException("존재하지 않는 주문입니다.");
        }
        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException("FORBIDDEN", "접근 권한이 없는 주문입니다.");
        }
        order.setOrderItems(orderMapper.findItemsByOrderId(orderId));
        return order;
    }
}
