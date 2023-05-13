package main;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import main.util.Zookeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.*;

@RestController
@SpringBootApplication
public class MasterApplication {

    private static int maxRegion = 2;
    private static Zookeeper zk;

    @Autowired
    DataSource dataSource;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MasterApplication.class, args);

        // Zookeeper 连接测试
        Zookeeper zk = new Zookeeper(MasterApplication.maxRegion);
        MasterApplication.zk = zk;
        zk.connect();
    }

    // 建表操作
    @RequestMapping("/create")
    public JSONObject createTable(@RequestParam("tableName") String tName) {
        JSONObject res = new JSONObject();
        // 1 检查是否已经存在同名 table
        if(zk.hasTable(tName)) {
            res.put("status", 204);
            res.put("msg", "表" + tName + "已存在");
        } else if(!zk.hasWritable()){
            res.put("status", 204);
            res.put("msg", "暂无可写 Region");
        } else {
            res.put("status", 200);
            res.put("addr", zk.getCreateServer());
        }
        return res;
    }

    // 读取操作
    @RequestMapping("/read")
    public JSONObject readTable(@RequestParam("tableName") String tName) {
        JSONObject res = new JSONObject();
        // 1 检查是否存在 table
        if(!zk.hasTable(tName)) {
            res.put("status", 204);
            res.put("msg", "表" + tName + "不存在");
        } else {
            res.put("status", 200);
            res.put("addr", zk.getReadServer(tName));
        }
        return res;
    }

    // 插入 / 建索引
    @RequestMapping("/write")
    public JSONObject writeTable(@RequestParam("tableName") String tName) {
        JSONObject res = new JSONObject();
        // 1 检查是否存在 table
        if(!zk.hasTable(tName)) {
            res.put("status", 204);
            res.put("msg", "表" + tName + "不存在");
        } else if(!zk.isWritable(tName)){
            res.put("status", 204);
            res.put("msg", "所在区间不可写");
        } else {
            res.put("status", 200);
            res.put("addr", zk.getWriteServer(tName));
        }
        return res;
    }

    // Meta 信息（tableName + writable）
    @RequestMapping("/meta")
    public JSONObject getMeta() {
        JSONObject res = new JSONObject();
        res.put("data", zk.getMeta());
        return res;
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
