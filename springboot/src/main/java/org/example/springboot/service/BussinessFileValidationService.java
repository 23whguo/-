package org.example.springboot.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.enumClass.FileBusinessTypeEnum;
import org.example.springboot.enumClass.FileTypeEnum;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.SysFileInfoMapper;
import org.springframework.stereotype.Service;

/**
 * 文件上传验证服务
 * @author system
 */
@Slf4j
@Service
public class BussinessFileValidationService {

    @Resource
    private SysFileInfoMapper fileInfoMapper;

    /**
     *
     * @param originalName 文件名
     * @param businessType 文件类型

     */
    public void validateFileUpload(String originalName, String businessType) {
        
        // 1. 文件类型识别
        FileTypeEnum fileType = FileTypeEnum.getByFileName(originalName);
        log.info("文件类型识别: {} -> {}", originalName, fileType.getCode());
        
        // 2. 业务类型验证
        FileBusinessTypeEnum.isAllowedFileBussinessType(businessType);
        FileBusinessTypeEnum businessEnum = FileBusinessTypeEnum.getByCode(businessType);
        if (businessEnum == null) {
            throw new BusinessException("不支持的业务类型: " + businessType);
        }
        
        // 3. 文件类型与业务类型匹配检查
        if (!FileBusinessTypeEnum.isTypeMatchBussinessType(fileType.getCode(), businessType)) {
            log.info("文件类型与业务类型匹配检查,originalName: {},fileType:{},bussinessType:{} ", originalName,fileType, businessType);
            throw new BusinessException(String.format(
                "业务类型 [%s] 不支持文件类型 [%s]，允许的类型: %s", 
                businessEnum.getDesc(), 
                fileType.getDesc(),
                String.join(",", businessEnum.getAllowedTypes())
            ));
        }


        
        log.info("文件验证通过: 业务类型={}, 文件类型={}",
                businessEnum.getDesc(), fileType.getDesc());

    }




    /**
     * 验证业务权限
     */
    public boolean validateBusinessPermission(String businessType, String businessId, Long userId) {
        // 这里可以根据具体业务需求添加权限验证逻辑
        // 例如：验证用户是否有权限操作该业务对象
        log.info("验证业务权限: 用户ID={}, 业务类型={}, 业务ID={}", userId, businessType, businessId);
        return true;
    }
} 