package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.SysFileInfo;

/**
 * 文件信息数据访问接口
 * @author system
 */
@Mapper
public interface SysFileInfoMapper extends BaseMapper<SysFileInfo> {
    // 使用MyBatis-Plus构造器查询，不需要自定义方法
} 