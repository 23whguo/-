package org.example.springboot.DTO.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 知识文章列表查询DTO
 * @author system
 */
@Data
@Schema(description = "知识文章列表查询")
public class ArticleListQueryDTO {

    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "关键词搜索（标题+内容+标签）", example = "焦虑")
    private String keyword;

    @Schema(description = "文章标题（模糊查询）", example = "焦虑")
    private String title;

    @Schema(description = "标签（模糊查询）", example = "情绪管理")
    private String tags;

    @Schema(description = "作者ID", example = "1")
    private Long authorId;

    @Schema(description = "状态", example = "1", allowableValues = {"0", "1", "2"})
    private Integer status;

    @Schema(description = "开始日期（yyyy-MM-dd）", example = "2024-01-01")
    private String startDate;

    @Schema(description = "结束日期（yyyy-MM-dd）", example = "2024-12-31")
    private String endDate;

    @Schema(description = "排序字段", example = "publishedAt", allowableValues = {"publishedAt", "readCount", "createdAt"})
    private String sortField = "publishedAt";

    @Schema(description = "排序方向", example = "desc", allowableValues = {"asc", "desc"})
    private String sortDirection = "desc";

    @Schema(description = "当前页码", example = "1")
    private Long currentPage = 1L;

    @Schema(description = "每页大小", example = "10")
    private Long size = 10L;
}