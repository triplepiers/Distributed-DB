package Client;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class Main {
    public static void main(String[] args) {
        System.out.println("Program init...");

        // 1. 创建客户端连接，每 3000 ms 重试，最大重试 10 次
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000, 10);

        // 2. 连接服务器
        //    可以添加 .namespace(str) 指定独立根路径 `/str`
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("ip:port")
                .sessionTimeoutMs(60 * 1000)
                .connectionTimeoutMs(15 * 1000)
                .retryPolicy(retryPolicy)
                .build();

        // 3. 开启连接
        client.start();
    }
}