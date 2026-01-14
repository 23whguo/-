package org.example.springboot.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章统计信息响应DTO
 * @author system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文章统计信息响应")
public class ArticleStatisticsResponseDTO {

    @Schema(description = "文章总数")
    private Long totalArticles;

    @Schema(description = "已发布文章数")
    private Long publishedArticles;

    @Schema(description = "草稿文章数")
    private Long draftArticles;

    @Schema(description = "已下线文章数")
    private Long offlineArticles;

    @Schema(description = "总阅读量")
    private Long totalViews;

    @Schema(description = "总收藏数")
    private Long totalFavorites;
}