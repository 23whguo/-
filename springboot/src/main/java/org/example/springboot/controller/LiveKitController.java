package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.common.Result;
import org.example.springboot.service.LiveKitTokenService;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/livekit")
@Tag(name = "LiveKit 实时语音", description = "为前端生成 LiveKit 房间 Token")
public class LiveKitController {

    private final LiveKitTokenService liveKitTokenService;

    public LiveKitController(LiveKitTokenService liveKitTokenService) {
        this.liveKitTokenService = liveKitTokenService;
    }

    @Operation(summary = "生成 LiveKit Token", description = "用于前端加入实时语音房间")
    @GetMapping("/token")
    public Result<LiveKitTokenService.LiveKitTokenResponse> token(
            @RequestParam(required = false) String roomName
    ) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }

        Integer role = JwtTokenUtils.getCurrentUserRole();
        String identity = "user_" + userId;
        String displayName = identity + (role != null ? ("_" + role) : "");

        try {
            LiveKitTokenService.LiveKitTokenResponse res =
                    liveKitTokenService.createToken(roomName, identity, displayName);
            return Result.success(res);
        } catch (Exception e) {
            log.error("生成LiveKit Token失败: {}", e.getMessage(), e);
            return Result.error("生成LiveKit Token失败: " + e.getMessage());
        }
    }
}

