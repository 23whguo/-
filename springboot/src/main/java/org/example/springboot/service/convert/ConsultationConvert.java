package org.example.springboot.service.convert;

import org.example.springboot.DTO.response.ConsultationMessageResponseDTO;
import org.example.springboot.DTO.response.ConsultationSessionResponseDTO;
import org.example.springboot.entity.ConsultationMessage;
import org.example.springboot.entity.ConsultationSession;
import org.springframework.stereotype.Component;

/**
 * 咨询模块对象转换器
 * @author system
 */
@Component
public class ConsultationConvert {

    /**
     * 转换会话实体为响应DTO
     */
    public ConsultationSessionResponseDTO toResponseDTO(ConsultationSession session) {
        if (session == null) {
            return null;
        }

        ConsultationSessionResponseDTO responseDTO = new ConsultationSessionResponseDTO();
        responseDTO.setId(session.getId());
        responseDTO.setUserId(session.getUserId());
        responseDTO.setSessionTitle(session.getSessionTitle());
        responseDTO.setStartedAt(session.getStartedAt());

        return responseDTO;
    }

    /**
     * 转换消息实体为响应DTO
     */
    public ConsultationMessageResponseDTO toMessageResponseDTO(ConsultationMessage message) {
        if (message == null) {
            return null;
        }

        ConsultationMessageResponseDTO responseDTO = new ConsultationMessageResponseDTO();
        responseDTO.setId(message.getId());
        responseDTO.setSessionId(message.getSessionId());
        responseDTO.setSenderType(message.getSenderType());
        responseDTO.setMessageType(message.getMessageType());
        responseDTO.setContent(message.getContent());
        responseDTO.setEmotionTag(message.getEmotionTag());
        responseDTO.setAiModel(message.getAiModel());
        responseDTO.setCreatedAt(message.getCreatedAt());

        return responseDTO;
    }
}
