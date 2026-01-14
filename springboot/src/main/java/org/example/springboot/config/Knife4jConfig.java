package org.example.springboot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API文档配置类

 * 用于配置Knife4j的API文档展示及相关资源访问
 * Knife4j是一个基于Swagger的API文档增强工具
 */
@Configuration
public class Knife4jConfig {

    /**
     * 配置OpenAPI对象
     * 用于生成API文档的核心配置
     * 包含API文档的基本信息、安全配置等
     *
     * @return OpenAPI 返回配置好的OpenAPI对象
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // 配置接口文档基本信息
                .info(this.getApiInfo());
    }

    /**
     * 获取API文档的基本信息配置
     * 配置API文档的标题、描述、版本等元数据信息
     * 支持以下配置项：
     * - 文档标题
     * - 文档描述
     * - 作者信息（当前已注释）
     * - 许可证信息（当前已注释）
     * - 服务条款（当前已注释）
     * - 版本信息
     *
     * @return Info 返回API文档基本信息对象
     */
    private Info getApiInfo() {
        return new Info()
                .title(" API文档")
                .description("提供API接口")
                .version("1.0.0");
    }
}
