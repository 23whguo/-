package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.EmotionDiary;

/**
 * 情绪日记数据访问层
 * @author system
 */
@Mapper
public interface EmotionDiaryMapper extends BaseMapper<EmotionDiary> {
    // 继承BaseMapper，获得基础的CRUD操作
    // 所有复杂查询都在Service层使用Lambda构造器实现
}

