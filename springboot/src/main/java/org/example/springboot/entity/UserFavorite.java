package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户收藏实体类
 * @author system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_favorite")
@Schema(description = "用户收藏实体类")
public class UserFavorite {

    @TableId(type = IdType.AUTO)
    @Schema(description = "收藏ID")
    private Long id;

    @Schema(description = "用户ID")
    @NotNull(message = "用户ID不能为空")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "文章ID")
    @NotNull(message = "文章ID不能为空")
    @TableField("article_id")
    private String articleId;

    @Schema(description = "收藏时间")
    @TableField("created_at")
    private LocalDateTime createdAt;
}