package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.DTO.query.UserFavoriteQueryDTO;
import org.example.springboot.DTO.response.ArticleSimpleResponseDTO;
import org.example.springboot.common.Result;
import org.example.springboot.service.UserFavoriteService;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 用户收藏管理控制器
 * @author system
 */
@Tag(name = "用户收藏管理")
@RestController
@Slf4j
@RequestMapping("/knowledge/favorite")
public class UserFavoriteController {

    @Resource
    private UserFavoriteService favoriteService;

    /**
     * 收藏文章
     */
    @Operation(summary = "收藏文章")
    @PostMapping("/{articleId}")
    public Result<Void> favoriteArticle(
            @Parameter(description = "文章ID") @PathVariable String articleId,
            HttpServletRequest request) {
        
        // 获取当前用户ID
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

        log.info("用户收藏文章: userId={}, articleId={}", currentUserId, articleId);
        favoriteService.favoriteArticle(currentUserId, articleId);
        return Result.success();
    }

    /**
     * 取消收藏文章
     */
    @Operation(summary = "取消收藏文章")
    @DeleteMapping("/{articleId}")
    public Result<Void> unfavoriteArticle(
            @Parameter(description = "文章ID") @PathVariable String articleId,
            HttpServletRequest request) {
        
        // 获取当前用户ID
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

        log.info("用户取消收藏文章: userId={}, articleId={}", currentUserId, articleId);
        favoriteService.unfavoriteArticle(currentUserId, articleId);
        return Result.success();
    }

    /**
     * 检查文章是否已收藏
     */
    @Operation(summary = "检查文章是否已收藏")
    @GetMapping("/{articleId}/status")
    public Result<Boolean> checkFavoriteStatus(
            @Parameter(description = "文章ID") @PathVariable String articleId,
            HttpServletRequest request) {
        
        // 获取当前用户ID
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.success(false);
        }

        log.info("检查文章收藏状态: userId={}, articleId={}", currentUserId, articleId);
        boolean isFavorited = favoriteService.isFavorited(currentUserId, articleId);
        return Result.success(isFavorited);
    }

    /**
     * 分页查询用户收藏的文章
     */
    @Operation(summary = "分页查询用户收藏的文章")
    @GetMapping("/page")
    public Result<Page<ArticleSimpleResponseDTO>> getUserFavoritePage(
            @Parameter(description = "文章标题") @RequestParam(required = false) String title,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortField,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDirection,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Long currentPage,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            HttpServletRequest request) {

        // 获取当前用户ID
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

        UserFavoriteQueryDTO queryDTO = new UserFavoriteQueryDTO();
        queryDTO.setUserId(currentUserId);
        queryDTO.setTitle(title);
        queryDTO.setCategoryId(categoryId);
        queryDTO.setSortField(sortField);
        queryDTO.setSortDirection(sortDirection);
        queryDTO.setCurrentPage(currentPage);
        queryDTO.setSize(size);

        log.info("分页查询用户收藏文章: userId={}, page={}, size={}", currentUserId, currentPage, size);
        Page<ArticleSimpleResponseDTO> response = favoriteService.getUserFavoritePage(queryDTO);
        return Result.success(response);
    }

    /**
     * 获取用户收藏文章总数
     */
    @Operation(summary = "获取用户收藏文章总数")
    @GetMapping("/count")
    public Result<Long> getUserFavoriteCount(HttpServletRequest request) {
        
        // 获取当前用户ID
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

        log.info("获取用户收藏文章总数: userId={}", currentUserId);
        Long count = favoriteService.getUserFavoriteCount(currentUserId);
        return Result.success(count);
    }
}