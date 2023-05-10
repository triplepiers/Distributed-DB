package main.util;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;

public class Zookeeper {
    private final String serverIP = "127.0.0.1";
    private final String serverPort = "2181";

    public void connect() throws Exception {
        String connectStr = this.serverIP + ":" + this.serverPort;
        System.out.println("Trying to connect Zk Sever @" + connectStr);

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000, 1);
        CuratorFramework client = CuratorFrameworkFactory.newClient(connectStr, 5000, 5000, retryPolicy);
        client.start();
        // 创建持久化节点
        // client.create().withMode(CreateMode.EPHEMERAL).forPath("/test", "init test".getBytes());
        // 读取创建结果
        Stat stat = new Stat();
        System.out.println("data @ /test = " + new String(client.getData().storingStatIn(stat).forPath("/test")));
        System.out.println("if you can see the return value, then you're successfully connected.");
        client.close();
    }
}
