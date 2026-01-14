package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import org.example.springboot.entity.KnowledgeCategory;
import org.example.springboot.entity.KnowledgeArticle;
import org.example.springboot.mapper.KnowledgeCategoryMapper;
import org.example.springboot.mapper.KnowledgeArticleMapper;
import org.example.springboot.DTO.command.CategoryCreateDTO;
import org.example.springboot.DTO.command.CategoryUpdateDTO;
import org.example.springboot.DTO.query.CategoryListQueryDTO;
import org.example.springboot.DTO.response.CategoryResponseDTO;
import org.example.springboot.enumClass.CategoryStatus;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.service.convert.CategoryConvert;

/**
 * 知识分类业务逻辑层
 * @author system
 */
@Slf4j
@Service
public class KnowledgeCategoryService {

    @Resource
    private KnowledgeCategoryMapper categoryMapper;

    @Resource
    private KnowledgeArticleMapper articleMapper;

    /**
     * 创建分类
     * @param createDTO 创建命令
     * @return 分类信息
     */
    @Transactional(rollbackFor = Exception.class)
    public CategoryResponseDTO createCategory(CategoryCreateDTO createDTO) {
        try {
            // 检查分类名称是否重复
            LambdaQueryWrapper<KnowledgeCategory> nameQuery = new LambdaQueryWrapper<>();
            nameQuery.eq(KnowledgeCategory::getCategoryName, createDTO.getCategoryName());
            if (categoryMapper.selectCount(nameQuery) > 0) {
                throw new BusinessException("分类名称已存在");
            }

            // 创建分类
            KnowledgeCategory category = CategoryConvert.createCommandToEntity(createDTO);
            categoryMapper.insert(category);

            log.info("创建知识分类成功: {}", category.getCategoryName());
            return CategoryConvert.entityToResponse(category);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建知识分类失败", e);
            throw new ServiceException("创建分类失败，请稍后重试");
        }
    }

    /**
     * 更新分类
     * @param categoryId 分类ID
     * @param updateDTO 更新命令
     * @return 分类信息
     */
    @Transactional(rollbackFor = Exception.class)
    public CategoryResponseDTO updateCategory(Long categoryId, CategoryUpdateDTO updateDTO) {
        try {
            // 检查分类是否存在
            KnowledgeCategory existingCategory = categoryMapper.selectById(categoryId);
            if (existingCategory == null) {
                throw new BusinessException("分类不存在");
            }

            // 检查分类名称是否被其他分类使用
            if (StringUtils.hasText(updateDTO.getCategoryName()) && 
                !updateDTO.getCategoryName().equals(existingCategory.getCategoryName())) {
                LambdaQueryWrapper<KnowledgeCategory> nameQuery = new LambdaQueryWrapper<>();
                nameQuery.eq(KnowledgeCategory::getCategoryName, updateDTO.getCategoryName())
                        .ne(KnowledgeCategory::getId, categoryId);
                if (categoryMapper.selectCount(nameQuery) > 0) {
                    throw new BusinessException("分类名称已被其他分类使用");
                }
            }

            // 更新分类
            KnowledgeCategory updateCategory = CategoryConvert.updateCommandToEntity(updateDTO);
            LambdaUpdateWrapper<KnowledgeCategory> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(KnowledgeCategory::getId, categoryId);
            
            if (StringUtils.hasText(updateCategory.getCategoryName())) {
                updateWrapper.set(KnowledgeCategory::getCategoryName, updateCategory.getCategoryName());
            }
            if (StringUtils.hasText(updateCategory.getDescription())) {
                updateWrapper.set(KnowledgeCategory::getDescription, updateCategory.getDescription());
            }
            if (updateCategory.getSortOrder() != null) {
                updateWrapper.set(KnowledgeCategory::getSortOrder, updateCategory.getSortOrder());
            }
            if (updateCategory.getStatus() != null) {
                updateWrapper.set(KnowledgeCategory::getStatus, updateCategory.getStatus());
            }
            updateWrapper.set(KnowledgeCategory::getUpdatedAt, LocalDateTime.now());

            categoryMapper.update(null, updateWrapper);

            // 获取更新后的分类
            KnowledgeCategory updatedCategory = categoryMapper.selectById(categoryId);
            log.info("更新知识分类成功: {}", updatedCategory.getCategoryName());
            
            return CategoryConvert.entityToResponse(updatedCategory);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新知识分类失败", e);
            throw new ServiceException("更新分类失败，请稍后重试");
        }
    }

    /**
     * 删除分类
     * @param categoryId 分类ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long categoryId) {
        try {
            // 检查分类是否存在
            KnowledgeCategory category = categoryMapper.selectById(categoryId);
            if (category == null) {
                throw new BusinessException("分类不存在");
            }

            // 检查是否有关联文章
            checkRelatedArticles(categoryId);

            // 删除分类
            categoryMapper.deleteById(categoryId);
            log.info("删除知识分类成功: {}", category.getCategoryName());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除知识分类失败", e);
            throw new ServiceException("删除分类失败，请稍后重试");
        }
    }

    /**
     * 根据ID获取分类
     * @param categoryId 分类ID
     * @return 分类信息
     */
    public CategoryResponseDTO getCategoryById(Long categoryId) {
        KnowledgeCategory category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 统计文章数量
        Integer articleCount = getArticleCount(categoryId);

        return CategoryConvert.entityToResponseWithStats(category, articleCount);
    }

    /**
     * 分页查询分类列表
     * @param queryDTO 查询条件
     * @return 分类分页列表
     */
    public Page<CategoryResponseDTO> getCategoryPage(CategoryListQueryDTO queryDTO) {
        try {
            Page<KnowledgeCategory> page = new Page<>(queryDTO.getCurrentPage(), queryDTO.getSize());

            // 构建查询条件
            LambdaQueryWrapper<KnowledgeCategory> queryWrapper = new LambdaQueryWrapper<>();
            
            if (StringUtils.hasText(queryDTO.getCategoryName())) {
                queryWrapper.like(KnowledgeCategory::getCategoryName, queryDTO.getCategoryName());
            }
            if (queryDTO.getStatus() != null) {
                queryWrapper.eq(KnowledgeCategory::getStatus, queryDTO.getStatus());
            }

            // 排序
            queryWrapper.orderByAsc(KnowledgeCategory::getSortOrder)
                       .orderByDesc(KnowledgeCategory::getCreatedAt);

            Page<KnowledgeCategory> categoryPage = categoryMapper.selectPage(page, queryWrapper);

            // 转换为响应DTO
            Page<CategoryResponseDTO> responsePage = new Page<>();
            responsePage.setCurrent(categoryPage.getCurrent());
            responsePage.setSize(categoryPage.getSize());
            responsePage.setTotal(categoryPage.getTotal());
            responsePage.setPages(categoryPage.getPages());

            List<CategoryResponseDTO> responseList = categoryPage.getRecords().stream()
                    .map(category -> {
                        Integer articleCount = getArticleCount(category.getId());
                        return CategoryConvert.entityToResponseWithStats(category, articleCount);
                    })
                    .toList();

            responsePage.setRecords(responseList);
            return responsePage;

        } catch (Exception e) {
            log.error("查询知识分类列表失败", e);
            throw new ServiceException("查询分类列表失败，请稍后重试");
        }
    }

    /**
     * 获取所有启用的分类（树形结构）
     * @return 分类树列表
     */
    public List<CategoryResponseDTO> getCategoryTree() {
        try {
            // 查询所有启用的分类
            LambdaQueryWrapper<KnowledgeCategory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(KnowledgeCategory::getStatus, CategoryStatus.ENABLED.getCode())
                       .orderByAsc(KnowledgeCategory::getSortOrder)
                       .orderByDesc(KnowledgeCategory::getCreatedAt);

            List<KnowledgeCategory> allCategories = categoryMapper.selectList(queryWrapper);
            
            // 转换为响应DTO，包含文章数量统计
            return allCategories.stream()
                    .map(category -> {
                        Integer articleCount = getArticleCount(category.getId());
                        return CategoryConvert.entityToResponseWithStats(category, articleCount);
                    })
                    .toList();

        } catch (Exception e) {
            log.error("获取分类树失败", e);
            throw new ServiceException("获取分类树失败，请稍后重试");
        }
    }


    /**
     * 检查关联的文章
     */
    private void checkRelatedArticles(Long categoryId) {
        LambdaQueryWrapper<KnowledgeArticle> articleQuery = new LambdaQueryWrapper<>();
        articleQuery.eq(KnowledgeArticle::getCategoryId, categoryId);
        Long articleCount = articleMapper.selectCount(articleQuery);
        if (articleCount > 0) {
            throw new BusinessException("该分类下存在文章，无法删除");
        }
    }


    /**
     * 获取文章数量
     */
    private Integer getArticleCount(Long categoryId) {
        LambdaQueryWrapper<KnowledgeArticle> articleQuery = new LambdaQueryWrapper<>();
        articleQuery.eq(KnowledgeArticle::getCategoryId, categoryId);
        return Math.toIntExact(articleMapper.selectCount(articleQuery));
    }
}