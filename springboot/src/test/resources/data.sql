-- 插入测试用户数据
-- 密码为 123456 的 BCrypt 加密值
INSERT INTO users (id, username, email, password, nickname, phone, user_type, status, created_at, updated_at) VALUES 
(1, 'admin', 'admin@example.com', '$2a$10$K8K8K8K8K8K8K8K8K8K8K.K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8', '系统管理员', '13800138000', 'admin', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'testuser', 'test@example.com', '$2a$10$K8K8K8K8K8K8K8K8K8K8K.K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8', '测试用户', '13800138001', 'user', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'merchant', 'merchant@example.com', '$2a$10$K8K8K8K8K8K8K8K8K8K8K.K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8', '测试商家', '13800138002', 'merchant', 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'banneduser', 'banned@example.com', '$2a$10$K8K8K8K8K8K8K8K8K8K8K.K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8', '被封禁用户', '13800138003', 'user', 'banned', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'inactiveuser', 'inactive@example.com', '$2a$10$K8K8K8K8K8K8K8K8K8K8K.K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8', '未激活用户', '13800138004', 'user', 'inactive', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); 