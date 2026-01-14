package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.DTO.command.ArticleCreateDTO;
import org.example.springboot.DTO.command.ArticleUpdateDTO;
import org.example.springboot.DTO.command.ArticleStatusUpdateDTO;
import org.example.springboot.DTO.command.ArticleBatchDeleteDTO;
import org.example.springboot.DTO.query.ArticleListQueryDTO;
import org.example.springboot.DTO.response.ArticleResponseDTO;
import org.example.springboot.DTO.response.ArticleSimpleResponseDTO;
import org.example.springboot.DTO.response.ArticleStatisticsResponseDTO;
import org.example.springboot.common.Result;
import org.example.springboot.service.KnowledgeArticleService;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 知识文章管理控制器
 * @author system
 */
@Tag(name = "知识文章管理")
@RestController
@Slf4j
@RequestMapping("/knowledge/article")
public class KnowledgeArticleController {

    @Resource
    private KnowledgeArticleService articleService;

    /**
     * 创建文章
     */
    @Operation(summary = "创建知识文章")
    @PostMapping
    public Result<ArticleResponseDTO> createArticle(
            @Valid @RequestBody ArticleCreateDTO createDTO,
            HttpServletRequest request) {
        
        // 获取当前用户ID
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

        // 添加详细日志
        log.info("📝 用户创建知识文章: userId={}, title={}, contentLength={}, summary={}", 
            currentUserId, createDTO.getTitle(), 
            createDTO.getContent() != null ? createDTO.getContent().length() : 0,
            createDTO.getSummary());
        
        if (createDTO.getContent() == null || createDTO.getContent().trim().isEmpty()) {
            log.warn("⚠️ Controller层检测到内容为空: {}", createDTO);
            return Result.error("文章内容不能为空");
        }
        
        ArticleResponseDTO response = articleService.createArticle(createDTO, currentUserId);
        return Result.success("创建文章成功", response);
    }

    /**
     * 更新文章
     */
    @Operation(summary = "更新知识文章")
    @PutMapping("/{id}")
    public Result<ArticleResponseDTO> updateArticle(
            @Parameter(description = "文章ID") @PathVariable String id,
            @Valid @RequestBody ArticleUpdateDTO updateDTO,
            HttpServletRequest request) {
        
        // 获取当前用户ID
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

        log.info("用户更新知识文章: userId={}, articleId={}", currentUserId, id);
        ArticleResponseDTO response = articleService.updateArticle(id, updateDTO, currentUserId);
        return Result.success("更新文章成功", response);
    }

    /**
     * 删除文章
     */
    @Operation(summary = "删除知识文章")
    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(
            @Parameter(description = "文章ID") @PathVariable String id,
            HttpServletRequest request) {
        
        // 获取当前用户ID
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

        log.info("用户删除知识文章: userId={}, articleId={}", currentUserId, id);
        articleService.deleteArticle(id, currentUserId);
        return Result.success();
    }

    /**
     * 根据ID获取文章详情
     */
    @Operation(summary = "获取知识文章详情")
    @GetMapping("/{id}")
    public Result<ArticleResponseDTO> getArticleById(
            @Parameter(description = "文章ID") @PathVariable String id,
            HttpServletRequest request) {
        
        // 获取当前用户ID（可为空，用于判断收藏状态）
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        
        log.info("获取知识文章详情: articleId={}, userId={}", id, currentUserId);
        ArticleResponseDTO response = articleService.getArticleById(id, currentUserId);
        return Result.success(response);
    }

    /**
     * 阅读文章（增加阅读次数）
     */
    @Operation(summary = "阅读知识文章")
    @PostMapping("/{id}/read")
    public Result<ArticleResponseDTO> readArticle(
            @Parameter(description = "文章ID") @PathVariable String id,
            HttpServletRequest request) {
        
        // 获取当前用户ID（可为空）
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        
        log.info("阅读知识文章: articleId={}, userId={}", id, currentUserId);
        ArticleResponseDTO response = articleService.readArticle(id, currentUserId);
        return Result.success(response);
    }

    /**
     * 发布文章
     */
    @Operation(summary = "发布知识文章")
    @PostMapping("/{id}/publish")
    public Result<ArticleResponseDTO> publishArticle(
            @Parameter(description = "文章ID") @PathVariable String id,
            HttpServletRequest request) {
        
        // 获取当前用户ID
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

        log.info("用户发布知识文章: userId={}, articleId={}", currentUserId, id);
        ArticleResponseDTO response = articleService.publishArticle(id, currentUserId);
        return Result.success("发布文章成功", response);
    }

    /**
     * 下线文章
     */
    @Operation(summary = "下线知识文章")
    @PostMapping("/{id}/offline")
    public Result<ArticleResponseDTO> offlineArticle(
            @Parameter(description = "文章ID") @PathVariable String id,
            HttpServletRequest request) {
        
        // 获取当前用户ID
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

        log.info("用户下线知识文章: userId={}, articleId={}", currentUserId, id);
        ArticleResponseDTO response = articleService.offlineArticle(id, currentUserId);
        return Result.success("下线文章成功", response);
    }

    /**
     * 分页查询文章列表
     */
    @Operation(summary = "分页查询知识文章列表")
    @GetMapping("/page")
    public Result<Page<ArticleSimpleResponseDTO>> getArticlePage(
            @Parameter(description = "关键词搜索（标题+内容+标签）") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "文章标题") @RequestParam(required = false) String title,
            @Parameter(description = "标签") @RequestParam(required = false) String tags,
            @Parameter(description = "作者ID") @RequestParam(required = false) Long authorId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "publishedAt") String sortField,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDirection,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Long currentPage,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Long size,
            HttpServletRequest request) {

        // 获取当前用户ID（可为空，用于判断收藏状态和权限）
        Long currentUserId = JwtTokenUtils.getCurrentUserId();

        ArticleListQueryDTO queryDTO = new ArticleListQueryDTO();
        queryDTO.setKeyword(keyword);
        queryDTO.setCategoryId(categoryId);
        queryDTO.setTitle(title);
        queryDTO.setTags(tags);
        queryDTO.setAuthorId(authorId);
        queryDTO.setStatus(status);
        queryDTO.setStartDate(startDate);
        queryDTO.setEndDate(endDate);
        queryDTO.setSortField(sortField);
        queryDTO.setSortDirection(sortDirection);
        queryDTO.setCurrentPage(currentPage);
        queryDTO.setSize(size);

        log.info("分页查询知识文章列表: keyword={}, page={}, size={}, userId={}", keyword, currentPage, size, currentUserId);
        Page<ArticleSimpleResponseDTO> response = articleService.getArticlePage(queryDTO, currentUserId);
        return Result.success(response);
    }

    /**
     * 更新文章状态
     */
    @Operation(summary = "更新文章状态")
    @PutMapping("/{id}/status")
    public Result<ArticleResponseDTO> updateArticleStatus(
            @Parameter(description = "文章ID") @PathVariable String id,
            @Valid @RequestBody ArticleStatusUpdateDTO statusUpdateDTO,
            HttpServletRequest request) {
        
        // 获取当前用户ID
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

        log.info("用户更新文章状态: userId={}, articleId={}, status={}", currentUserId, id, statusUpdateDTO.getStatus());
        ArticleResponseDTO response = articleService.updateArticleStatus(id, statusUpdateDTO.getStatus(), currentUserId);
        return Result.success("状态更新成功", response);
    }

    /**
     * 批量删除文章
     */
    @Operation(summary = "批量删除文章")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteArticles(
            @Valid @RequestBody ArticleBatchDeleteDTO batchDeleteDTO,
            HttpServletRequest request) {
        
        // 获取当前用户ID
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

        log.info("用户批量删除文章: userId={}, articleIds={}", currentUserId, batchDeleteDTO.getIds());
        articleService.batchDeleteArticles(batchDeleteDTO.getIds(), currentUserId);
        return Result.success();
    }

    /**
     * 获取文章统计信息
     */
    @Operation(summary = "获取文章统计信息")
    @GetMapping("/statistics")
    public Result<ArticleStatisticsResponseDTO> getArticleStatistics(HttpServletRequest request) {
        
        // 获取当前用户ID（用于权限控制）
        Long currentUserId = JwtTokenUtils.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

        log.info("获取文章统计信息: userId={}", currentUserId);
        ArticleStatisticsResponseDTO response = articleService.getArticleStatistics(currentUserId);
        return Result.success(response);
    }
}