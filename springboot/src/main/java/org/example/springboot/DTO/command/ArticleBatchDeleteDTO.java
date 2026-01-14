package org.example.springboot.DTO.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 文章批量删除命令DTO
 * @author system
 */
@Data
@Schema(description = "文章批量删除命令")
public class ArticleBatchDeleteDTO {

    @Schema(description = "要删除的文章ID列表", example = "[\"uuid1\", \"uuid2\", \"uuid3\"]")
    @NotEmpty(message = "删除的文章ID列表不能为空")
    private List<String> ids;
}