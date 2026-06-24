package com.travel.entity;

import java.math.BigDecimal;
import java.util.Date;

public class Scenic {
    private Integer scenicId;
    private String scenicName;
    private String city;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String description;
    private String openTime;
    private BigDecimal ticketPrice;
    private Integer recommendLevel;
    private String coverImage;
    private String images;
    private String tags;
    private Integer viewCount;
    private Integer status;
    private Date createTime;

    public Integer getScenicId() { return scenicId; }
    public void setScenicId(Integer scenicId) { this.scenicId = scenicId; }
    public String getScenicName() { return scenicName; }
    public void setScenicName(String scenicName) { this.scenicName = scenicName; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getOpenTime() { return openTime; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }
    public BigDecimal getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(BigDecimal ticketPrice) { this.ticketPrice = ticketPrice; }
    public Integer getRecommendLevel() { return recommendLevel; }
    public void setRecommendLevel(Integer recommendLevel) { this.recommendLevel = recommendLevel; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
