package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.common.Result;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "测试接口")
@Slf4j
@RestController
@RequestMapping("Test")
public class TestController {

    @Operation(summary = "JWT认证测试接口", description = "测试JWT token解析和角色获取")
    @GetMapping("/jwt-test")
    public Result<Map<String, Object>> jwtTest() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取当前token
            String token = JwtTokenUtils.getCurrentToken();
            result.put("tokenExists", token != null);
            result.put("tokenLength", token != null ? token.length() : 0);
            
            if (token != null) {
                // 获取用户信息
                Long userId = JwtTokenUtils.getCurrentUserId();
                String username = JwtTokenUtils.getCurrentUsername();
                Integer userRole = JwtTokenUtils.getCurrentUserRole();
                
                result.put("userId", userId);
                result.put("username", username);
                result.put("userRole", userRole);
                result.put("isAdmin", userRole != null && userRole.equals(2));
                
                // 验证token
                JwtTokenUtils.TokenValidationResult validation = JwtTokenUtils.validateToken(token);
                if (validation != null) {
                    result.put("tokenValid", validation.isValid());
                    result.put("validationUserId", validation.getUserId());
                    result.put("validationUsername", validation.getUsername());
                    result.put("validationRoleType", validation.getRoleType());
                } else {
                    result.put("tokenValid", false);
                }
            }
            
            log.info("JWT测试结果: {}", result);
            return Result.success("JWT测试完成", result);
            
        } catch (Exception e) {
            log.error("JWT测试失败", e);
            result.put("error", e.getMessage());
            return Result.error("JWT测试失败: " + e.getMessage());
        }
    }

    @GetMapping("/debug")
    public void Test(@RequestParam Integer index,@RequestParam Integer count) {
        // 简单的测试方法，可以在这里打断点
        log.info("测试开始，index:{}", index);
        if(index==1){
            log.debug("测试1开始");
            String message = "Hello Debug";
            System.out.println(message);

            // 计算示例
            int a = 10;
            int b = 20;
            int result =add(a,b);
            log.debug("测试1结束");
            log.info("结果: " + result);
        }else if(index==2){
            log.debug("测试2开始");
            loopTest(count);
            log.debug("测试2结束");
        } else if (index==3){
            log.debug("测试3开始");
            threadTest();
            log.debug("测试3结束");
        }  else {
            log.warn("非法参数:index:{}", index);

        }

        log.info("测试结束，index:{}", index);




    }
    public int add(int a, int b){
        return a+b;
    }


    public void loopTest(Integer count) {
        // 循环测试，方便观察变量变化
        for (int i = 0; i < count; i++) {
            System.out.println("循环次数: " + i);
        }
    }

    public void threadTest()  {
        new Thread(()->{
            System.out.println("线程1-1");
            System.out.println("线程1-2");
            System.out.println("线程1-3");
        }).start();

        new Thread(()->{
            System.out.println("线程2-1");
            System.out.println("线程2-2");
            System.out.println("线程2-3");
        }).start();
    }
}
