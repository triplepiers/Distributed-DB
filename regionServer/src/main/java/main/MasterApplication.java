package main;

import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import main.util.Checksum;
import main.util.Zookeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@RestController
@CrossOrigin
@SpringBootApplication
public class MasterApplication {

    private static Zookeeper zk;

    // 用来获取配置文件中的端口号
    @Value("${server.port}")
    private int port;

    @Autowired
    DataSource dataSource;

    private int countVisit = 0;

    public static void main(String[] args) {
        SpringApplication.run(MasterApplication.class, args);
    }

    @PostConstruct
    public void init() {
        // 设置 zkServer 地址
        Scanner in = new Scanner(System.in);
        System.out.print("Please input zkServer IP: ");
        String zkServerIP = in.next();

        // 测试动态获取本机 ip
        try {
            String IP = InetAddress.getLocalHost().getHostAddress();
            String selfAddr = IP + ":" + this.port;
            System.out.println("Current Server is @"+ selfAddr);
            // Zookeeper 连接测试
            Zookeeper zk = new Zookeeper(dataSource, selfAddr, zkServerIP);
            MasterApplication.zk = zk;
            zk.connect();
        } catch (Exception e) {
            System.out.println("初始化失败");
        }
    }

    // 返回累积的访问数量
    @RequestMapping("/count")
    public int getCountVisit() {
        int val = this.countVisit;
        this.countVisit = 0;
        return val;
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
        this.countVisit += 1;
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
            // 同步
            sync(sql);
        } catch (Exception e) {
            res.put("status", 204);
            res.put("msg", "SQL 执行失败");
            return res;
        }

        // 往 /tables 下插入新的记录
        if(zk.checkMaster()) {
            this.zk.addTable(tName);
            this.zk.checkTable(tName);
        }

        res.put("status", 200);
        return res;
    }

    // drop Table 请求（需要更新 zk）
    @RequestMapping("/drop")
    public JSONObject dropTable(@RequestBody Map<String, String> data) {
        this.countVisit += 1;
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
            // 同步
            sync(sql);
        } catch (Exception e) {
            res.put("status", 204);
            res.put("msg", "SQL 执行失败");
            return res;
        }

        // 往 /tables 下插入新的记录
        if(zk.checkMaster()) {
            this.zk.removeTable(tName);
            this.zk.checkTable(tName);
        }

        res.put("status", 200);
        return res;
    }

    // Select 操作需要统一格式后输出（首行附带 key meta 信息）
    @RequestMapping("/select")
    public JSONObject select(@RequestBody Map<String, String> data) {
        this.countVisit += 1;
        JSONObject res = new JSONObject();
        String sql = data.get("sql");
        // 缺少参数
        if (sql == null) {
            res.put("status", 204);
            res.put("msg", "缺少必要参数");
            return res;
        }

        Checksum c = new Checksum(dataSource);
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

        // 转发 select 查询语句
        // JSONObject[] checksumList = new JSONObject[zk.getSlaves().size()];
        List<JSONObject> checksumList = new ArrayList<JSONObject>();
        List<String> slavePaths = zk.getSlaves();
        Integer slavesNum = slavePaths.size();
        for(String slaveAddr : slavePaths) {
            // 发送 POST 请求
            String url = "http://" +slaveAddr + "/vote";
            // 构建参数
            JSONObject params = new JSONObject();
            params.put("sql", sql);
            // 构建 header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<JSONObject> httpEntity = new HttpEntity<>(params, headers);
            // 发起请求
            RestTemplate client = new RestTemplate();
            // 这里没有管 slave 的回应（管发不管执行）
            JSONObject ans = client.postForEntity(url, httpEntity, JSONObject.class).getBody();
            checksumList.add(ans);
        }

        Integer myChecksum = c.getCRC4result(rs);
        Integer vote = 0;
        for (JSONObject ans : checksumList) {
            if (ans.get("data").equals(myChecksum)) vote++;
        }
        System.out.println("投票总数：" + vote);

        ArrayList<String> colNames = null;
        if( vote >= slavesNum/2+1) {
            // 提取一下 meta 信息
            if(rs != null) {
                try {
                    ResultSetMetaData meta = rs.getMetaData();
                    countCol = meta.getColumnCount();
                    colNames = new ArrayList<>();
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
                ArrayList<JSONObject> result = new ArrayList<>();
                while(rs.next()) {
                    JSONObject record = new JSONObject();
                    for(int i = 0 ; i < countCol ; i++) {
                        record.put(colNames.get(i), rs.getString(i+1));
                    }
                    result.add(record);
                }
                res.put("data", result);

                // 释放连接
                rs.close();
                stmt.close();
                conn.close();
            } catch (Exception e) {}
        } else {
            res.put("status", 204);
            res.put("msg", "投票失败");
        }

        res.put("status", 200);
        return res;
    }

    // 只返回 成功/失败（感觉 execute 可以单独封装一下）
    @RequestMapping("/execute")
    public JSONObject execute(@RequestBody Map<String, String> data) {
        this.countVisit += 1;
        JSONObject res = new JSONObject();
        String sql = data.get("sql");
        String tName = data.get("tableName");
        System.out.println("Execute " + sql);

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
            // 同步
            sync(sql);
        } catch (Exception e) {
            res.put("status", 204);
            res.put("msg", "SQL 执行失败");
            return res;
        }

        if(zk.checkMaster()) {
            this.zk.checkTable(tName);
        }

        res.put("status", 200);
        return res;
    }

    @RequestMapping("/dump")
    public JSONObject dumpTable(@RequestBody Map<String, String> data) {
        JSONObject res = new JSONObject();
        String srcIP = data.get("srcIP");
        String tName = data.get("tableName");
        zk.dumpRemoteTable(srcIP, tName);
        res.put("status", 200);
        return res;
    }

    // 向从节点同步
    private void sync(String sql) {
        // 如果是主节点，向从节点转发 sql 操作
        if(zk.checkMaster()) {
            System.out.println("ok sync");
            List<String> slavePaths = zk.getSlaves();
            for(String slaveAddr : slavePaths) {
                // 发送 POST 请求
                String url = "http://" +slaveAddr + "/execute";
                // 构建参数
                JSONObject params = new JSONObject();
                params.put("sql", sql);
                // 构建 header
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<JSONObject> httpEntity = new HttpEntity<>(params, headers);
                // 发起请求
                RestTemplate client = new RestTemplate();
                client.postForEntity(url, httpEntity, JSONObject.class);
                // 这里没有管 slave 的回应（管发不管执行）
    //            JSONObject ans = client.postForEntity(url, httpEntity, JSONObject.class).getBody();
    //            assert ans != null;
    //            System.out.println("answer = " + ans.toJSONString());
            }
        }
    }
    // Vote
    @RequestMapping("/vote")
    public JSONObject vote(@RequestBody Map<String, String> data) {
        JSONObject res = new JSONObject();
        String sql = data.get("sql");
        // 缺少参数
        if (sql == null) {
            res.put("status", 204);
            res.put("msg", "缺少必要参数");
            return res;
        }
        Checksum c = new Checksum(dataSource);
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
        res.put("data", c.getCRC4result(rs));
        res.put("status", 200);
        return res;
    }
}
