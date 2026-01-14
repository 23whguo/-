package org.example.springboot.service.convert;

import org.example.springboot.DTO.command.ArticleCreateDTO;
import org.example.springboot.DTO.command.ArticleUpdateDTO;
import org.example.springboot.DTO.response.ArticleResponseDTO;
import org.example.springboot.DTO.response.ArticleSimpleResponseDTO;
import org.example.springboot.entity.KnowledgeArticle;
import org.example.springboot.enumClass.ArticleStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识文章转换类
 * @author system
 */
public class ArticleConvert {

    /**
     * 创建命令DTO转换为实体
     * @param createDTO 创建命令DTO
     * @param authorId 作者ID
     * @return 文章实体
     */
    public static KnowledgeArticle createCommandToEntity(ArticleCreateDTO createDTO, Long authorId) {
        KnowledgeArticle.KnowledgeArticleBuilder builder = KnowledgeArticle.builder()
                .categoryId(createDTO.getCategoryId())
                .title(createDTO.getTitle())
                .summary(createDTO.getSummary())
                .content(createDTO.getContent())
                .coverImage(createDTO.getCoverImage())
                .tags(createDTO.getTags())
                .authorId(authorId)
                .readCount(0)
                .status(createDTO.getStatus())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());
                
        // 如果前端提供了ID（UUID预生成），则使用该ID
        if (createDTO.getId() != null && !createDTO.getId().trim().isEmpty()) {
            builder.id(createDTO.getId());
        }
        
        return builder.build();
    }

    /**
     * 更新命令DTO转换为实体
     * @param updateDTO 更新命令DTO
     * @return 文章实体
     */
    public static KnowledgeArticle updateCommandToEntity(ArticleUpdateDTO updateDTO) {
        KnowledgeArticle article = new KnowledgeArticle();
        article.setCategoryId(updateDTO.getCategoryId());
        article.setTitle(updateDTO.getTitle());
        article.setSummary(updateDTO.getSummary());
        article.setContent(updateDTO.getContent());
        article.setCoverImage(updateDTO.getCoverImage());
        article.setTags(updateDTO.getTags());
        article.setStatus(updateDTO.getStatus());
        article.setUpdatedAt(LocalDateTime.now());
        return article;
    }

    /**
     * 实体转换为响应DTO
     * @param article 文章实体
     * @param categoryName 分类名称
     * @param authorName 作者名称
     * @param isFavorited 是否收藏
     * @return 文章响应DTO
     */
    public static ArticleResponseDTO entityToResponse(KnowledgeArticle article, 
                                                     String categoryName, 
                                                     String authorName, 
                                                     Boolean isFavorited) {
        return ArticleResponseDTO.builder()
                .id(article.getId())
                .categoryId(article.getCategoryId())
                .categoryName(categoryName)
                .title(article.getTitle())
                .summary(article.getSummary())
                .content(article.getContent())
                .coverImage(article.getCoverImage())
                .tags(article.getTags())
                .tagArray(article.getTagArray())
                .authorId(article.getAuthorId())
                .authorName(authorName)
                .readCount(article.getReadCount())
                .status(article.getStatus())
                .statusText(getStatusText(article.getStatus()))
                .isFavorited(isFavorited)
                .publishedAt(article.getPublishedAt())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }

    /**
     * 实体转换为简化响应DTO
     * @param article 文章实体
     * @param categoryName 分类名称
     * @param authorName 作者名称
     * @param isFavorited 是否收藏
     * @return 文章简化响应DTO
     */
    public static ArticleSimpleResponseDTO entityToSimpleResponse(KnowledgeArticle article, 
                                                                 String categoryName, 
                                                                 String authorName, 
                                                                 Boolean isFavorited) {
        return ArticleSimpleResponseDTO.builder()
                .id(article.getId())
                .categoryId(article.getCategoryId())
                .categoryName(categoryName)
                .title(article.getTitle())
                .summary(article.getAutoSummary()) // 使用自动摘要
                .coverImage(article.getCoverImage())
                .tags(article.getTags())
                .authorName(authorName)
                .readCount(article.getReadCount())
                .status(article.getStatus())
                .statusText(getStatusText(article.getStatus()))
                .isFavorited(isFavorited)
                .favoriteCount(0) // 默认值，需要在Service层中设置实际值
                .publishedAt(article.getPublishedAt())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }

    /**
     * 实体转换为简化响应DTO（带收藏数量）
     * @param article 文章实体
     * @param categoryName 分类名称
     * @param authorName 作者名称
     * @param isFavorited 是否收藏
     * @param favoriteCount 收藏数量
     * @return 文章简化响应DTO
     */
    public static ArticleSimpleResponseDTO entityToSimpleResponseWithFavoriteCount(KnowledgeArticle article, 
                                                                                   String categoryName, 
                                                                                   String authorName, 
                                                                                   Boolean isFavorited,
                                                                                   Integer favoriteCount) {
        return ArticleSimpleResponseDTO.builder()
                .id(article.getId())
                .categoryId(article.getCategoryId())
                .categoryName(categoryName)
                .title(article.getTitle())
                .summary(article.getAutoSummary()) // 使用自动摘要
                .coverImage(article.getCoverImage())
                .tags(article.getTags())
                .authorName(authorName)
                .readCount(article.getReadCount())
                .status(article.getStatus())
                .statusText(getStatusText(article.getStatus()))
                .isFavorited(isFavorited)
                .favoriteCount(favoriteCount)
                .publishedAt(article.getPublishedAt())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }

    /**
     * 实体转换为简化响应DTO（带收藏时间）
     * @param article 文章实体
     * @param categoryName 分类名称
     * @param authorName 作者名称
     * @param isFavorited 是否收藏
     * @param favoriteTime 收藏时间
     * @return 文章简化响应DTO
     */
    public static ArticleSimpleResponseDTO entityToSimpleResponseWithFavoriteTime(KnowledgeArticle article, 
                                                                                   String categoryName, 
                                                                                   String authorName, 
                                                                                   Boolean isFavorited,
                                                                                   LocalDateTime favoriteTime) {
        return ArticleSimpleResponseDTO.builder()
                .id(article.getId())
                .categoryId(article.getCategoryId())
                .categoryName(categoryName)
                .title(article.getTitle())
                .summary(article.getAutoSummary()) // 使用自动摘要
                .coverImage(article.getCoverImage())
                .tags(article.getTags())
                .authorName(authorName)
                .readCount(article.getReadCount())
                .status(article.getStatus())
                .statusText(getStatusText(article.getStatus()))
                .isFavorited(isFavorited)
                .favoriteCount(0) // 默认值，在MyFavorites页面不显示收藏数量
                .favoriteTime(favoriteTime)
                .publishedAt(article.getPublishedAt())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }

    /**
     * 实体列表转换为简化响应DTO列表
     * @param articles 文章实体列表
     * @return 文章简化响应DTO列表
     */
    public static List<ArticleSimpleResponseDTO> entityListToSimpleResponseList(List<KnowledgeArticle> articles) {
        return articles.stream()
                .map(article -> entityToSimpleResponse(article, null, null, false))
                .collect(Collectors.toList());
    }

    /**
     * 发布文章时更新实体
     * @param article 文章实体
     * @return 更新后的文章实体
     */
    public static KnowledgeArticle publishArticle(KnowledgeArticle article) {
        article.setStatus(ArticleStatus.PUBLISHED.getCode());
        article.setPublishedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());
        return article;
    }

    /**
     * 下线文章时更新实体
     * @param article 文章实体
     * @return 更新后的文章实体
     */
    public static KnowledgeArticle offlineArticle(KnowledgeArticle article) {
        article.setStatus(ArticleStatus.OFFLINE.getCode());
        article.setUpdatedAt(LocalDateTime.now());
        return article;
    }

    /**
     * 获取状态显示文本
     * @param status 状态代码
     * @return 状态显示文本
     */
    private static String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        try {
            return ArticleStatus.fromCode(status).getDescription();
        } catch (IllegalArgumentException e) {
            return "未知";
        }
    }
}