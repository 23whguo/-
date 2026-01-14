package org.example.springboot.DTO.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文章状态更新命令DTO
 * @author system
 */
@Data
@Schema(description = "文章状态更新命令")
public class ArticleStatusUpdateDTO {

    @Schema(description = "文章状态", example = "1", allowableValues = {"0", "1", "2"})
    @NotNull(message = "文章状态不能为空")
    private Integer status;
}