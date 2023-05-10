package main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.*;

@RestController
@SpringBootApplication
public class MasterApplication {
    @Autowired
    DataSource dataSource;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MasterApplication.class, args);

        // Zookeeper 连接测试
        // Zookeeper zk = new Zookeeper();
        // zk.connect();

    }

    @RequestMapping("/")
    public String hello() {
        return "hello world";
    }

    // 测试 JDBC
    @RequestMapping("/test")
    public void testJDBC() {
        System.out.println(dataSource);
        try {
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "select * from users";
            ResultSet rs = stmt.executeQuery(sql);
            // 打印结果
            System.out.println("uid, username, password:");
            while(rs.next()) {
                System.out.println(rs.getString(2) + ", " + rs.getString(1) + ", " + rs.getString(3));
            }
            // 释放连接
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("数据库连接失败");
            System.out.println(e);
        }
    }
}
