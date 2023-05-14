package main;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import main.util.Zookeeper;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Map;

@RestController
@SpringBootApplication
public class MasterApplication {

    private static Zookeeper zk;

    @Autowired
    DataSource dataSource;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MasterApplication.class, args);
    }

    @PostConstruct
    public void init() {
        // Zookeeper 连接测试
        Zookeeper zk = new Zookeeper(dataSource);
        MasterApplication.zk = zk;
        zk.connect();
    }



    @RequestMapping("/")
    public String hello() {
        return "hello world";
    }

    // 关闭 zookeeper 会话（否则临时节点不能正常销毁）
    @PreDestroy
    public void disconnect() {
        zk.disconnect();
        System.out.println("Zookeeper connection CLOSED");
    }

    // create Table 请求（需要更新 zk）
    @RequestMapping("/new")
    public JSONObject createTable(@RequestBody Map<String, String> data) {
        JSONObject res = new JSONObject();
        String tName = data.get("tableName");
        String sql = data.get("sql");
        // 缺少参数
        if (tName == null || sql == null) {
            res.put("status", 204);
            res.put("msg", "缺少必要参数");
            return res;
        }

        System.out.println("TABLE: " + data.get("tableName"));
        System.out.println("SQL: " + data.get("sql"));

        // 执行 sql
        try {
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            // executeQuery 必须产生 resultSet，此处使用 execute
            stmt.execute(sql);
            // 释放连接
            stmt.close();
            conn.close();
        } catch (Exception e) {
            res.put("status", 204);
            res.put("msg", "SQL 执行失败");
            return res;
        }

        // 往 /tables 下插入新的记录
        this.zk.addTable(tName);

        res.put("status", 200);
        return res;
    }

    // drop Table 请求（需要更新 zk）
    @RequestMapping("/drop")
    public JSONObject dropTable(@RequestBody Map<String, String> data) {
        JSONObject res = new JSONObject();
        String tName = data.get("tableName");
        String sql = data.get("sql");
        // 缺少参数
        if (tName == null || sql == null) {
            res.put("status", 204);
            res.put("msg", "缺少必要参数");
            return res;
        }
        System.out.println("DROP TABLE: " + data.get("tableName"));

        // 执行 sql
        try {
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            // executeQuery 必须产生 resultSet，此处使用 execute
            stmt.execute(sql);
            // 释放连接
            stmt.close();
            conn.close();
        } catch (Exception e) {
            res.put("status", 204);
            res.put("msg", "SQL 执行失败");
            return res;
        }

        // 往 /tables 下插入新的记录
        this.zk.removeTable(tName);

        res.put("status", 200);
        return res;
    }

    // Select 操作需要统一格式后输出（首行附带 key meta 信息）
    @RequestMapping("/select")
    public JSONObject select(@RequestBody Map<String, String> data) {
        JSONObject res = new JSONObject();
        String sql = data.get("sql");
        // 缺少参数
        if (sql == null) {
            res.put("status", 204);
            res.put("msg", "缺少必要参数");
            return res;
        }

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        int countCol = -1;
        // 尝试执行一下
        try {
            conn = dataSource.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
        } catch (Exception e) {
            res.put("status", 204);
            res.put("msg", "SQL 执行失败");
            return res;
        }


        // 提取一下 meta 信息
        if(rs != null) {
            try {
                ResultSetMetaData meta = rs.getMetaData();
                countCol = meta.getColumnCount();
                ArrayList<String> colNames = new ArrayList<>();
                for(int i = 0 ; i < countCol ; i++) {
//                    System.out.println(i + " " +  meta.getColumnName(i+1));
                    colNames.add(meta.getColumnName(i+1));
                }
                res.put("meta", colNames);
            } catch (Exception e) {
                System.out.println("提取 resultSet Meta 信息时出错");
                System.out.println(e);
            }
        }

        // 放一下数据集（每一行都是一个 Array）
        try {
            ArrayList<ArrayList<String>> result = new ArrayList<>();
            while(rs.next()) {
                ArrayList<String> record = new ArrayList<>();
                for(int i = 0 ; i < countCol ; i++) {
                    record.add(rs.getString(i+1));
                }
                result.add(record);
            }
            res.put("data", result);

            // 释放连接
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {

        }

        res.put("status", 200);
        return res;
    }

    // 只返回 成功/失败（感觉 execute 可以单独封装一下）
    @RequestMapping("/execute")
    public JSONObject execute(@RequestBody Map<String, String> data) {
        JSONObject res = new JSONObject();
        String sql = data.get("sql");
        // 缺少参数
        if (sql == null) {
            res.put("status", 204);
            res.put("msg", "缺少必要参数");
            return res;
        }

        // 尝试执行
        try {
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            // 释放连接
            stmt.close();
            conn.close();
        } catch (Exception e) {
            res.put("status", 204);
            res.put("msg", "SQL 执行失败");
            return res;
        }

        res.put("status", 200);
        return res;
    }

}
