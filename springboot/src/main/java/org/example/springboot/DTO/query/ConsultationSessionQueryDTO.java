package org.example.springboot.DTO.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 咨询会话查询DTO
 * @author system
 */
@Data
@Schema(description = "咨询会话查询DTO")
public class ConsultationSessionQueryDTO {

    @Schema(description = "当前页", example = "1")
    private Integer currentPage = 1;

    @Schema(description = "每页条数", example = "10")
    private Integer size = 10;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "情绪标签")
    private String emotionTag;

    @Schema(description = "开始时间（起）格式：yyyy-MM-dd")
    private String startDate;

    @Schema(description = "开始时间（止）格式：yyyy-MM-dd")
    private String endDate;

    @Schema(description = "关键词搜索（会话标题或消息内容）")
    private String keyword;
}
