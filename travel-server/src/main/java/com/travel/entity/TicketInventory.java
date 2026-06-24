package com.travel.entity;

import java.util.Date;

public class TicketInventory {
    private Integer inventoryId;
    private Integer scenicId;
    private Date useDate;
    private Integer totalStock;
    private Integer availableStock;
    private Integer version;
    private Date createTime;
    private Date updateTime;

    public Integer getInventoryId() { return inventoryId; }
    public void setInventoryId(Integer inventoryId) { this.inventoryId = inventoryId; }
    public Integer getScenicId() { return scenicId; }
    public void setScenicId(Integer scenicId) { this.scenicId = scenicId; }
    public Date getUseDate() { return useDate; }
    public void setUseDate(Date useDate) { this.useDate = useDate; }
    public Integer getTotalStock() { return totalStock; }
    public void setTotalStock(Integer totalStock) { this.totalStock = totalStock; }
    public Integer getAvailableStock() { return availableStock; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
