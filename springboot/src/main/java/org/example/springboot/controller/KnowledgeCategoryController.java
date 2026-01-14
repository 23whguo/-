package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.DTO.command.CategoryCreateDTO;
import org.example.springboot.DTO.command.CategoryUpdateDTO;
import org.example.springboot.DTO.query.CategoryListQueryDTO;
import org.example.springboot.DTO.response.CategoryResponseDTO;
import org.example.springboot.common.Result;
import org.example.springboot.enumClass.UserType;
import org.example.springboot.service.KnowledgeCategoryService;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识分类管理控制器
 * @author system
 */
@Tag(name = "知识分类管理")
@RestController
@Slf4j
@RequestMapping("/knowledge/category")
public class KnowledgeCategoryController {

    @Resource
    private KnowledgeCategoryService categoryService;

    /**
     * 创建分类（管理员功能）
     */
    @Operation(summary = "创建知识分类")
    @PostMapping
    public Result<CategoryResponseDTO> createCategory(
            @Valid @RequestBody CategoryCreateDTO createDTO,
            HttpServletRequest request) {
        
        // 权限检查：只有管理员可以创建分类
        Integer currentUserRole = JwtTokenUtils.getCurrentUserRole();
        if (!UserType.ADMIN.getCode().equals(currentUserRole)) { // 2代表管理员
            return Result.error("权限不足");
        }

        log.info("管理员创建知识分类: {}", createDTO.getCategoryName());
        CategoryResponseDTO response = categoryService.createCategory(createDTO);
        return Result.success("创建分类成功", response);
    }

    /**
     * 更新分类（管理员功能）
     */
    @Operation(summary = "更新知识分类")
    @PutMapping("/{id}")
    public Result<CategoryResponseDTO> updateCategory(
            @Parameter(description = "分类ID") @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateDTO updateDTO,
            HttpServletRequest request) {
        
        // 权限检查：只有管理员可以更新分类
        Integer currentUserRole = JwtTokenUtils.getCurrentUserRole();
        if (!UserType.ADMIN.getCode().equals(currentUserRole)) { // 2代表管理员
            return Result.error("权限不足");
        }

        log.info("管理员更新知识分类: categoryId={}", id);
        CategoryResponseDTO response = categoryService.updateCategory(id, updateDTO);
        return Result.success("更新分类成功", response);
    }

    /**
     * 删除分类（管理员功能）
     */
    @Operation(summary = "删除知识分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(
            @Parameter(description = "分类ID") @PathVariable Long id,
            HttpServletRequest request) {
        
        // 权限检查：只有管理员可以删除分类
        Integer currentUserRole = JwtTokenUtils.getCurrentUserRole();
        if (!UserType.ADMIN.getCode().equals(currentUserRole)) { // 2代表管理员
            return Result.error("权限不足");
        }

        log.info("管理员删除知识分类: categoryId={}", id);
        categoryService.deleteCategory(id);
        return Result.success();
    }

    /**
     * 根据ID获取分类详情
     */
    @Operation(summary = "获取知识分类详情")
    @GetMapping("/{id}")
    public Result<CategoryResponseDTO> getCategoryById(
            @Parameter(description = "分类ID") @PathVariable Long id) {
        
        log.info("获取知识分类详情: categoryId={}", id);
        CategoryResponseDTO response = categoryService.getCategoryById(id);
        return Result.success(response);
    }

    /**
     * 分页查询分类列表
     */
    @Operation(summary = "分页查询知识分类列表")
    @GetMapping("/page")
    public Result<Page<CategoryResponseDTO>> getCategoryPage(
            @Parameter(description = "分类名称") @RequestParam(required = false) String categoryName,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Long currentPage,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size) {

        CategoryListQueryDTO queryDTO = new CategoryListQueryDTO();
        queryDTO.setCategoryName(categoryName);
        queryDTO.setStatus(status);
        queryDTO.setCurrentPage(currentPage);
        queryDTO.setSize(size);

        log.info("分页查询知识分类列表: page={}, size={}", currentPage, size);
        Page<CategoryResponseDTO> response = categoryService.getCategoryPage(queryDTO);
        return Result.success(response);
    }


    /**
     * 获取分类树（用于前端展示）
     */
    @Operation(summary = "获取知识分类树")
    @GetMapping("/tree")
    public Result<List<CategoryResponseDTO>> getCategoryTree() {
        log.info("获取知识分类树");
        List<CategoryResponseDTO> response = categoryService.getCategoryTree();
        return Result.success(response);
    }
}