package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.common.Result;
import org.example.springboot.service.SpeechToTextService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/speech")
@Tag(name = "语音能力", description = "语音转文字（用于AI辅导输入）")
public class SpeechController {

    private final SpeechToTextService speechToTextService;

    public SpeechController(SpeechToTextService speechToTextService) {
        this.speechToTextService = speechToTextService;
    }

    @Operation(summary = "语音转文字", description = "上传音频文件并返回识别文本")
    @PostMapping(value = "/stt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, String>> speechToText(@RequestPart("file") MultipartFile file) {
        try {
            String text = speechToTextService.transcribe(file);
            return Result.success(Map.of("text", text));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("语音转文字接口异常: {}", e.getMessage(), e);
            return Result.error("语音识别失败: " + e.getMessage());
        }
    }
}

