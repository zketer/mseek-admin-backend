package com.lynn.museum.info.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lynn.museum.info.dto.ExhibitionResponse;
import com.lynn.museum.info.dto.MuseumResponse;
import com.lynn.museum.info.mapper.UserExhibitionFavoriteMapper;
import com.lynn.museum.info.mapper.UserMuseumFavoriteMapper;
import com.lynn.museum.info.model.entity.UserExhibitionFavorite;
import com.lynn.museum.info.model.entity.UserMuseumFavorite;
import com.lynn.museum.info.service.MiniAppUserFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户收藏服务实现类
 *
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniAppUserFavoriteServiceImpl implements MiniAppUserFavoriteService {

    private final UserMuseumFavoriteMapper userMuseumFavoriteMapper;
    private final UserExhibitionFavoriteMapper userExhibitionFavoriteMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean favoriteMuseum(Long userId, Long museumId) {
        // 使用忽略逻辑删除的查询方法，查询所有记录（包括deleted=1的）
        UserMuseumFavorite existing = userMuseumFavoriteMapper.selectByUserIdAndMuseumIdIgnoreLogic(userId, museumId);
        
        if (existing != null) {
            if (existing.getDeleted() == 0) {
                // 已收藏，返回成功
                return true;
            } else {
                // 恢复收藏（使用原生SQL强制更新 deleted=0）
                int result = userMuseumFavoriteMapper.updateDeletedById(existing.getId(), 0);
                boolean success = result > 0;
                if (success) {
                    log.info("恢复博物馆收藏: userId={}, museumId={}", userId, museumId);
                }
                return success;
            }
        }

        // 创建新收藏
        UserMuseumFavorite favorite = new UserMuseumFavorite();
        favorite.setUserId(userId);
        favorite.setMuseumId(museumId);
        favorite.setDeleted(0);

        boolean success = userMuseumFavoriteMapper.insert(favorite) > 0;
        if (success) {
            log.info("收藏博物馆: userId={}, museumId={}", userId, museumId);
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unfavoriteMuseum(Long userId, Long museumId) {
        // 基于 userId + museumId 查询（不限制 deleted 状态）
        UserMuseumFavorite favorite = userMuseumFavoriteMapper.selectByUserIdAndMuseumIdIgnoreLogic(userId, museumId);
        
        if (favorite == null || favorite.getDeleted() == 1) {
            // 没有记录或已取消，返回成功
            return true;
        }

        // 使用原生SQL强制更新 deleted=1（绕过@TableLogic）
        int result = userMuseumFavoriteMapper.updateDeletedById(favorite.getId(), 1);
        boolean success = result > 0;
        if (success) {
            log.info("取消收藏博物馆: userId={}, museumId={}", userId, museumId);
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean favoriteExhibition(Long userId, Long exhibitionId) {
        // 使用忽略逻辑删除的查询方法，查询所有记录（包括deleted=1的）
        UserExhibitionFavorite existing = userExhibitionFavoriteMapper.selectByUserIdAndExhibitionIdIgnoreLogic(userId, exhibitionId);
        
        if (existing != null) {
            if (existing.getDeleted() == 0) {
                // 已收藏，返回成功
                return true;
            } else {
                // 恢复收藏（使用原生SQL强制更新 deleted=0）
                int result = userExhibitionFavoriteMapper.updateDeletedById(existing.getId(), 0);
                boolean success = result > 0;
                if (success) {
                    log.info("恢复展览收藏: userId={}, exhibitionId={}", userId, exhibitionId);
                }
                return success;
            }
        }

        // 创建新收藏
        UserExhibitionFavorite favorite = new UserExhibitionFavorite();
        favorite.setUserId(userId);
        favorite.setExhibitionId(exhibitionId);
        favorite.setDeleted(0);

        boolean success = userExhibitionFavoriteMapper.insert(favorite) > 0;
        if (success) {
            log.info("收藏展览: userId={}, exhibitionId={}", userId, exhibitionId);
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unfavoriteExhibition(Long userId, Long exhibitionId) {
        // 基于 userId + exhibitionId 查询（不限制 deleted 状态）
        UserExhibitionFavorite favorite = userExhibitionFavoriteMapper.selectByUserIdAndExhibitionIdIgnoreLogic(userId, exhibitionId);
        
        if (favorite == null || favorite.getDeleted() == 1) {
            // 没有记录或已取消，返回成功
            return true;
        }

        // 使用原生SQL强制更新 deleted=1（绕过@TableLogic）
        int result = userExhibitionFavoriteMapper.updateDeletedById(favorite.getId(), 1);
        boolean success = result > 0;
        if (success) {
            log.info("取消收藏展览: userId={}, exhibitionId={}", userId, exhibitionId);
        }
        return success;
    }

    @Override
    public IPage<MuseumResponse> getUserFavoriteMuseums(Long userId, Integer page, Integer pageSize, 
                                                       String keyword, Boolean visitStatus, String sortBy) {
        log.info("查询用户收藏的博物馆 - 用户ID：{}, 页码：{}, 大小：{}", userId, page, pageSize);

        Page<MuseumResponse> pageRequest = new Page<>(page, pageSize);
        return userMuseumFavoriteMapper.selectUserFavoriteMuseums(pageRequest, userId, keyword, visitStatus, sortBy);
    }

    @Override
    public IPage<ExhibitionResponse> getUserFavoriteExhibitions(Long userId, Integer page, Integer pageSize,
                                                              String keyword, Integer status, String sortBy) {
        log.info("查询用户收藏的展览 - 用户ID：{}, 页码：{}, 大小：{}", userId, page, pageSize);

        Page<ExhibitionResponse> pageRequest = new Page<>(page, pageSize);
        return userExhibitionFavoriteMapper.selectUserFavoriteExhibitions(pageRequest, userId, keyword, status, sortBy);
    }

    @Override
    public Boolean isMuseumFavorited(Long userId, Long museumId) {
        Integer count = userMuseumFavoriteMapper.checkUserFavoriteMuseum(userId, museumId);
        return count != null && count > 0;
    }

    @Override
    public Boolean isExhibitionFavorited(Long userId, Long exhibitionId) {
        Integer count = userExhibitionFavoriteMapper.checkUserFavoriteExhibition(userId, exhibitionId);
        return count != null && count > 0;
    }

    @Override
    public FavoriteStats getUserFavoriteStats(Long userId) {
        log.info("获取用户收藏统计 - 用户ID：{}", userId);

        Integer museumCount = userMuseumFavoriteMapper.countUserFavoriteMuseums(userId);
        Integer exhibitionCount = userExhibitionFavoriteMapper.countUserFavoriteExhibitions(userId);

        return new FavoriteStats(
            museumCount != null ? museumCount : 0,
            exhibitionCount != null ? exhibitionCount : 0
        );
    }
}
