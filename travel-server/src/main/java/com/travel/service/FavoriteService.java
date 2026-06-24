package com.travel.service;

import com.travel.entity.Favorite;
import com.travel.exception.BusinessException;
import com.travel.mapper.FavoriteMapper;
import com.travel.mapper.HotelMapper;
import com.travel.mapper.ScenicMapper;
import com.travel.mapper.TravelNoteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;
    @Autowired
    private ScenicMapper scenicMapper;
    @Autowired
    private HotelMapper hotelMapper;
    @Autowired
    private TravelNoteMapper travelNoteMapper;

    /** 收藏目标类型常量 */
    private static final int TARGET_SCENIC = 1;
    private static final int TARGET_HOTEL = 2;
    private static final int TARGET_NOTE = 3;

    /** 添加收藏（原子操作防 TOCTOU 竞态） */
    public void add(Favorite favorite) {
        validateTarget(favorite.getTargetType(), favorite.getTargetId());
        int rows = favoriteMapper.insertIgnore(favorite);
        if (rows == 0) {
            throw new BusinessException(400, "已收藏，请勿重复操作");
        }
    }

    /** 校验收藏目标实体是否存在，不存在则给出明确错误提示 */
    private void validateTarget(Integer targetType, Integer targetId) {
        if (targetType == null || targetId == null) {
            throw new BusinessException(400, "收藏目标参数不完整");
        }
        boolean exists;
        String targetName;
        switch (targetType) {
            case TARGET_SCENIC:
                exists = scenicMapper.selectById(targetId) != null;
                targetName = "景点";
                break;
            case TARGET_HOTEL:
                exists = hotelMapper.selectById(targetId) != null;
                targetName = "酒店";
                break;
            case TARGET_NOTE:
                exists = travelNoteMapper.selectById(targetId) != null;
                targetName = "游记";
                break;
            default:
                throw new BusinessException(400, "不支持的收藏类型");
        }
        if (!exists) {
            throw new BusinessException(404, targetName + "不存在");
        }
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
