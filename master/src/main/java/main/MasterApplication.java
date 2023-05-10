package main;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
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

    // 测试 JDBC（打印，并返回 JSON 数组）
    @RequestMapping("/test")
    public JSONArray testJDBC() {
        System.out.println(dataSource);
        // 返回的 JSON 数组
        JSONArray res = new JSONArray();
        try {
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "select * from users";
            ResultSet rs = stmt.executeQuery(sql);
            // 打印结果
            System.out.println("uid, username, password:");
            while(rs.next()) {
                // 终端输出
                System.out.println(rs.getString(2) + ", " + rs.getString(1) + ", " + rs.getString(3));
                // 搓单个 JSON 对象
                JSONObject user = new JSONObject();
                user.put("id", rs.getString(2));
                user.put("username", rs.getString(1));
                user.put("password", rs.getString(3));
                // 塞进数组
                res.add(user);
            }
            // 释放连接
            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            System.out.println("数据库连接失败");
            System.out.println(e);
        }

        return res;
    }
}
