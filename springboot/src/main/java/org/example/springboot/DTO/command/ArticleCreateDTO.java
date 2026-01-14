package org.example.springboot.DTO.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 知识文章创建命令DTO
 * @author system
 */
@Data
@Schema(description = "知识文章创建命令")
public class ArticleCreateDTO {

    @Schema(description = "文章ID（可选，支持UUID预生成）", example = "550e8400-e29b-41d4-a716-446655440000")
    @Size(max = 50, message = "文章ID长度不能超过50个字符")
    private String id;

    @Schema(description = "分类ID", example = "1")
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @Schema(description = "文章标题", example = "如何管理焦虑情绪")
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "文章标题长度不能超过200个字符")
    private String title;

    @Schema(description = "文章摘要", example = "本文介绍了管理焦虑情绪的有效方法和技巧")
    @Size(max = 1000, message = "文章摘要长度不能超过1000个字符")
    private String summary;

    @Schema(description = "文章内容", example = "焦虑是一种常见的情绪反应...")
    @NotBlank(message = "文章内容不能为空")
    private String content;

    @Schema(description = "封面图片URL", example = "https://example.com/cover.jpg")
    @Size(max = 500, message = "封面图片URL长度不能超过500个字符")
    private String coverImage;

    @Schema(description = "标签（多个标签用逗号分隔）", example = "焦虑,情绪管理,心理健康")
    @Size(max = 500, message = "标签长度不能超过500个字符")
    private String tags;

    @Schema(description = "状态", example = "0", allowableValues = {"0", "1", "2"})
    private Integer status = 0; // 默认为草稿状态
}