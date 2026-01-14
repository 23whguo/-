package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@Schema(description = "业务文件配置DTO")
@Data
@AllArgsConstructor
public class BussinessFileUploadConfig {
  @Schema(description = "业务文件类型")
  String  businessType;
  @Schema(description = "业务文件描述")
  String  businessDesc;
  @Schema(description = "允许的类型")
  List<String> allowedTypes;
  @Schema(description = "文件最大大小")
  Long  maxFileSize;
  @Schema(description = "允许的扩展")
  List<String> allowedExtensions;
}
