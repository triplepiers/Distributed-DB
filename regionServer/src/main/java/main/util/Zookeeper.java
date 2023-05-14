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
import java.util.function.Function;

public class Zookeeper {

    public Zookeeper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private DataSource dataSource;

    private final String basePath = "/region1"; // 本 regionServer 所在的 regionID

    private final String serverID = "1"; // 本 regionServer 的 ServerID

    private final byte[] selfAddr = "127.0.0.1:8081".getBytes(); // 本 regionServer 后端项目监听的端口

    private final String zkServerAddr  = "127.0.0.1:2181";

    private ZkListener zkListener = null;

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

        // INIT
        this.init();
    }

    public void disconnect() {
        if(this.client != null) {
            this.client.close();
            this.client = null;
        }
    }

    // 初始化当前数据集的 meta 信息
    public void init() {
        ZkListener zkListener = new ZkListener(this.client, this.basePath);
        this.zkListener = zkListener;

        // 1. 检查是否存在 master 节点
        try {
            if (this.client.checkExists().forPath(basePath +  "/master") == null) {
                this.beMaster(); // 尝试成为主节点
            } else {
                this.beSlave(); // 成为从节点
            }
        } catch (Exception e) {
            System.out.println("检查 MASTER 是否存在时出错");
        }
    }


    // 尝试成为 master
    public void beMaster() {
        // 注册临时节点
        try {
            this.client.create().withMode(CreateMode.EPHEMERAL).forPath(basePath + "/master", this.selfAddr);
        } catch (Exception e) {
            System.out.println("未能成为 MASTER");
            // be slave
            this.beSlave();
            return;
        }

        System.out.println("current server is a MASTER");
        // 关闭对 /master 的监听
        this.zkListener.stopListenMaster();
        // 删除对应的 slave 节点
        try {
            this.client.delete().forPath(basePath + "/slaves/" + serverID);
        } catch (Exception e) {}

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

    // 成为 slave
    public void beSlave() {
        System.out.println("current server is a SLAVE");
        // 把自己的 ID 写到 /slaves 下
        try {
            this.client.create().withMode(CreateMode.EPHEMERAL).forPath(basePath + "/slaves/" + serverID, selfAddr);
        } catch (Exception e) {}
        // 注册对 /master 的监听
        this.zkListener.listenMaster();
    }

    // 查看 tables 下共有多少个节点
    public int getTableNum() {
        int tot = -1;
        try {
            tot = this.client.getChildren().forPath(basePath + "/tables").size();
        } catch (Exception e) {
            System.out.println("未能获取 " + basePath + "/tables 下的子节点总数");
        }
        return tot;
    }

    // 向 /tables 下添加新的 table 信息
    public void addTable(int idx, String tName) {
        try {
            this.client.create().withMode(CreateMode.EPHEMERAL).forPath(basePath + "/tables/" + (idx+1), tName.getBytes());
        } catch (Exception e) {
            System.out.println("向 zookeeper 插入 ID " + idx + " TABLE " + tName + " 信息时出错");
        }
    }

    class ZkListener {

        public void stopListenMaster() {
            if(this.treeCache != null) {
                try {
                    this.treeCache.close();
                } catch (Exception e) {
                    System.out.println("未能成功停止对 master 的监视");
                }
            }
        }

        private TreeCache treeCache = null;

        private String basePath;

        ZkListener(CuratorFramework client, String basePath) {
            this.client = client;
            this.basePath = basePath;
        }

        private CuratorFramework client;

        // 对指定的 leaf Node 进行监听
        public void listenMaster() {
            try {
                TreeCache treeCache = new TreeCache(this.client, basePath + "/master");
                this.treeCache  = treeCache ;
                MasterListener masterListener = new MasterListener();
                treeCache .getListenable().addListener(masterListener);
                treeCache .start();
            } catch (Exception e) {
                System.out.println("监听 " + basePath + " 的 MASTER 时出错");
            }
        }

        class MasterListener implements TreeCacheListener {

            @Override
            public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent treeCacheEvent) throws Exception {
                if(treeCacheEvent.getType() == TreeCacheEvent.Type.NODE_REMOVED) {
                    // master 被删除
                    System.out.println("原 MASTER 失去连接。尝试成为 MASTER ...");
                    beMaster();
                }
            }
        }

    }
}
