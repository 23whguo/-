package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.ConsultationSession;
import org.springframework.stereotype.Repository;

/**
 * 咨询会话Mapper接口
 * @author system
 */
@Mapper
public interface ConsultationSessionMapper extends BaseMapper<ConsultationSession> {
}
