package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import cn.hutool.core.io.FileUtil;

import java.time.LocalDateTime;

/**
 * 文件信息实体类
 * @author system
 */
@Data
@TableName("sys_file_info")
@Schema(description = "文件信息实体类")
public class SysFileInfo {

    @TableId(type = IdType.AUTO)
    @Schema(description = "文件ID")
    private Long id;

    @Schema(description = "原始文件名")
    private String originalName;

    @Schema(description = "文件访问路径")
    private String filePath;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "文件类型(IMG/PDF/TXT等)")
    private String fileType;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "业务对象ID（支持数字ID和UUID格式）")
    private String businessId;

    @Schema(description = "业务字段名")
    private String businessField;

    @Schema(description = "上传用户ID")
    private Long uploadUserId;

    @Schema(description = "是否临时文件(0:否 1:是)")
    private Integer isTemp;

    @Schema(description = "状态(0:删除 1:正常)")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "过期时间(临时文件)")
    private LocalDateTime expireTime;

    /**
     * 获取文件名（从文件路径中提取）
     */
    public String getFileName() {
        if (this.filePath == null) {
            return null;
        }
        return this.filePath.substring(this.filePath.lastIndexOf('/') + 1);
    }

    /**
     * 获取文件扩展名（从原始文件名中提取）
     */
    public String getFileExtension() {
        if (this.originalName == null) {
            return null;
        }
        return FileUtil.extName(this.originalName);
    }

    /**
     * 是否为临时文件
     */
    public boolean isTempFile() {
        return this.isTemp != null && this.isTemp == 1;
    }

    /**
     * 是否为正常状态
     */
    public boolean isNormalStatus() {
        return this.status != null && this.status == 1;
    }

    /**
     * 是否已过期（仅针对临时文件）
     */
    public boolean isExpired() {
        if (!isTempFile() || this.expireTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.expireTime);
    }
} 