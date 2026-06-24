package com.travel.entity;

import java.util.Date;

public class Favorite {
    private Integer favoriteId;
    private Integer userId;
    private Integer targetType;
    private Integer targetId;
    private Date createTime;
    /** 关联查询的目标名称（景点名/酒店名/游记标题） */
    private String targetName;
    /** 关联查询的目标封面图 */
    private String targetCover;

    public Integer getFavoriteId() { return favoriteId; }
    public void setFavoriteId(Integer favoriteId) { this.favoriteId = favoriteId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getTargetType() { return targetType; }
    public void setTargetType(Integer targetType) { this.targetType = targetType; }
    public Integer getTargetId() { return targetId; }
    public void setTargetId(Integer targetId) { this.targetId = targetId; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    public String getTargetCover() { return targetCover; }
    public void setTargetCover(String targetCover) { this.targetCover = targetCover; }
}
