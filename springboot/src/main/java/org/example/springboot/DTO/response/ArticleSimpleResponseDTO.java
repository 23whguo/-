package org.example.springboot.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文章简化响应DTO（用于列表展示）
 * @author system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文章简化响应")
public class ArticleSimpleResponseDTO {

    @Schema(description = "文章ID")
    private String id;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章摘要")
    private String summary;

    @Schema(description = "封面图片")
    private String coverImage;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "作者名称")
    private String authorName;

    @Schema(description = "阅读次数")
    private Integer readCount;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusText;

    @Schema(description = "是否收藏")
    private Boolean isFavorited;

    @Schema(description = "收藏数量")
    private Integer favoriteCount;

    @Schema(description = "发布时间")
    private LocalDateTime publishedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "收藏时间")
    private LocalDateTime favoriteTime;
}