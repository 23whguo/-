package org.example.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DebugTest {

    @Test
    public void Test() {
        // 简单的测试方法，可以在这里打断点
        String message = "Hello Debug";
        System.out.println(message);
        
        // 计算示例
        int a = 10;
        int b = 20;
        int result = a + b;
        
        System.out.println("结果: " + result);
    }
    
    @Test
    public void loopTest() {
        // 循环测试，方便观察变量变化
        for (int i = 0; i < 5; i++) {
            System.out.println("循环次数: " + i);
        }
    }
} 