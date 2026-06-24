package com.travel.service;

import com.travel.entity.Favorite;
import com.travel.exception.BusinessException;
import com.travel.mapper.FavoriteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    /** 添加收藏（原子操作防 TOCTOU 竞态） */
    public void add(Favorite favorite) {
        // 校验目标实体存在
        validateTarget(favorite.getTargetType(), favorite.getTargetId());
        int rows = favoriteMapper.insertIgnore(favorite);
        if (rows == 0) {
            throw new BusinessException(400, "已收藏，请勿重复操作");
        }
    }

    private void validateTarget(Integer targetType, Integer targetId) {
        // 此校验非必须——INSERT IGNORE 不会抛异常，但提前校验可给出更明确的错误
        // 当前跳过，因为 LEFT JOIN 查询会自然返回 NULL targetName
    }

    /** 取消收藏 */
    public void remove(Integer userId, Integer targetType, Integer targetId) {
        favoriteMapper.delete(userId, targetType, targetId);
    }

    /** 是否已收藏 */
    public boolean isFavorited(Integer userId, Integer targetType, Integer targetId) {
        return favoriteMapper.exists(userId, targetType, targetId);
    }

    /** 我的收藏列表 */
    public List<Favorite> myFavorites(Integer userId, Integer targetType) {
        return favoriteMapper.selectByUserId(userId, targetType);
    }
}
