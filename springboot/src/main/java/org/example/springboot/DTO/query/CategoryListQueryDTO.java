package org.example.springboot.DTO.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 知识分类列表查询DTO
 * @author system
 */
@Data
@Schema(description = "知识分类列表查询")
public class CategoryListQueryDTO {

    @Schema(description = "分类名称（模糊查询）", example = "心理")
    private String categoryName;

    @Schema(description = "状态", example = "1", allowableValues = {"0", "1"})
    private Integer status;

    @Schema(description = "当前页码", example = "1")
    private Long currentPage = 1L;

    @Schema(description = "每页大小", example = "10")
    private Long size = 10L;
}