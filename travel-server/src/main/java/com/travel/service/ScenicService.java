package com.travel.service;

import com.travel.dto.ApiResponse;
import com.travel.dto.PageRequest;
import com.travel.entity.Scenic;
import com.travel.exception.BusinessException;
import com.travel.mapper.FavoriteMapper;
import com.travel.mapper.ScenicMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScenicService {

    private static final Logger log = LoggerFactory.getLogger(ScenicService.class);

    @Autowired
    private ScenicMapper scenicMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    /** 景点详情 */
    public Scenic getById(Integer scenicId) {
        Scenic scenic = scenicMapper.selectById(scenicId);
        if (scenic == null) {
            throw new BusinessException(404, "景点不存在");
        }
        scenicMapper.updateViewCount(scenicId);
        return scenic;
    }

    /** 按城市查询 */
    public List<Scenic> listByCity(String city) {
        return scenicMapper.selectByCity(city);
    }

    /** 热门景点 */
    public List<Scenic> listHot(String city, int limit) {
        return scenicMapper.selectHotScenics(city, limit);
    }

    /** 搜索景点（分页） */
    public ApiResponse.PageResult<Scenic> searchPage(String city, String keyword, String tags,
                                                      Integer minLevel, PageRequest page) {
        List<Scenic> list = scenicMapper.selectByConditionPage(city, keyword, tags, minLevel,
                page.getOffset(), page.getPageSize());
        long total = scenicMapper.countByCondition(city, keyword, tags, minLevel);
        return ApiResponse.page(list, total, page.getPage(), page.getPageSize());
    }

    /** 基于标签的推荐 */
    public List<Scenic> recommend(Integer userId, String city, int limit) {
        if (userId == null) {
            return scenicMapper.selectHotScenics(city, limit);
        }
        List<Integer> favIds = favoriteMapper.selectByUserAndType(userId, 1);
        if (favIds.isEmpty()) {
            return scenicMapper.selectHotScenics(city, limit);
        }
        Set<String> tagSet = new HashSet<>();
        for (Integer id : favIds) {
            Scenic s = scenicMapper.selectById(id);
            if (s != null && s.getTags() != null) {
                for (String t : s.getTags().split(",")) {
                    tagSet.add(t.trim());
                }
            }
        }
        if (tagSet.isEmpty()) {
            return scenicMapper.selectHotScenics(city, limit);
        }
        List<String> tags = new ArrayList<>(tagSet);
        return scenicMapper.selectByTags(tags, Math.min(limit, 20));
    }

    /** 附近景点（搜索半径约0.5度≈55km，同城景点基本覆盖） */
    public List<Scenic> nearby(BigDecimal lng, BigDecimal lat, int limit) {
        BigDecimal offset = new BigDecimal("0.5");
        return scenicMapper.selectNearby(lng.subtract(offset), lng.add(offset),
                lat.subtract(offset), lat.add(offset), Math.min(limit, 20));
    }
}
