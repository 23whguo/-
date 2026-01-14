package org.example.springboot.enumClass;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 文件业务类型枚举
 * @author system
 */
@Getter
public enum FileBusinessTypeEnum {
    
    // 用户相关
    USER_AVATAR("USER_AVATAR", "用户头像", new String[]{"IMG"}),
    USER_BACKGROUND("USER_BACKGROUND", "用户背景", new String[]{"IMG"}),
    
    // 文章相关
    ARTICLE("ARTICLE", "文章封面", new String[]{"IMG"}),


    
    // 系统相关
    TEMP_FILE("TEMP_FILE", "临时文件", new String[]{"IMG", "PDF", "DOC", "TXT"}),
    SYSTEM_NOTICE("SYSTEM_NOTICE", "系统通知", new String[]{"IMG"});

    // Getter方法
    private final String code;
    private final String desc;

    private final String[] allowedTypes;  // 允许的文件类型


    FileBusinessTypeEnum(String code, String desc, String[] allowedTypes) {
        this.code = code;
        this.desc = desc;
        this.allowedTypes = allowedTypes;

    }

    /**
     * 根据业务类型代码获取枚举
     */
    public static FileBusinessTypeEnum getByCode(String code) {
        for (FileBusinessTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 检查文件类型是否允许
     */
    public static boolean isAllowedFileBussinessType(String fileBussinessType) {


        if(StrUtil.isBlank(fileBussinessType)){
            return false;
        }
        FileBusinessTypeEnum[] values = FileBusinessTypeEnum.values();
        List<String> valueCodes = Arrays.stream(values).map(FileBusinessTypeEnum::getCode).toList();
        return valueCodes.contains(fileBussinessType);

    }
    /**
     * 业务文件类型和文件类型匹配检查
     */
    public static boolean isTypeMatchBussinessType(String fileType, String bussinessType) {
        FileBusinessTypeEnum fileBusinessTypeEnum = getByCode(bussinessType);
        if(fileBusinessTypeEnum == null){
            return false;
        }

            List<String> allowTypesList = Arrays.stream(fileBusinessTypeEnum.getAllowedTypes()).toList();
            if(!allowTypesList.contains(fileType)){
                return false;
            }
        return true;


    }


} 