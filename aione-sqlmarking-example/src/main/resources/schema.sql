-- 创建测试用户表
DROP TABLE IF EXISTS test_user;

CREATE TABLE test_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(200) UNIQUE NOT NULL,
    age INT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    deleted TINYINT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入测试数据
INSERT INTO test_user (name, email, age, status) VALUES 
('张三', 'zhangsan@test.com', 25, 'ACTIVE'),
('李四', 'lisi@test.com', 30, 'ACTIVE'),
('王五', 'wangwu@test.com', 28, 'INACTIVE'),
('赵六', 'zhaoliu@test.com', 35, 'ACTIVE'),
('测试用户1', 'test1@example.com', 22, 'ACTIVE'),
('测试用户2', 'test2@example.com', 27, 'ACTIVE');