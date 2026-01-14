package org.example.springboot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Slf4j
@Service
public class SpeechToTextService {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);
    private static final String DEFAULT_STT_MODEL = "FunAudioLLM/SenseVoiceSmall";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    public SpeechToTextService(ObjectMapper objectMapper) {
        this.webClient = WebClient.builder().build();
        this.objectMapper = objectMapper;
    }

    public String transcribe(MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new IllegalArgumentException("音频文件不能为空");
        }

        String normalizedBaseUrl = normalizeBaseUrl(baseUrl);
        String transcriptionUrl = normalizedBaseUrl.endsWith("/v1")
                ? normalizedBaseUrl + "/audio/transcriptions"
                : normalizedBaseUrl + "/v1/audio/transcriptions";
        String filename = StringUtils.hasText(audioFile.getOriginalFilename())
                ? audioFile.getOriginalFilename()
                : "recording.webm";

        try {
            byte[] bytes = audioFile.getBytes();

            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(bytes) {
                        @Override
                        public String getFilename() {
                            return filename;
                        }
                    })
                    .contentType(resolveMediaType(audioFile.getContentType()));
            builder.part("model", DEFAULT_STT_MODEL);

            String responseBody = webClient.post()
                    .uri(transcriptionUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(DEFAULT_TIMEOUT);

            if (!StringUtils.hasText(responseBody)) {
                throw new IllegalStateException("语音识别服务返回为空");
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode textNode = root.get("text");
            if (textNode == null || !textNode.isTextual()) {
                throw new IllegalStateException("语音识别服务返回格式异常");
            }

            String text = textNode.asText("");
            if (!StringUtils.hasText(text)) {
                throw new IllegalStateException("未识别到有效文本");
            }

            return text.trim();
        } catch (Exception e) {
            log.error("语音转文字失败: {}", e.getMessage(), e);
            throw new RuntimeException("语音识别失败: " + e.getMessage(), e);
        }
    }

    private static String normalizeBaseUrl(String url) {
        if (!StringUtils.hasText(url)) return "";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static MediaType resolveMediaType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (Exception ignored) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
