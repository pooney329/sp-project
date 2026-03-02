package com.test.dto;

import java.util.Date;
import java.util.List;

public class OrderDto {

    /* -------- orders 테이블 -------- */
    private Long id;
    private Long memberId;
    private int totalAmount;
    private int pointUsed;
    private int paymentAmount;
    private String status;
    private Date orderedAt;

    /* -------- 주문 상품 목록 -------- */
    private List<OrderItemDto> items;

    /* -------- 주문 요청용 내부 DTO -------- */
    public static class OrderItemDto {
        private Long productId;
        private int quantity;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    /* -------- order_item 조회 결과용 DTO -------- */
    public static class OrderItemResultDto {
        private Long id;
        private Long orderId;
        private Long productId;
        private String productName;
        private int quantity;
        private int price;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public int getPrice() { return price; }
        public void setPrice(int price) { this.price = price; }
    }

    private List<OrderItemResultDto> orderItems;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public int getTotalAmount() { return totalAmount; }
    public void setTotalAmount(int totalAmount) { this.totalAmount = totalAmount; }

    public int getPointUsed() { return pointUsed; }
    public void setPointUsed(int pointUsed) { this.pointUsed = pointUsed; }

    public int getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(int paymentAmount) { this.paymentAmount = paymentAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getOrderedAt() { return orderedAt; }
    public void setOrderedAt(Date orderedAt) { this.orderedAt = orderedAt; }

    public List<OrderItemDto> getItems() { return items; }
    public void setItems(List<OrderItemDto> items) { this.items = items; }

    public List<OrderItemResultDto> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemResultDto> orderItems) { this.orderItems = orderItems; }
}
