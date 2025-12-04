package com.lynn.museum.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * 博物馆项目全局配置属性
 * 统一管理所有硬编码配置
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "museum")
public class MuseumConfigProperties {

    /**
     * 应用基础配置
     */
    private App app = new App();

    /**
     * 安全相关配置
     */
    private Security security = new Security();

    /**
     * 业务规则配置
     */
    private Business business = new Business();

    /**
     * 第三方服务配置
     */
    private External external = new External();

    @Data
    public static class App {
        /**
         * 应用名称
         */
        private String name = "文博探索";
        
        /**
         * 应用版本
         */
        private String version = "1.0.0";
        
        /**
         * 应用描述
         */
        private String description = "博物馆智能管理系统";
        
        /**
         * 开发团队
         */
        private String author = "lynn";
        
        /**
         * 许可证
         */
        private License license = new License();
        
        @Data
        public static class License {
            private String name = "Apache 2.0";
            private String url = "http://www.apache.org/licenses/LICENSE-2.0.html";
        }
    }

    @Data
    public static class Security {
        /**
         * 密码策略配置
         */
        private Password password = new Password();
        
        /**
         * Token配置
         */
        private Token token = new Token();
        
        /**
         * 验证码配置
         */
        private Captcha captcha = new Captcha();
        
        @Data
        public static class Password {
            /**
             * 最小长度
             */
            private int minLength = 6;
            
            /**
             * 最大长度
             */
            private int maxLength = 20;
            
            /**
             * BCrypt加密轮数
             */
            private int bcryptRounds = 10;
            
            /**
             * 密码复杂度要求
             */
            private Complexity complexity = new Complexity();
            
            @Data
            public static class Complexity {
                /**
                 * 是否要求包含数字
                 */
                private boolean requireDigit = true;
                
                /**
                 * 是否要求包含字母
                 */
                private boolean requireLetter = true;
                
                /**
                 * 是否要求包含特殊字符
                 */
                private boolean requireSpecialChar = false;
                
                /**
                 * 不允许全数字
                 */
                private boolean forbidAllDigits = true;
                
                /**
                 * 不允许全字母
                 */
                private boolean forbidAllLetters = true;
            }
        }
        
        @Data
        public static class Token {
            /**
             * Token前缀
             */
            private String prefix = "Bearer ";
            
            /**
             * Header名称
             */
            private String headerName = "Authorization";
            
            /**
             * 用户ID Header名称
             */
            private String userIdHeader = "X-User-Id";
            
            /**
             * 用户名 Header名称
             */
            private String usernameHeader = "X-Username";
        }
        
        @Data
        public static class Captcha {
            /**
             * 验证码长度
             */
            private int length = 4;
            
            /**
             * 验证码有效期（分钟）
             */
            private int expireMinutes = 5;
            
            /**
             * 验证码字符集
             */
            private String charset = "0123456789";
        }
    }

    @Data
    public static class Business {
        /**
         * 用户相关配置
         */
        private User user = new User();
        
        /**
         * 文件上传配置
         */
        private Upload upload = new Upload();
        
        /**
         * 分页配置
         */
        private Pagination pagination = new Pagination();
        
        @Data
        public static class User {
            /**
             * 用户名规则
             */
            private Username username = new Username();
            
            /**
             * 昵称规则
             */
            private Nickname nickname = new Nickname();
            
            /**
             * 性别选项
             */
            private Gender gender = new Gender();
            
            /**
             * 用户状态
             */
            private Status status = new Status();
            
            @Data
            public static class Username {
                private int minLength = 3;
                private int maxLength = 20;
                private String pattern = "^[a-zA-Z0-9_]+$";
                private String message = "用户名只能包含字母、数字和下划线";
            }
            
            @Data
            public static class Nickname {
                private int maxLength = 50;
                private String message = "昵称长度不能超过50个字符";
            }
            
            @Data
            public static class Gender {
                private Map<Integer, String> options = Map.of(
                    0, "未知",
                    1, "男", 
                    2, "女"
                );
            }
            
            @Data
            public static class Status {
                private Map<Integer, String> options = Map.of(
                    0, "禁用",
                    1, "启用"
                );
            }
        }
        
        @Data
        public static class Upload {
            /**
             * 允许的文件类型
             */
            private List<String> allowedTypes = List.of(
                "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx"
            );
            
            /**
             * 最大文件大小（MB）
             */
            private long maxSizeMB = 10;
            
            /**
             * 上传路径
             */
            private String path = "/uploads/";
        }
        
        @Data
        public static class Pagination {
            /**
             * 默认页大小
             */
            private int defaultSize = 10;
            
            /**
             * 最大页大小
             */
            private int maxSize = 100;
        }
    }

    @Data
    public static class External {
        /**
         * API文档配置
         */
        private ApiDoc apiDoc = new ApiDoc();
        
        /**
         * 邮件服务配置
         */
        private Email email = new Email();
        
        /**
         * 短信服务配置
         */
        private Sms sms = new Sms();
        
        @Data
        public static class ApiDoc {
            /**
             * Swagger UI路径
             */
            private List<String> swaggerPaths = List.of(
                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v2/api-docs/**"
            );
            
            /**
             * Knife4j路径
             */
            private List<String> knife4jPaths = List.of(
                "/doc.html", "/webjars/**", "/swagger-resources/**"
            );
        }
        
        @Data
        public static class Email {
            /**
             * 是否启用邮件服务
             */
            private boolean enabled = false;
            
            /**
             * SMTP服务器
             */
            private String host = "smtp.example.com";
            
            /**
             * 端口
             */
            private int port = 587;
            
            /**
             * 发件人
             */
            private String from = "noreply@museum.com";
        }
        
        @Data
        public static class Sms {
            /**
             * 是否启用短信服务
             */
            private boolean enabled = false;
            
            /**
             * 服务商
             */
            private String provider = "aliyun";
            
            /**
             * 签名
             */
            private String signature = "博物馆";
        }
    }
}
