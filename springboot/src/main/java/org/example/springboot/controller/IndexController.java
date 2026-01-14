package org.example.springboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 首页控制器
 * 用于处理根路径访问，避免404错误
 * 提供系统欢迎信息和API文档入口
 */
@Controller
public class IndexController {

    /**
     * 根路径欢迎页面
     * 当访问根路径时显示欢迎信息和API文档链接
     *
     * @return 返回HTML格式的欢迎页面
     */
    @GetMapping("/")
    @ResponseBody
    public String index() {
        return """
               <!DOCTYPE html>
               <html lang="zh-CN">
               <head>
                   <meta charset="UTF-8">
                   <meta name="viewport" content="width=device-width, initial-scale=1.0">
                   <title>大学生心理健康打卡社区 - API服务</title>
                   <style>
                       body {
                           font-family: 'Microsoft YaHei', Arial, sans-serif;
                           margin: 0;
                           padding: 40px;
                           background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                           color: white;
                           min-height: 100vh;
                           display: flex;
                           align-items: center;
                           justify-content: center;
                       }
                       .container {
                           text-align: center;
                           background: rgba(255, 255, 255, 0.1);
                           padding: 40px;
                           border-radius: 20px;
                           backdrop-filter: blur(10px);
                           box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
                       }
                       h1 {
                           margin-bottom: 20px;
                           font-size: 2.5em;
                           font-weight: 300;
                       }
                       .subtitle {
                           font-size: 1.2em;
                           margin-bottom: 30px;
                           opacity: 0.9;
                       }
                       .links {
                           margin-top: 30px;
                       }
                       .link-button {
                           display: inline-block;
                           padding: 12px 24px;
                           margin: 10px;
                           background: rgba(255, 255, 255, 0.2);
                           color: white;
                           text-decoration: none;
                           border-radius: 25px;
                           transition: all 0.3s ease;
                           border: 1px solid rgba(255, 255, 255, 0.3);
                       }
                       .link-button:hover {
                           background: rgba(255, 255, 255, 0.3);
                           transform: translateY(-2px);
                           box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
                       }
                       .info {
                           margin-top: 20px;
                           font-size: 0.9em;
                           opacity: 0.8;
                       }
                   </style>
               </head>
               <body>
                   <div class="container">
                       <h1>🌟 大学生心理健康打卡社区</h1>
                       <div class="subtitle">API 服务正在运行中...</div>
                       
                       <div class="links">
                           <a href="/doc.html" class="link-button">📚 查看API文档</a>
                           <a href="/api/user/login" class="link-button">🔑 用户登录API</a>
                       </div>
                       
                       <div class="info">
                           <p>✅ Spring Boot 应用已成功启动</p>
                           <p>🚀 API 前缀: /api</p>
                           <p>📖 文档地址: <a href="/doc.html" style="color: #fff;">/doc.html</a></p>
                       </div>
                   </div>
               </body>
               </html>
               """;
    }

    /**
     * 健康检查接口
     * 用于检查服务是否正常运行
     *
     * @return 返回服务状态信息
     */
    @GetMapping("/health")
    @ResponseBody
    public String health() {
        return "🎉 服务运行正常！";
    }
} 