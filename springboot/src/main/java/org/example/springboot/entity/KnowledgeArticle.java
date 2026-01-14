package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识文章实体类
 * @author system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("knowledge_article")
@Schema(description = "知识文章实体类")
public class KnowledgeArticle {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "文章ID")
    private String id;

    @Schema(description = "分类ID")
    @NotNull(message = "分类ID不能为空")
    @TableField("category_id")
    private Long categoryId;

    @Schema(description = "文章标题")
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "文章标题长度不能超过200个字符")
    private String title;

    @Schema(description = "文章摘要")
    @Size(max = 1000, message = "文章摘要长度不能超过1000个字符")
    private String summary;

    @Schema(description = "文章内容")
    @NotBlank(message = "文章内容不能为空")
    private String content;

    @Schema(description = "封面图片")
    @Size(max = 500, message = "封面图片URL长度不能超过500个字符")
    @TableField("cover_image")
    private String coverImage;

    @Schema(description = "标签（多个标签用逗号分隔）")
    @Size(max = 500, message = "标签长度不能超过500个字符")
    private String tags;

    @Schema(description = "作者ID")
    @TableField("author_id")
    private Long authorId;

    @Schema(description = "阅读次数")
    @TableField("read_count")
    private Integer readCount;

    @Schema(description = "状态 0:草稿 1:已发布 2:已下线")
    private Integer status;

    @Schema(description = "发布时间")
    @TableField("published_at")
    private LocalDateTime publishedAt;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 是否已发布
     */
    public boolean isPublished() {
        return status != null && status.equals(1);
    }

    /**
     * 是否为草稿
     */
    public boolean isDraft() {
        return status != null && status.equals(0);
    }

    /**
     * 是否已下线
     */
    public boolean isOffline() {
        return status != null && status.equals(2);
    }

    /**
     * 增加阅读次数
     */
    public void incrementReadCount() {
        this.readCount = (this.readCount == null ? 0 : this.readCount) + 1;
    }

    /**
     * 获取标签数组
     */
    public String[] getTagArray() {
        if (tags == null || tags.trim().isEmpty()) {
            return new String[0];
        }
        return tags.split(",");
    }

    /**
     * 设置标签数组
     */
    public void setTagArray(String[] tagArray) {
        if (tagArray == null || tagArray.length == 0) {
            this.tags = null;
        } else {
            this.tags = String.join(",", tagArray);
        }
    }

    /**
     * 获取文章摘要（自动生成）
     */
    public String getAutoSummary() {
        if (summary != null && !summary.trim().isEmpty()) {
            return summary;
        }
        if (content != null && content.length() > 100) {
            // 移除HTML标签，取前100个字符作为摘要
            String plainText = content.replaceAll("<[^>]+>", "");
            return plainText.length() > 100 ? plainText.substring(0, 100) + "..." : plainText;
        }
        return content;
    }
}