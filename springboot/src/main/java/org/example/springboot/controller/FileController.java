package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.DTO.BussinessFileUploadConfig;
import org.example.springboot.DTO.FileInfoDTO;
import org.example.springboot.DTO.FileUploadDTO;
import org.example.springboot.DTO.SimpleFileInfoDTO;
import org.example.springboot.common.Result;
import org.example.springboot.service.FileService;
import org.example.springboot.service.SimpleFileService;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 文件管理控制器
 * 提供简单文件上传和完整业务文件管理功能
 * @author system
 */
@Tag(name = "文件管理", description = "文件上传、下载、删除、信息查询接口")
@RequestMapping("/file")
@RestController
@Slf4j
@Validated
public class FileController {

    @Resource
    private FileService fileService;

    @Resource
    private SimpleFileService simpleFileService;

    // ========== 简单文件上传接口（不保存在数据库） ==========

    @Operation(summary = "简单图片上传", description = "上传图片文件，返回访问路径")
    @PostMapping("/simple/upload/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("收到简单图片上传请求，文件名：{}", file.getOriginalFilename());
        return simpleFileService.uploadImage(file);
    }

    @Operation(summary = "简单文件上传", description = "上传通用类型文件，返回访问路径")
    @PostMapping("/simple/upload")
    public Result<String> uploadSimpleFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "COMMON") String fileType) {
        log.info("收到简单文件上传请求，文件名：{}，类型：{}",
                file.getOriginalFilename(), fileType);
        return simpleFileService.uploadSimpleFile(file, fileType);
    }

    @Operation(summary = "多文件上传", description = "批量上传多个文件，支持部分成功")
    @PostMapping("/simple/upload/multiple")
    public Result<List<String>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "fileType", defaultValue = "COMMON") String fileType) {
        log.info("收到批量文件上传请求，文件数量：{}，类型：{}", files != null ? files.length : 0, fileType);
        return simpleFileService.uploadMultipleFiles(files, fileType);
    }

    @Operation(summary = "简单文件删除", description = "根据文件名删除文件")
    @DeleteMapping("/simple/delete/{filename}")
    public Result<Void> deleteSimpleFile(@PathVariable String filename) {
        log.info("收到简单文件删除请求，文件名：{}", filename);
        return simpleFileService.deleteFile(filename);
    }

    @Operation(summary = "获取文件信息", description = "获取文件的详细信息")
    @GetMapping("/simple/info/{filename}")
    public Result<SimpleFileInfoDTO> getSimpleFileInfo(@PathVariable String filename) {
        log.info("收到文件信息查询请求，文件名：{}", filename);
        return Result.success(simpleFileService.getFileInfo(filename));
    }

    @Operation(summary = "文件下载", description = "下载指定文件")
    @GetMapping("/simple/download/{filename}")
    public Result<String> downloadFile(@PathVariable String filename) {
        log.info("收到文件下载请求，文件名：{}", filename);
        return simpleFileService.getDownloadPath(filename);
    }

    // ========== 完整业务文件管理接口 ==========

    @Operation(summary = "业务文件上传", description = "上传文件并绑定业务对象，可选择是否替换旧文件")
    @PostMapping("/upload")
    public Result<FileInfoDTO> uploadFile(
            @Parameter(description = "上传的文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "业务类型") @RequestParam("businessType") String businessType,
            @Parameter(description = "业务对象ID") @RequestParam("businessId") String businessId,
            @Parameter(description = "业务字段名") @RequestParam(value = "businessField", required = false) String businessField,
            @Parameter(description = "是否替换旧文件") @RequestParam(value = "replaceOld", defaultValue = "false") boolean replaceOld) {

        try {
            Long userId = getCurrentUserId();
            log.info("文件上传请求: 用户ID={}, 文件名={}, 业务类型={}, 业务ID={}, 替换模式={}",
                    userId, file.getOriginalFilename(), businessType, businessId, replaceOld);

            FileUploadDTO uploadDTO = buildFileUploadDTO(businessType, businessId, businessField, false);
            FileInfoDTO result = fileService.uploadFile(file, uploadDTO, userId, replaceOld);

            return Result.success(result);

        } catch (Exception e) {
            log.error("文件上传失败: 文件名={}, 错误={}", file.getOriginalFilename(), e.getMessage(), e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "临时文件上传", description = "上传临时文件，后续可确认转为正式文件")
    @PostMapping("/upload/temp")
    public Result<FileInfoDTO> uploadTempFile(
            @Parameter(description = "上传的文件") @RequestParam("file") MultipartFile file) {

        try {
            Long userId = getCurrentUserId();
            log.info("临时文件上传请求: 用户ID={}, 文件名={}", userId, file.getOriginalFilename());

            FileInfoDTO result = fileService.uploadTempFile(file, userId);
            return Result.success(result);

        } catch (Exception e) {
            log.error("临时文件上传失败: 文件名={}, 错误={}", file.getOriginalFilename(), e.getMessage(), e);
            return Result.error("临时文件上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "临时业务文件上传", description = "上传临时业务文件，业务ID为0，标记为临时文件")
    @PostMapping("/upload/temp-business")
    public Result<FileInfoDTO> uploadTempBusinessFile(
            @Parameter(description = "上传的文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "业务类型") @RequestParam("businessType") String businessType,
            @Parameter(description = "业务字段名") @RequestParam(value = "businessField", required = false) String businessField) {

        try {
            Long userId = getCurrentUserId();
            log.info("临时业务文件上传请求: 用户ID={}, 文件名={}, 业务类型={}, 业务字段={}",
                    userId, file.getOriginalFilename(), businessType, businessField);

            FileUploadDTO uploadDTO = buildFileUploadDTO(businessType, "0", businessField, true);
            FileInfoDTO result = fileService.uploadFile(file, uploadDTO, userId, false);
            return Result.success(result);

        } catch (Exception e) {
            log.error("临时业务文件上传失败: 文件名={}, 错误={}", file.getOriginalFilename(), e.getMessage(), e);
            return Result.error("临时业务文件上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "确认临时文件", description = "将临时文件确认为正式文件并绑定业务对象")
    @PutMapping("/confirm/{tempFileId}")
    public Result<FileInfoDTO> confirmTempFile(
            @Parameter(description = "临时文件ID") @PathVariable Long tempFileId,
            @Parameter(description = "文件上传信息") @Valid @RequestBody FileUploadDTO uploadDTO) {

        try {
            log.info("确认临时文件请求: 文件ID={}, 业务类型={}, 业务ID={}",
                    tempFileId, uploadDTO.getBusinessType(), uploadDTO.getBusinessId());

            FileInfoDTO result = fileService.confirmTempFile(tempFileId, uploadDTO);
            return Result.success(result);

        } catch (Exception e) {
            log.error("确认临时文件失败: 文件ID={}, 错误={}", tempFileId, e.getMessage(), e);
            return Result.error("确认临时文件失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取业务文件列表", description = "根据业务类型和业务ID获取文件列表")
    @GetMapping("/business/{businessType}/{businessId}")
    public Result<List<FileInfoDTO>> getFilesByBusiness(
            @Parameter(description = "业务类型") @PathVariable String businessType,
            @Parameter(description = "业务对象ID") @PathVariable String businessId) {

        try {
            log.info("查询业务文件列表: 业务类型={}, 业务ID={}", businessType, businessId);
            List<FileInfoDTO> fileList = fileService.getFilesByBusiness(businessType, businessId);
            return Result.success(fileList);

        } catch (Exception e) {
            log.error("查询业务文件列表失败: 业务类型={}, 业务ID={}, 错误={}",
                    businessType, businessId, e.getMessage(), e);
            return Result.error("查询文件列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取业务字段文件", description = "根据业务类型、业务ID和字段名获取文件")
    @GetMapping("/business/{businessType}/{businessId}/{businessField}")
    public Result<List<FileInfoDTO>> getFilesByBusinessField(
            @Parameter(description = "业务类型") @PathVariable String businessType,
            @Parameter(description = "业务对象ID") @PathVariable Long businessId,
            @Parameter(description = "业务字段名") @PathVariable String businessField) {

        try {
            log.info("查询业务字段文件: 业务类型={}, 业务ID={}, 字段={}",
                    businessType, businessId, businessField);

            List<FileInfoDTO> fileList = fileService.getFilesByBusinessField(
                    businessType, businessId, businessField);
            return Result.success(fileList);

        } catch (Exception e) {
            log.error("查询业务字段文件失败: 业务类型={}, 业务ID={}, 字段={}, 错误={}",
                    businessType, businessId, businessField, e.getMessage(), e);
            return Result.error("查询文件失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除业务文件", description = "删除指定业务文件")
    @DeleteMapping("/{fileId}")
    public Result<Boolean> deleteFile(
            @Parameter(description = "文件ID") @PathVariable Long fileId) {

        try {
            Long userId = JwtTokenUtils.getCurrentUserId();
            log.info("删除文件请求: 用户ID={}, 文件ID={}", userId, fileId);

            boolean result = fileService.deleteFile(fileId, userId);
            return Result.success(result);

        } catch (Exception e) {
            log.error("删除文件失败: 文件ID={}, 错误={}", fileId, e.getMessage(), e);
            return Result.error("删除文件失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量删除业务文件", description = "根据业务信息批量删除文件")
    @DeleteMapping("/business/{businessType}/{businessId}")
    public Result<Boolean> deleteFilesByBusiness(
            @Parameter(description = "业务类型") @PathVariable String businessType,
            @Parameter(description = "业务对象ID") @PathVariable Long businessId,
            @Parameter(description = "业务字段名") @RequestParam(value = "businessField", required = false) String businessField) {

        try {
            log.info("批量删除业务文件请求: 业务类型={}, 业务ID={}, 字段={}",
                    businessType, businessId, businessField);

            boolean result = fileService.deleteFilesByBusiness(businessType, businessId, businessField);
            return Result.success(result);

        } catch (Exception e) {
            log.error("批量删除业务文件失败: 业务类型={}, 业务ID={}, 错误={}",
                    businessType, businessId, e.getMessage(), e);
            return Result.error("批量删除文件失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取文件上传配置", description = "获取指定业务类型的文件上传配置信息")
    @GetMapping("/upload/config")
    public Result<BussinessFileUploadConfig> getUploadConfig(
            @Parameter(description = "业务类型") @RequestParam String businessType) {

        try {
            log.info("获取文件上传配置: 业务类型={}", businessType);
            BussinessFileUploadConfig bussinessFileUploadConfig = fileService.getUploadConfig(businessType);
            return Result.success(bussinessFileUploadConfig);

        } catch (Exception e) {
            log.error("获取文件上传配置失败: 业务类型={}, 错误={}", businessType, e.getMessage(), e);
            return Result.error("获取配置失败: " + e.getMessage());
        }
    }

    @Operation(summary = "清理过期临时文件", description = "系统管理接口：清理过期的临时文件")
    @PostMapping("/cleanup/temp")
    public Result<Integer> cleanupExpiredTempFiles() {

        try {
            log.info("清理过期临时文件请求");
            int cleanupCount = fileService.cleanupExpiredTempFiles();
            return Result.success(cleanupCount);

        } catch (Exception e) {
            log.error("清理过期临时文件失败: 错误={}", e.getMessage(), e);
            return Result.error("清理失败: " + e.getMessage());
        }
    }

    // ========== 私有方法 ==========

    /**
     * 获取当前用户ID，如果获取失败则使用默认测试用户ID
     */
    private Long getCurrentUserId() {
        try {
            return JwtTokenUtils.getCurrentUserId();
        } catch (Exception e) {
            Long defaultUserId = 1L;
            log.warn("获取用户ID失败，使用默认测试用户ID: {}", defaultUserId);
            return defaultUserId;
        }
    }

    /**
     * 构建文件上传DTO
     */
    private FileUploadDTO buildFileUploadDTO(String businessType, String businessId, String businessField, Boolean isTemp) {
        FileUploadDTO uploadDTO = new FileUploadDTO();
        uploadDTO.setBusinessType(businessType);
        uploadDTO.setBusinessId(businessId);
        uploadDTO.setBusinessField(businessField);
        uploadDTO.setIsTemp(isTemp);
        return uploadDTO;
    }
}