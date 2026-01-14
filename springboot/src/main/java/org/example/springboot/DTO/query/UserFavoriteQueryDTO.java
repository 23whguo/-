package org.example.springboot.DTO.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户收藏文章查询DTO
 * @author system
 */
@Data
@Schema(description = "用户收藏文章查询")
public class UserFavoriteQueryDTO {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "文章标题（模糊查询）", example = "焦虑")
    private String title;

    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "排序字段", example = "createdAt", allowableValues = {"createdAt", "title"})
    private String sortField = "createdAt";

    @Schema(description = "排序方向", example = "desc", allowableValues = {"asc", "desc"})
    private String sortDirection = "desc";

    @Schema(description = "当前页码", example = "1")
    private Long currentPage = 1L;

    @Schema(description = "每页大小", example = "10")
    private Long size = 10L;
}