package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "简单文件信息")
public class SimpleFileInfoDTO {
    @Schema(description = "文件名")
    private String filename;
    @Schema(description = "文件大小")
    private long size;
    @Schema(description = "上次修改")
    private long lastModified;
    @Schema(description = "文件路径")
    private String path;

}
