package main.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class Zookeeper {

    public Zookeeper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private DataSource dataSource;

    private final String basePath = "/region1"; // 本 regionServer 所在的 regionID

    private final int serverID = 1; // 本 regionServer 的 ServerID

    private final byte[] selfAddr = "127.0.0.1:8081".getBytes(); // 本 regionServer 后端项目监听的端口

    private final String zkServerAddr  = "127.0.0.1:2181";


    private CuratorFramework client;


    public void connect() {
        System.out.println("Trying to connect Zk Sever @" + zkServerAddr);

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000, 1);
        this.client = CuratorFrameworkFactory.newClient(zkServerAddr, 5000, 5000, retryPolicy);
        client.start();
//
//        // 创建持久化节点
//        // client.create().withMode(CreateMode.EPHEMERAL).forPath("/test", "init test".getBytes());
        // 读取创建结果
        try {
            Stat stat = new Stat();
            System.out.println("data @ /test = " + new String(client.getData().storingStatIn(stat).forPath("/test")));
            System.out.println("if you can see the return value, then you're successfully connected.");
        } catch (Exception e) {
            System.out.println(e);
        }
//
//        // INIT
        this.init();
    }

    public void disconnect() {
        if(this.client != null) {
            this.client.close();
            this.client = null;
        }
    }

    // 获取指定路径下所有节点的[数据]！
    public List<String> getChildsData(String parentPath) {
        // 获取所有子节点路径
        List<String> childsData = new ArrayList<>();
        try {
            List<String> childsPath = this.client.getChildren().forPath(parentPath);
            for (String path : childsPath) {
                String data = new String(this.client.getData().forPath(parentPath + "/" + path));
                childsData.add(data);
                System.out.println("data @" + path + " = " + data);
            }
        } catch (Exception e) {
            System.out.println("子节点数据获取失败");
        }
        return childsData;
    }

    // 初始化当前数据集的 meta 信息
    public void init() {
        // 1. 检查是否存在 master 节点
        try {
            if (this.client.checkExists().forPath(basePath +  "/master") == null) {
                System.out.println("not exist");
                this.beMaster(); // 尝试成为主节点
            } else {
                System.out.println("already exist");
            }
        } catch (Exception e) {
            System.out.println("检查 MASTER 是否存在时出错");
        }
//        ZkListener zkListener = new ZkListener(this.client, "/region" + this.regionID);

    }

    public void beMaster() {
        // 注册临时节点
        try {
            this.client.create().withMode(CreateMode.EPHEMERAL).forPath(basePath + "/master", this.selfAddr);
        } catch (Exception e) {
            System.out.println("未能成为 MASTER");
            // be slave
            return;
        }
        // 向 /tables 路径写入所有 table 信息
        try {
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            String sql = "show tables";
            ResultSet rs = stmt.executeQuery(sql);
            // 打印结果
            System.out.println("TABLES in current database are as follows:");
            int countTable = 0;
            while(rs.next()) {
                countTable += 1;
                String tName = rs.getString(1);
                System.out.println(countTable + " " + tName);
                // 写进 /tables
                this.client.create().withMode(CreateMode.EPHEMERAL).forPath(basePath + "/tables/" + countTable, tName.getBytes());
            }
            // 释放连接
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("获取 TABLE 列表失败");
            System.out.println(e);
        }
    }
}

class ZkListener {

    private String basePath;

    ZkListener(CuratorFramework client, String basePath) {
        this.client = client;
        this.basePath = basePath;
    }

    private CuratorFramework client;

    // 对指定的 leaf Node 进行监听
    public void listenMaster() {
        try {
            NodeCache nodeCache = new NodeCache(this.client, basePath + "/master");
            MaterListener masterListener = new MaterListener(nodeCache);
            nodeCache.getListenable().addListener(masterListener);
            nodeCache.start();
        } catch (Exception e) {
            System.out.println("监听 " + basePath + " 的 MASTER 时出错");
        }
    }

    class MaterListener implements NodeCacheListener {

        MaterListener(NodeCache nodeCache) {
            this.nodeCache = nodeCache;
        }

        private NodeCache nodeCache;

        @Override
        public void nodeChanged() {
            try {
                // master 已存在/被创建
                ChildData childData = nodeCache.getCurrentData();
                String masterAddr = new String(childData.getData());
                System.out.println(basePath + "'s MASTER is @" + masterAddr);

            } catch (Exception e){
                // master 被删除
                System.out.println(basePath + " lost its MASTER");
            }
        }
    }

}