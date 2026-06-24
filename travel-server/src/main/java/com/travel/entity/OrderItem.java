package com.travel.entity;

import java.math.BigDecimal;
import java.util.Date;

public class OrderItem {
    private Integer itemId;
    private String orderId;
    private Integer productType;
    private Integer productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private Date useDate;
    private Integer refundStatus;

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public Integer getProductType() { return productType; }
    public void setProductType(Integer productType) { this.productType = productType; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Date getUseDate() { return useDate; }
    public void setUseDate(Date useDate) { this.useDate = useDate; }
    public Integer getRefundStatus() { return refundStatus; }
    public void setRefundStatus(Integer refundStatus) { this.refundStatus = refundStatus; }
}
