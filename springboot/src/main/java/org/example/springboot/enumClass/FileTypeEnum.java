package org.example.springboot.enumClass;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 文件类型枚举
 * 统一管理所有支持的文件类型及其扩展名
 * 
 * @author system
 */
@Getter
public enum FileTypeEnum {
    
    // 图片类型
    IMG("IMG", "图片", Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg")),
    
    // 文档类型  
    PDF("PDF", "PDF文档", List.of("pdf")),
    DOC("DOC", "Word文档", Arrays.asList("doc", "docx")),
    XLS("XLS", "Excel表格", Arrays.asList("xls", "xlsx")),
    PPT("PPT", "PPT演示", Arrays.asList("ppt", "pptx")),
    TXT("TXT", "文本文件", Arrays.asList("txt", "md", "log")),
    
    // 音频类型
    AUDIO("AUDIO", "音频", Arrays.asList("mp3", "wav", "flac", "aac", "m4a", "ogg")),
    
    // 视频类型
    VIDEO("VIDEO", "视频", Arrays.asList("mp4", "avi", "mov", "wmv", "flv", "mkv", "webm")),
    
    // 压缩文件
    ZIP("ZIP", "压缩文件", Arrays.asList("zip", "rar", "7z", "tar", "gz")),
    
    // 其他类型
    OTHER("OTHER", "其他", List.of());

    private final String code;
    private final String desc;
    private final List<String> extensions;

    FileTypeEnum(String code, String desc, List<String> extensions) {
        this.code = code;
        this.desc = desc;
        this.extensions = extensions;
    }


    public static  boolean isAllowType(String fileType){
        if(StrUtil.isBlank(fileType)){
            return false;
        }
        FileTypeEnum[] values = FileTypeEnum.values();
       List<String>  valueCodes = Arrays.stream(values).map(FileTypeEnum::getCode).toList();
        return valueCodes.contains(fileType);
    }

    /**
     * 根据文件扩展名获取文件类型
     * 
     * @param extension 文件扩展名（支持带点或不带点格式）
     * @return 对应的文件类型枚举
     */
    public static FileTypeEnum getByExtension(String extension) {
        if (StrUtil.isEmpty(extension)) {
            return OTHER;
        }
        
        // 标准化扩展名：移除点号并转为小写
        String normalizedExt = extension.toLowerCase().replace(".", "");
        
        for (FileTypeEnum type : values()) {
            if (type.getExtensions().contains(normalizedExt)) {
                return type;
            }
        }
        return OTHER;
    }

    /**
     * 根据原始文件名获取文件类型
     * 
     * @param fileName 原始文件名
     * @return 对应的文件类型枚举
     */
    public static FileTypeEnum getByFileName(String fileName) {
        if (StrUtil.isEmpty(fileName)) {
            return OTHER;
        }
        
        // 获取文件扩展名
        String extension = getFileExtension(fileName);
        return getByExtension(extension);
    }


    
    /**
     * 从文件名中提取扩展名
     */
    private static String getFileExtension(String fileName) {
        if (StrUtil.isEmpty(fileName)) {
            return "";
        }
        
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1); // 不包含点号
        }
        return "";
    }

} 