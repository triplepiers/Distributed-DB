package Client;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;

public class Client {
    public static void main(String[] args) throws Exception {
        System.out.println("init...");
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000, 1);
        CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", 5000, 5000, retryPolicy);
        client.start();
        // 创建持久化节点
        // client.create().withMode(CreateMode.EPHEMERAL).forPath("/test", "inittest".getBytes());
        // 读取创建结果
        Stat stat = new Stat();
        System.out.println("data @ /test = " + new String(client.getData().storingStatIn(stat).forPath("/test")));
        client.close();
    }
}
