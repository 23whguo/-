package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.AiAnalysisTask;

/**
 * AI分析任务Mapper接口
 * @author system
 */
@Mapper
public interface AiAnalysisTaskMapper extends BaseMapper<AiAnalysisTask> {
}


