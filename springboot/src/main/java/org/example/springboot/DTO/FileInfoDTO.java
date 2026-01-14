package org.example.springboot.DTO;

import cn.hutool.core.date.DateUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件信息响应DTO
 * @author system
 */
@Data
@Schema(description = "文件信息响应DTO")
public class FileInfoDTO {

    @Schema(description = "文件ID")
    private Long id;

    @Schema(description = "原始文件名")
    private String originalName;

    @Schema(description = "文件访问路径")
    private String filePath;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "文件类型描述")
    private String fileTypeDesc;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "业务类型描述")
    private String businessTypeDesc;

    @Schema(description = "业务对象ID")
    private String businessId;

    @Schema(description = "业务字段名")
    private String businessField;

    @Schema(description = "上传用户ID")
    private Long uploadUserId;

    @Schema(description = "是否临时文件")
    private Boolean isTemp;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "过期时间")
    private String expireTime;

    @Schema(description = "文件扩展名")
    private String fileExtension;

    @Schema(description = "是否已过期")
    private Boolean isExpired;

    /**
     * 设置创建时间（将LocalDateTime转换为字符串）
     */
    public void setCreateTime(LocalDateTime createTime) {
        if (createTime != null) {
            this.createTime = DateUtil.formatLocalDateTime(createTime);
        }
    }

    /**
     * 设置过期时间（将LocalDateTime转换为字符串）
     */
    public void setExpireTime(LocalDateTime expireTime) {
        if (expireTime != null) {
            this.expireTime = DateUtil.formatLocalDateTime(expireTime);
        }
    }
} 