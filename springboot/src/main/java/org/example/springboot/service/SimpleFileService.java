package org.example.springboot.service;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.DTO.FileInfoDTO;
import org.example.springboot.DTO.SimpleFileInfoDTO;
import org.example.springboot.common.Result;
import org.example.springboot.common.ResultCode;
import org.example.springboot.enumClass.FileTypeEnum;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 简单文件上传服务
 * 重构后统一使用FileValidationService进行验证，避免重复代码
 * 
 * @author system
 */
@Slf4j
@Service
public class SimpleFileService {



    /**
     * 简单图片上传
     */
    public Result<String> uploadImage(MultipartFile file) {
        log.info("开始图片上传：{}", file.getOriginalFilename());
        return uploadSimpleFile(file, "IMG");
    }

    /**
     * 简单文件上传
     */
    public Result<String> uploadSimpleFile(MultipartFile file, String fileType) {
        try {
            log.info("开始文件上传：{}，类型：{}", file.getOriginalFilename(), fileType);

            // 解析目标目录
            String relativeDir = FileUtil.parseFileTypeToRelativeDir(fileType);
            
            // 使用FileUtil保存文件（自动生成唯一文件名）
            String filePath = FileUtil.saveFile(file, relativeDir, null);
            if (filePath == null) {
                return Result.error(ResultCode.ERROR.code(), "文件保存失败");
            }

            log.info("文件上传成功：{}", filePath);
            return Result.success(filePath);

        } catch (Exception e) {
            log.error("文件上传异常：{}，错误：{}", file.getOriginalFilename(), e.getMessage(), e);
            return Result.error(ResultCode.ERROR.code(), "文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 批量文件上传
     */
    public Result<List<String>> uploadMultipleFiles(MultipartFile[] files, String fileType) {
        if (files == null || files.length == 0) {
            return Result.error(ResultCode.PARAM_ERROR.code(), "未选择文件");
        }

        List<String> uploadedFiles = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            Result<String> result = uploadSimpleFile(file, fileType);
            if (result.isSuccess()) {
                uploadedFiles.add(result.getData());
            } else {
                failedFiles.add(file.getOriginalFilename() + ": " + result.getMessage());
                log.error("文件上传失败：{}，原因：{}", file.getOriginalFilename(), result.getMessage());
            }
        }

        if (uploadedFiles.isEmpty()) {
            return Result.error(ResultCode.ERROR.code(), "所有文件上传失败：" + String.join("; ", failedFiles));
        } else if (!failedFiles.isEmpty()) {
            log.warn("部分文件上传失败：{}", String.join("; ", failedFiles));
        }

        log.info("批量文件上传完成，成功：{}个，失败：{}个", uploadedFiles.size(), failedFiles.size());
        return Result.success(uploadedFiles);
    }

    /**
     * 删除文件
     */
    public Result<Void> deleteFile(String filename) {
        try {
            log.info("开始删除文件：{}", filename);

            // 使用FileUtil统一删除
            boolean success = FileUtil.deleteFile(filename);
            if (!success) {
                return Result.error(ResultCode.ERROR.code(), "文件删除失败");
            }

            log.info("文件删除成功：{}", filename);
            return Result.success();

        } catch (Exception e) {
            log.error("文件删除异常：{}，错误：{}", filename, e.getMessage(), e);
            return Result.error(ResultCode.ERROR.code(), "文件删除失败：" + e.getMessage());
        }
    }

    /**
     *
     * @param filename 文件名
     * @return 简单文件信息
     */
    public SimpleFileInfoDTO getFileInfo(String filename) {
        try {
            log.info("获取文件信息：{}", filename);

            if (StrUtil.isBlank(filename)) {
                throw new ServiceException("文件名不能为空");

            }

            // 使用FileUtil检查文件存在性
            if (!FileUtil.fileExists(filename)) {
                throw new ServiceException("文件不存在");
            }

            SimpleFileInfoDTO fileInfo = new SimpleFileInfoDTO();
            fileInfo.setFilename(filename);
            fileInfo.setPath(filename);
            fileInfo.setSize(FileUtil.getFileSize(filename));

            // 获取文件修改时间
            try {
                // 转换为相对物理路径
                String relativePath = FileUtil.convertToRelativePath(filename);
                Path filePath = Paths.get(FileUtil.FILE_BASE_PATH, relativePath);
                if (Files.exists(filePath)) {
                    fileInfo.setLastModified(Files.getLastModifiedTime(filePath).toMillis());
                }
            } catch (Exception e) {
                log.warn("获取文件修改时间失败：{}", e.getMessage());
                fileInfo.setLastModified(System.currentTimeMillis());
            }

            return fileInfo;

        } catch (Exception e) {
            log.error("获取文件信息异常：{}，错误：{}", filename, e.getMessage(), e);
            throw new ServiceException("获取文件信息异常："+filename);
        }
    }

    /**
     * 获取文件下载路径
     */
    public Result<String> getDownloadPath(String filename) {
        try {
            if (StrUtil.isBlank(filename)) {
                return Result.error(ResultCode.PARAM_ERROR.code(), "文件名不能为空");
            }

            if (!FileUtil.fileExists(filename)) {
                return Result.error(ResultCode.ERROR.code(), "文件不存在");
            }

            // 返回文件访问路径
            String downloadPath = filename.startsWith("/") ? filename : "/" + filename;
            return Result.success(downloadPath);

        } catch (Exception e) {
            log.error("获取文件下载路径异常：{}，错误：{}", filename, e.getMessage(), e);
            return Result.error(ResultCode.ERROR.code(), "获取文件下载路径失败：" + e.getMessage());
        }
    }




} 