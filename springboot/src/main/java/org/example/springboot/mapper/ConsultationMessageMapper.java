package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.ConsultationMessage;
import org.springframework.stereotype.Repository;

/**
 * 咨询消息Mapper接口
 * @author system
 */
@Mapper
public interface ConsultationMessageMapper extends BaseMapper<ConsultationMessage> {
}
