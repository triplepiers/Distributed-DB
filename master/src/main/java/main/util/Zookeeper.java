package main.util;

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

    private ArrayList<Region> meta = new ArrayList<>();

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
            for(String path : childsPath) {
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
        int maxRegion = 2;
        //
        for(int i = 0 ; i < maxRegion; i++) {
            Region region = new Region();
            // 监听 /master 节点
            ZkListener zkListener = new ZkListener(this.client, "/region"+(i+1), region);
            zkListener.listenMaster();
            // 监听 /slaves 的所有子节点
            zkListener.listenSlaves();
            // 监听 /tables 的所有子节点
        }
    }
}

class ZkListener {

    private Region region;
    private String basePath;

    ZkListener(CuratorFramework client, String basePath, Region region) {
        this.client = client;
        this.basePath = basePath;
        this.region = region;
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

    // 监听 slave 的创建与删除事件
    public void listenSlaves() {
        try {
            // 测试监听子节点创建事件
            TreeCache treeCache = new TreeCache(this.client, basePath + "/slaves");
            SlaveListener listener = new SlaveListener();
            // 注册监听
            treeCache.getListenable().addListener(listener);
            // 开启监听
            treeCache.start();
        } catch (Exception e) {
            System.out.println("监听 " + basePath + " 的 SLAVES 时出错");
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
                region.master = masterAddr;

            } catch (Exception e){
                // master 被删除
                System.out.println(basePath + " lost its MASTER");
                region.master = "";
            }
        }
    }

    class SlaveListener implements TreeCacheListener {

        @Override
        public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent treeCacheEvent) throws Exception {
            if (treeCacheEvent.getType() == TreeCacheEvent.Type.NODE_ADDED
                    &&
                    !treeCacheEvent.getData().getPath().equals(basePath + "/slaves")) {
                // 创建新的 slaveNode（第二个条件是忽略父节点本身的创建事件）
                String connectStr = new String(treeCacheEvent.getData().getData());
                region.slaves.add(connectStr);
                region.n_slave = region.slaves.size();
                System.out.println("new SLAVE for " + basePath + " @" + connectStr + ", " + region.n_slave + " SLAVES available now" );
            } else if (treeCacheEvent.getType() == TreeCacheEvent.Type.NODE_REMOVED) {
                // slaveNode 被删除（更新可用数量与路由信息列表）
                String connectStr = new String(treeCacheEvent.getData().getData());
                region.slaves.remove(connectStr);
                region.n_slave = region.slaves.size();
                System.out.println("lost SLAVE for " + basePath + " @" + connectStr + ", " + region.n_slave + " SLAVES available now" );
            }
        }
    }
}