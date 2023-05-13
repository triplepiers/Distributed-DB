package main.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class Zookeeper {

    private final String serverIP = "127.0.0.1";
    private final String serverPort = "2181";


    private CuratorFramework client;


    public void connect() throws Exception {
        String connectStr = this.serverIP + ":" + this.serverPort;
        System.out.println("Trying to connect Zk Sever @" + connectStr);

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000, 1);
        this.client = CuratorFrameworkFactory.newClient(connectStr, 5000, 5000, retryPolicy);
        this.client.start();

        // 创建持久化节点
        // client.create().withMode(CreateMode.EPHEMERAL).forPath("/test", "init test".getBytes());
        // 读取创建结果
        Stat stat = new Stat();
        System.out.println("data @ /test = " + new String(client.getData().storingStatIn(stat).forPath("/test")));
        System.out.println("if you can see the return value, then you're successfully connected.");

        // INIT
        this.initMeta();


//        this.client.close();
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
    public void initMeta() {

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