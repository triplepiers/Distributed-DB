package main;

import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import main.util.Zookeeper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;

@RestController
@CrossOrigin
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class MasterApplication {

    private static int maxRegion = 2;
    private static Zookeeper zk;

    public static void main(String[] args) {
        SpringApplication.run(MasterApplication.class, args);
    }

    // 用来获取配置文件中的端口号
    @Value("${server.port}")
    private int port;

    @PostConstruct
    public void init() {
        // 输出本机信息
        try {
            String IP = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Master & Zookeeper are @"+ IP);
            System.out.println("Master listening Port: " + this.port);
            // Zookeeper 连接测试
            Zookeeper zk = new Zookeeper(MasterApplication.maxRegion, IP);
            MasterApplication.zk = zk;
            zk.connect();
        } catch (Exception e) {
            System.out.println("IP信息获取失败");
        }

    }

    @PreDestroy
    public void disconnect() {
        zk.disconnect();
        System.out.println("Zookeeper connection CLOSED");
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

}
