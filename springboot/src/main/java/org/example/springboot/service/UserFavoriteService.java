package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.springboot.entity.UserFavorite;
import org.example.springboot.entity.KnowledgeArticle;
import org.example.springboot.entity.KnowledgeCategory;
import org.example.springboot.entity.User;
import org.example.springboot.mapper.UserFavoriteMapper;
import org.example.springboot.mapper.KnowledgeArticleMapper;
import org.example.springboot.mapper.KnowledgeCategoryMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.DTO.query.UserFavoriteQueryDTO;
import org.example.springboot.DTO.response.ArticleSimpleResponseDTO;
import org.example.springboot.enumClass.ArticleStatus;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.service.convert.ArticleConvert;

/**
 * 用户收藏业务逻辑层
 * @author system
 */
@Slf4j
@Service
public class UserFavoriteService {

    @Resource
    private UserFavoriteMapper favoriteMapper;

    @Resource
    private KnowledgeArticleMapper articleMapper;

    @Resource
    private KnowledgeCategoryMapper categoryMapper;

    @Resource
    private UserMapper userMapper;

    /**
     * 收藏文章
     * @param userId 用户ID
     * @param articleId 文章ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void favoriteArticle(Long userId, String articleId) {
        try {
            // 验证用户是否存在
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // 验证文章是否存在且已发布
            KnowledgeArticle article = articleMapper.selectById(articleId);
            if (article == null) {
                throw new BusinessException("文章不存在");
            }
            if (!article.isPublished()) {
                throw new BusinessException("只能收藏已发布的文章");
            }

            // 检查是否已收藏
            LambdaQueryWrapper<UserFavorite> existQuery = new LambdaQueryWrapper<>();
            existQuery.eq(UserFavorite::getUserId, userId)
                     .eq(UserFavorite::getArticleId, articleId);
            if (favoriteMapper.selectCount(existQuery) > 0) {
                throw new BusinessException("已收藏该文章");
            }

            // 创建收藏记录
            UserFavorite favorite = UserFavorite.builder()
                    .userId(userId)
                    .articleId(articleId)
                    .createdAt(LocalDateTime.now())
                    .build();

            favoriteMapper.insert(favorite);
            log.info("用户收藏文章成功: userId={}, articleId={}", userId, articleId);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("收藏文章失败", e);
            throw new ServiceException("收藏失败，请稍后重试");
        }
    }

    /**
     * 取消收藏文章
     * @param userId 用户ID
     * @param articleId 文章ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void unfavoriteArticle(Long userId, String articleId) {
        try {
            // 查找收藏记录
            LambdaQueryWrapper<UserFavorite> query = new LambdaQueryWrapper<>();
            query.eq(UserFavorite::getUserId, userId)
                 .eq(UserFavorite::getArticleId, articleId);
            
            UserFavorite favorite = favoriteMapper.selectOne(query);
            if (favorite == null) {
                throw new BusinessException("未收藏该文章");
            }

            // 删除收藏记录
            favoriteMapper.deleteById(favorite.getId());
            log.info("用户取消收藏文章成功: userId={}, articleId={}", userId, articleId);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("取消收藏文章失败", e);
            throw new ServiceException("取消收藏失败，请稍后重试");
        }
    }

    /**
     * 检查用户是否收藏了文章
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return 是否收藏
     */
    public boolean isFavorited(Long userId, String articleId) {
        LambdaQueryWrapper<UserFavorite> query = new LambdaQueryWrapper<>();
        query.eq(UserFavorite::getUserId, userId)
             .eq(UserFavorite::getArticleId, articleId);
        return favoriteMapper.selectCount(query) > 0;
    }

    /**
     * 分页查询用户收藏的文章
     * @param queryDTO 查询条件
     * @return 收藏文章分页列表
     */
    public Page<ArticleSimpleResponseDTO> getUserFavoritePage(UserFavoriteQueryDTO queryDTO) {
        try {
            Page<UserFavorite> page = new Page<>(queryDTO.getCurrentPage(), queryDTO.getSize());

            // 构建查询条件
            LambdaQueryWrapper<UserFavorite> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserFavorite::getUserId, queryDTO.getUserId());

            // 排序
            String sortField = queryDTO.getSortField();
            String sortDirection = queryDTO.getSortDirection();
            if ("title".equals(sortField)) {
                // 如果按标题排序，需要关联查询
                queryWrapper.orderByDesc(UserFavorite::getCreatedAt);
            } else {
                // 默认按收藏时间排序
                if ("asc".equals(sortDirection)) {
                    queryWrapper.orderByAsc(UserFavorite::getCreatedAt);
                } else {
                    queryWrapper.orderByDesc(UserFavorite::getCreatedAt);
                }
            }

            Page<UserFavorite> favoritePage = favoriteMapper.selectPage(page, queryWrapper);

            // 获取文章信息
            List<String> articleIds = favoritePage.getRecords().stream()
                    .map(UserFavorite::getArticleId)
                    .toList();

            if (articleIds.isEmpty()) {
                Page<ArticleSimpleResponseDTO> responsePage = new Page<>();
                responsePage.setCurrent(favoritePage.getCurrent());
                responsePage.setSize(favoritePage.getSize());
                responsePage.setTotal(favoritePage.getTotal());
                responsePage.setPages(favoritePage.getPages());
                responsePage.setRecords(List.of());
                return responsePage;
            }

            // 查询文章信息
            LambdaQueryWrapper<KnowledgeArticle> articleQuery = new LambdaQueryWrapper<>();
            articleQuery.in(KnowledgeArticle::getId, articleIds)
                       .eq(KnowledgeArticle::getStatus, ArticleStatus.PUBLISHED.getCode());

            // 应用额外的筛选条件
            if (queryDTO.getCategoryId() != null) {
                articleQuery.eq(KnowledgeArticle::getCategoryId, queryDTO.getCategoryId());
            }
            if (queryDTO.getTitle() != null && !queryDTO.getTitle().trim().isEmpty()) {
                articleQuery.like(KnowledgeArticle::getTitle, queryDTO.getTitle());
            }

            List<KnowledgeArticle> articles = articleMapper.selectList(articleQuery);

            // 构建响应DTO
            Page<ArticleSimpleResponseDTO> responsePage = new Page<>();
            responsePage.setCurrent(favoritePage.getCurrent());
            responsePage.setSize(favoritePage.getSize());
            responsePage.setTotal(favoritePage.getTotal());
            responsePage.setPages(favoritePage.getPages());

            List<ArticleSimpleResponseDTO> responseList = buildFavoriteArticleResponseList(
                    articles, favoritePage.getRecords(), queryDTO.getSortField(), queryDTO.getSortDirection());

            responsePage.setRecords(responseList);
            return responsePage;

        } catch (Exception e) {
            log.error("查询用户收藏文章列表失败", e);
            throw new ServiceException("查询收藏列表失败，请稍后重试");
        }
    }

    /**
     * 获取用户收藏文章总数
     * @param userId 用户ID
     * @return 收藏总数
     */
    public Long getUserFavoriteCount(Long userId) {
        LambdaQueryWrapper<UserFavorite> query = new LambdaQueryWrapper<>();
        query.eq(UserFavorite::getUserId, userId);
        return favoriteMapper.selectCount(query);
    }

    /**
     * 批量获取文章收藏数量
     * @param articleIds 文章ID列表
     * @return 文章ID到收藏数量的映射
     */
    public Map<String, Integer> getArticleFavoriteCountMap(List<String> articleIds) {
        if (articleIds.isEmpty()) {
            return Map.of();
        }
        
        LambdaQueryWrapper<UserFavorite> query = new LambdaQueryWrapper<>();
        query.in(UserFavorite::getArticleId, articleIds)
             .select(UserFavorite::getArticleId);
        
        List<UserFavorite> favorites = favoriteMapper.selectList(query);
        
        return favorites.stream()
                .collect(Collectors.groupingBy(
                        UserFavorite::getArticleId,
                        Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));
    }

    /**
     * 构建收藏文章响应DTO列表
     */
    private List<ArticleSimpleResponseDTO> buildFavoriteArticleResponseList(List<KnowledgeArticle> articles,
                                                                           List<UserFavorite> favorites,
                                                                           String sortField,
                                                                           String sortDirection) {
        if (articles.isEmpty()) {
            return List.of();
        }

        // 获取分类信息
        List<Long> categoryIds = articles.stream()
                .map(KnowledgeArticle::getCategoryId)
                .distinct()
                .toList();
        
        Map<Long, String> categoryMap = categoryMapper.selectBatchIds(categoryIds)
                .stream()
                .collect(Collectors.toMap(KnowledgeCategory::getId, KnowledgeCategory::getCategoryName));

        // 获取作者信息
        List<Long> authorIds = articles.stream()
                .map(KnowledgeArticle::getAuthorId)
                .distinct()
                .toList();
        
        Map<Long, String> authorMap = userMapper.selectBatchIds(authorIds)
                .stream()
                .collect(Collectors.toMap(User::getId, User::getDisplayName));

        // 创建收藏时间映射
        Map<String, LocalDateTime> favoriteTimeMap = favorites.stream()
                .collect(Collectors.toMap(UserFavorite::getArticleId, UserFavorite::getCreatedAt));

        List<ArticleSimpleResponseDTO> responseList = articles.stream()
                .map(article -> ArticleConvert.entityToSimpleResponseWithFavoriteTime(
                        article,
                        categoryMap.getOrDefault(article.getCategoryId(), "未知分类"),
                        authorMap.getOrDefault(article.getAuthorId(), "未知作者"),
                        true, // 都是收藏的文章
                        favoriteTimeMap.get(article.getId()) // 收藏时间
                ))
                .collect(Collectors.toList());

        // 按指定字段排序
        if ("title".equals(sortField)) {
            if ("asc".equals(sortDirection)) {
                responseList.sort((a, b) -> a.getTitle().compareTo(b.getTitle()));
            } else {
                responseList.sort((a, b) -> b.getTitle().compareTo(a.getTitle()));
            }
        } else {
            // 按收藏时间排序
            if ("asc".equals(sortDirection)) {
                responseList.sort((a, b) -> favoriteTimeMap.get(a.getId()).compareTo(favoriteTimeMap.get(b.getId())));
            } else {
                responseList.sort((a, b) -> favoriteTimeMap.get(b.getId()).compareTo(favoriteTimeMap.get(a.getId())));
            }
        }

        return responseList;
    }
}