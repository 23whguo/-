package org.example.springboot.service.convert;

import org.example.springboot.DTO.command.CategoryCreateDTO;
import org.example.springboot.DTO.command.CategoryUpdateDTO;
import org.example.springboot.DTO.response.CategoryResponseDTO;
import org.example.springboot.entity.KnowledgeCategory;
import org.example.springboot.enumClass.CategoryStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识分类转换类
 * @author system
 */
public class CategoryConvert {

    /**
     * 创建命令DTO转换为实体
     * @param createDTO 创建命令DTO
     * @return 分类实体
     */
    public static KnowledgeCategory createCommandToEntity(CategoryCreateDTO createDTO) {
        return KnowledgeCategory.builder()
                .categoryName(createDTO.getCategoryName())
                .description(createDTO.getDescription())
                .sortOrder(createDTO.getSortOrder())
                .status(createDTO.getStatus())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 更新命令DTO转换为实体
     * @param updateDTO 更新命令DTO
     * @return 分类实体
     */
    public static KnowledgeCategory updateCommandToEntity(CategoryUpdateDTO updateDTO) {
        KnowledgeCategory category = new KnowledgeCategory();
        category.setCategoryName(updateDTO.getCategoryName());
        category.setDescription(updateDTO.getDescription());
        category.setSortOrder(updateDTO.getSortOrder());
        category.setStatus(updateDTO.getStatus());
        category.setUpdatedAt(LocalDateTime.now());
        return category;
    }

    /**
     * 实体转换为响应DTO
     * @param category 分类实体
     * @return 分类响应DTO
     */
    public static CategoryResponseDTO entityToResponse(KnowledgeCategory category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .sortOrder(category.getSortOrder())
                .status(category.getStatus())
                .statusText(getStatusText(category.getStatus()))
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    /**
     * 实体转换为响应DTO（包含统计信息）
     * @param category 分类实体
     * @param articleCount 文章数量
     * @return 分类响应DTO
     */
    public static CategoryResponseDTO entityToResponseWithStats(KnowledgeCategory category, 
                                                               Integer articleCount) {
        CategoryResponseDTO response = entityToResponse(category);
        response.setArticleCount(articleCount);
        return response;
    }

    /**
     * 实体列表转换为响应DTO列表
     * @param categories 分类实体列表
     * @return 分类响应DTO列表
     */
    public static List<CategoryResponseDTO> entityListToResponseList(List<KnowledgeCategory> categories) {
        return categories.stream()
                .map(CategoryConvert::entityToResponse)
                .collect(Collectors.toList());
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
            return CategoryStatus.fromCode(status).getDescription();
        } catch (IllegalArgumentException e) {
            return "未知";
        }
    }
}