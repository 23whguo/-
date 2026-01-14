package org.example.springboot.util;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.Data;
import lombok.Getter;
import org.example.springboot.common.Result;
import org.example.springboot.common.ResultCode;
import org.example.springboot.enumClass.FileBusinessTypeEnum;
import org.example.springboot.enumClass.FileTypeEnum;
import org.example.springboot.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件操作工具类
 * 专注于文件的基础操作：保存、删除、检查等
 * 文件类型验证统一使用FileTypeEnum
 * 
 * @author system
 */

public class FileUtil {
    private final static Logger log = LoggerFactory.getLogger(FileUtil.class);
    public final static String FILE_BASE_PATH = System.getProperty("user.dir") + "/files/";
    private static final String ROOT_PATH = "/files/";
    
    /**
     * 将访问路径转换为相对物理路径
     * @param filename 访问路径，可能包含/files/前缀
     * @return 相对物理路径，不包含前缀
     */
    public static String convertToRelativePath(String filename) {
        if (StrUtil.isBlank(filename)) {
            return filename;
        }
        
        // 如果包含ROOT_PATH前缀，去掉它，避免路径重复
        if (filename.startsWith(ROOT_PATH)) {
            return filename.substring(ROOT_PATH.length());
        }
        // 如果路径有前导斜杠，移除它
        else if (filename.startsWith("/")) {
            return filename.substring(1);
        }
        
        return filename;
    }

    /**
     * -- GETTER --
     *  获取最大文件大小
     */
    @Getter
    @Value("${file.upload.maxSize:10485760}")
    private static final long maxFileSize = 10245760; // 10MB


    /**
     * 安全的文件保存方法
     * 统一使用FileTypeEnum进行文件类型验证
     * 
     * @param file 上传的文件
     * @param folderName 子目录名称,位于relativeDir（可选）
     * @param relativeDir 基础目录（应与FileTypeEnum的code对应）
     * @return 文件访问路径，失败返回null
     */
    public static String saveFile(MultipartFile file,  String relativeDir,String folderName) {
        try {
            log.info("开始保存文件，原始文件名：{}，目标目录：{}", file.getOriginalFilename(), relativeDir);

          validateBasicFile(file);
          String originalFilename = file.getOriginalFilename();
            


            // 获取文件扩展名
            String extension = getFileExtension(originalFilename);
            if (StrUtil.isBlank(extension)) {
                log.error("文件没有扩展名：{}", originalFilename);
                return null;
            }
            
            // 生成唯一文件名
            long timestamp = System.currentTimeMillis();
            String uniqueFilename = timestamp + extension.toLowerCase();

            // 构造安全的保存路径
            Path fileDirectory = buildSafeFilePath(relativeDir, folderName);
            if (fileDirectory == null) {
                return null;
            }

            // 创建目录
            if (!Files.exists(fileDirectory)) {
                Files.createDirectories(fileDirectory);
                log.info("创建目录：{}", fileDirectory);
            }
            
            // 保存文件
            Path uploadFilePath = fileDirectory.resolve(uniqueFilename);
            File uploadFile = uploadFilePath.toFile();

            file.transferTo(uploadFile);
            log.info("文件保存成功：{}", uploadFile.getAbsolutePath());
            
            // 返回相对路径
            String relativePath = ROOT_PATH + relativeDir + "/" +
                (StrUtil.isNotBlank(folderName) ? folderName + "/" : "") + uniqueFilename;
            
            log.info("返回文件访问路径：{}", relativePath);
            return relativePath;
            
        } catch (IOException e) {
            log.error("文件保存异常，文件名：{}，错误：{}", file.getOriginalFilename(), e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("文件保存时发生未知异常，文件名：{}，错误：{}", file.getOriginalFilename(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 保存图片的便捷方法
     */
    public static String saveImage(MultipartFile file, String folderName) {
        return saveFile(file, folderName, "img");
    }

    /**
     * 保存视频的便捷方法
     */
    public static String saveVideo(MultipartFile file, String folderName) {
        return saveFile(file, folderName, "video");
    }
    
    /**
     * 安全的文件删除方法
     */
    public static boolean deleteFile(String filename) {
        try {
            log.info("开始删除文件：{}", filename);

            validateName(filename);

            // 转换为相对物理路径
            filename = convertToRelativePath(filename);
            
            // 获取文件的绝对路径
            Path filePath = Paths.get(FILE_BASE_PATH, filename);
            
            // 验证文件路径是否在允许的目录内
            Path basePath = Paths.get(FILE_BASE_PATH);
            if (!filePath.toAbsolutePath().startsWith(basePath.toAbsolutePath())) {
                log.error("文件路径超出允许范围：{}", filePath);
                return false;
            }
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("文件删除成功：{}", filePath);
                return true;
            } else {
                log.warn("文件不存在：{}", filePath);
                return false;
            }
            
        } catch (Exception e) {
            log.error("文件删除异常，文件名：{}，错误：{}", filename, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 写入文件内容
     */
    public static void writeToFile(String fileName, String content) throws IOException {
        log.info("开始写入文件：{}", fileName);



        if (StrUtil.isBlank(content)) {
            throw new IllegalArgumentException("内容不能为空");
        }

        //安全性检查
        validateName(fileName);

        // 创建文件对象
        File file = new File(fileName);
        log.info("文件绝对路径：{}", file.getAbsolutePath());

        // 使用 try-with-resources 确保 FileWriter 在使用完毕后自动关闭
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(content);
            log.info("文件写入成功：{}", fileName);
        } catch (IOException e) {
            log.error("文件写入失败，文件名：{}，错误：{}", fileName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 检查文件是否存在
     */
    public static boolean fileExists(String filename) {
        if (StrUtil.isBlank(filename)) {
            return false;
        }
        
        try {
            // 转换为相对物理路径
            filename = convertToRelativePath(filename);
            
            Path filePath = Paths.get(FILE_BASE_PATH, filename);
            return Files.exists(filePath);
        } catch (Exception e) {
            log.error("检查文件存在性异常，文件名：{}，错误：{}", filename, e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取文件大小
     */
    public static long getFileSize(String filename) {
        if (StrUtil.isBlank(filename)) {
            return -1;
        }
        
        try {
            // 转换为相对物理路径
            filename = convertToRelativePath(filename);
            
            Path filePath = Paths.get(FILE_BASE_PATH, filename);
            if (Files.exists(filePath)) {
                return Files.size(filePath);
            }
            return -1;
        } catch (Exception e) {
            log.error("获取文件大小异常，文件名：{}，错误：{}", filename, e.getMessage());
            return -1;
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String filename) {
        if (StrUtil.isBlank(filename)) {
            return "";
        }
        
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex);
        }
        return "";
    }



    /**
     * 构建安全的文件路径
     */
    private static Path buildSafeFilePath(String relativeDir, String folderName) {
        try {
            Path projectRootPath = Paths.get(FILE_BASE_PATH);

            String  filePath=relativeDir;
            // 如果folderName不为null，则在指定目录后面加入folderName
            if (StringUtils.isNotBlank(folderName)) {
                // 验证folderName的安全性
                validateName(folderName);
                filePath=relativeDir + File.separator + folderName;

            }



            return projectRootPath.resolve(filePath);
        } catch (Exception e) {
            log.error("构建文件路径失败，baseDir：{}，folderName：{}，错误：{}", relativeDir, folderName, e.getMessage());
            return null;
        }
    }



    public static void validateBasicFile(MultipartFile file) {
        //验证是否为空
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        //文件大小验证
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(String.format(
                    "文件大小超出限制，当前: %d 字节，最大允许: %d 字节",
                    file.getSize(), maxFileSize
            ));
        }
        //文件名安全性检查
        String originalName = file.getOriginalFilename();
        validateName(originalName);

    }

    /**
     * 文件名安全性与非空性检查
     * @param name 文件（夹）名
     */
   public static void validateName(String name){
       //验证文件名是否为空
       if (!StringUtils.isNotBlank(name)) {
           throw new BusinessException("文件（夹）名不能为空");
       }

       // 检查危险字符
        String[] dangerousChars = {"..",  "\\", ":", "*", "?", "\"", "<", ">", "|"};
        for (String dangerousChar : dangerousChars) {
            if (name.contains(dangerousChar)) {
                throw new BusinessException("文件（夹）名包含非法字符: " + dangerousChar);
            }
        }
    }


    /**
     * 解析文件类型为目录名(小写)
     */
    public static String parseFileTypeToRelativeDir(String fileType) {
        if (StrUtil.isBlank(fileType)) {
            return FileTypeEnum.OTHER.getCode();
        }
        if(!(FileTypeEnum.isAllowType(fileType))) {
            return "commom";
        }
        return fileType.toLowerCase();
    }

    /**
     * 解析业务文件类型为目录名
     */
    public static String parseBussinessFileTypeToFolerName(String bussinessType) {
        if (StrUtil.isBlank(bussinessType)) {
            return FileTypeEnum.OTHER.getCode();
        }
        if(!FileBusinessTypeEnum.isAllowedFileBussinessType(bussinessType)) {
            return "commom";
        }
        return bussinessType.toLowerCase();
    }

    /**
     * 构建完整的文件路径（用于检查文件是否存在）
     * @param originalFilename 原始文件名
     * @param relativeDir 相对目录
     * @param folderName 子目录名称（可选）
     * @return 完整的文件路径（相对于FILE_BASE_PATH）
     */
    public static String buildFullFilePath(String originalFilename, String relativeDir, String folderName) {
        if (StrUtil.isBlank(originalFilename)) {
            return null;
        }
        
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append("files/").append(relativeDir).append("/");
        
        if (StrUtil.isNotBlank(folderName)) {
            pathBuilder.append(folderName).append("/");
        }
        
        pathBuilder.append(originalFilename);
        
        return pathBuilder.toString();
    }

    /**
     * 构建带时间戳的唯一文件路径
     * @param originalFilename 原始文件名
     * @param relativeDir 相对目录
     * @param folderName 子目录名称（可选）
     * @return 带时间戳的文件路径
     */
    public static String buildUniqueFilePath(String originalFilename, String relativeDir, String folderName) {
        if (StrUtil.isBlank(originalFilename)) {
            return null;
        }
        
        // 获取文件扩展名
        String extension = getFileExtension(originalFilename);
        if (StrUtil.isBlank(extension)) {
            return null;
        }
        
        // 生成唯一文件名
        long timestamp = System.currentTimeMillis();
        String uniqueFilename = timestamp + extension.toLowerCase();
        
        return buildFullFilePath(uniqueFilename, relativeDir, folderName);
    }


}
