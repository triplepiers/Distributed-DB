# README

## 1 Set Up

### 1.1 环境

> 至少在这个环境能动

- Windows 10（谁来教我把 WSL 端口映射到本机啊！）
- JDK 7（至少 zookeeper 的运行环境是 JDK 7） 
    > JDK7 [下载链接](https://repo.huaweicloud.com/java/jdk/7u80-b15/)（华为镜像）
- IDEA（不重要）

### 1.2 安装 Zookeeper
> 教程见[此处](https://blog.csdn.net/qq_33316784/article/details/88563482#:~:text=windows%E5%AE%89%E8%A3%85zookeeper%E6%95%99%E7%A8%8B%2003-16%201.%20%E4%B8%8B%E8%BD%BD%20Zookeeper%E5%AE%89%E8%A3%85%20%E5%8C%85%EF%BC%9A%E4%BB%8E%E5%AE%98%E7%BD%91%E4%B8%8B%E8%BD%BD%20Zookeeper%20%E7%9A%84,2.%20%E8%A7%A3%E5%8E%8B%20%E5%AE%89%E8%A3%85%20%E5%8C%85%EF%BC%9A%E5%B0%86%E4%B8%8B%E8%BD%BD%E7%9A%84%20%E5%AE%89%E8%A3%85%20%E5%8C%85%E8%A7%A3%E5%8E%8B%E5%88%B0%E4%BD%A0%E6%83%B3%E8%A6%81%20%E5%AE%89%E8%A3%85%20%E7%9A%84%E7%9B%AE%E5%BD%95%E4%B8%8B%E3%80%82)

1. 下载对应版本的 [Zookeeper](https://archive.apache.org/dist/zookeeper/zookeeper-3.4.14/)
2. 使用 `tar -zxvf zookeeper-3.4.14.tar.gz` 解压到安装目录
3. 在安装目录（与 `bin` 同级）下新建 `/data` & `/log`
4. 将 `conf` 目录下的 `zoo_sample.cfg` 文件，复制一份，重命名为 `zoo.cfg`
    ```yaml
   dataDir = '/data 的绝对路径'
   dataLogDir = '/log 的绝对路径'
   clientPort = 2181 # 即为 Server 监听的端口号
   ```
5. 把 `/bin` 下的 `zkServer.cmd` 拖到命令行里执行就可以了，成功运行的预期显示如下：
    ```text
   # 最后一行
    - INFO  [main:NIOServerCnxnFactory@89] - binding to port 0.0.0.0/0.0.0.0:2181
    ```
6. 保险起见也运行一下 `zkCli.cmd`，预期的正常运行结果如下：
    ```text
    # 最后几行
    [zk: localhost:2181(CONNECTING) 0] 2023-04-27 23:01:27,347 [myid:] - INFO  [main-SendThread(localhost:2181):ClientCnxn$SendThread@1299] - Session establishment complete on server localhost/0:0:0:0:0:0:0:1:2181, sessionid = 0x10000a13e400000, negotiated timeout = 30000
    WATCHER::
    WatchedEvent state:SyncConnected type:None path:null
   
    # 正常连接时 Server 响应如下：
    - INFO  [NIOServerCxn.Factory:0.0.0.0/0.0.0.0:2181:NIOServerCnxnFactory@222] - Accepted socket connection from /0:0:0:0:0:0:0:1:14123 
    ```
#### INFO - 可能的报错
- `JAVA_HOME unset` => 设置环境变量 `JAVA_HOME` 到 JDK `/bin` 的上一级文件夹就可以了

## 2 Basic

> Reference - [Curator 基本使用](https://zhuanlan.zhihu.com/p/611161550#:~:text=Curator%E7%9A%84%E5%9F%BA%E6%9C%AC%E4%BD%BF%E7%94%A8%201%20%E4%B8%80.%20%E5%89%8D%E8%A8%80%20%E5%AE%98%E7%BD%91%20%3A%20Apache%20Curator,%E7%89%88%E6%9C%AC%20...%203%20%E4%B8%89.%20Curator%E4%BD%BF%E7%94%A8%201.%20%E5%88%9B%E5%BB%BA%E5%AE%A2%E6%88%B7%E7%AB%AF%E8%BF%9E%E6%8E%A5%20)

- 依赖
  - Curator 4.2.0
  - Zookeeper 3.4.14