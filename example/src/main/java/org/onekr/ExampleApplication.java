package org.onekr;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * MyBatis Plus SQL标记功能示例应用
 *
 * @author Billy
 */
@SpringBootApplication(scanBasePackages = {"org.onekr"})
@MapperScan("org.onekr.mapper")
public class ExampleApplication {

//    @Value("${server.port:8080}")
//    private static int port;

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(ExampleApplication.class, args);

        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        String port = environment.getProperty("server.port");

        String host = "http://localhost:" + port;

        System.out.println("=== MyBatis Plus SQL标记功能示例应用启动成功 ===");
        System.out.println("访问 " + host + "/api/sql-marking/test/status 查看SQL标记状态");
        System.out.println("访问 " + host + "/h2-console 查看H2数据库控制台");
        System.out.println("=== 可用的测试接口 ===");
        System.out.println("POST " + host + "/api/sql-marking/test/insert - 测试INSERT操作");
        System.out.println("GET  " + host + "/api/sql-marking/test/select - 测试SELECT操作");
        System.out.println("PUT  " + host + "/api/sql-marking/test/update - 测试UPDATE操作");
        System.out.println("DELETE " + host + "/api/sql-marking/test/delete - 测试DELETE操作");
        System.out.println("POST " + host + "/api/sql-marking/test/comprehensive - 综合测试所有CRUD操作");
        System.out.println("GET  " + host + "/api/sql-marking/test/config - 测试配置功能");
        System.out.println("GET  " + host + "/api/sql-marking/test/exception-handling - 测试异常处理");
        System.out.println("GET  " + host + "/api/sql-marking/test/status - 获取拦截器状态");
        System.out.println("POST " + host + "/api/sql-marking/test/reset-count - 重置执行计数");
    }
}