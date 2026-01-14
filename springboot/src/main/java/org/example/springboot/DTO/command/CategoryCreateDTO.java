package org.example.springboot.DTO.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 知识分类创建命令DTO
 * @author system
 */
@Data
@Schema(description = "知识分类创建命令")
public class CategoryCreateDTO {

    @Schema(description = "分类名称", example = "心理健康基础")
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称长度不能超过100个字符")
    private String categoryName;

    @Schema(description = "分类描述", example = "心理健康基础知识和概念")
    @Size(max = 500, message = "分类描述长度不能超过500个字符")
    private String description;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder = 0;

    @Schema(description = "状态", example = "1", allowableValues = {"0", "1"})
    private Integer status = 1;
}