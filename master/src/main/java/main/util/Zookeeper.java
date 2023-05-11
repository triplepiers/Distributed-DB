package main.util;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;

import java.util.HashMap;
import java.util.Map;

public class Zookeeper {
    private final String serverIP = "127.0.0.1";
    private final String serverPort = "2181";

    private CuratorFramework client;

    private Map<String, CuratorCache> cacheMap = new HashMap<>();

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
        listen("/hello");
//        this.client.close();
    }

    // 对指定的 leaf Node 进行监听
    public void listen(String path) {
        try {
            NodeCache nodeCache = new NodeCache(this.client, path);
            NodeCacheListener listener = new NodeCacheListener() {
                @Override
                public void nodeChanged() throws Exception {
                    try {
                        ChildData childData = nodeCache.getCurrentData();
                        System.out.println(path + ", new state = " + childData.getStat());
                    } catch (Exception e){
                        System.out.println(path + "is Deleted");
                    }

                }
            };
            nodeCache.getListenable().addListener(listener);
            nodeCache.start();
        } catch (Exception e) {

        }
    }
}
