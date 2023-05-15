# TODO

## 1 未实现

### 1.1 数据的主从备份

- Copy 1 使用 FTP 备份（amazing，不懂）
- Copy 2 使用 JDBC 的 API 复制 SQL 文件传给 Slave，Slave 读取到 MySQL 中执行

---

- 曾经的美好计划：

    用 JDBC 配置 MySQL 自带的主从配置（难绷，到现在没搞完）

- 目前的想法：干脆在前端用 Promise.all 把请求转到 Region 中的 master + slave 上，全搞完才当 success

    - 相当于 client 实现备份（非常离谱）
    - 需要改接口（master 可能需要返回 region 中 master + slaves 的地址列表）


### 1.2 初始化脚本

1. MySQL 的初始化脚本

    - `distributed` database 的建立
    - 可以放 1-2 张样本表

2. Zookeeper 的初始化脚本（如果后面改进了就不用写）

    - 创建持久化节点
        ```text
        - region1
            - tables
            - slaves
        - region2
            - tables
            - slaves
        ```

## 2 待改进

1. 多线程

    Master & RegionServer 目前都是单线程工作

2. 自动初始化

    Master 现在默认 1.2 中提到的路径都在，可以全改成 notExist -> createNode 的版本（更友好）

3. 根据命令行参数动态修改启动的 port（好像没必要）

    - Copy 2 中有一个“自动获取空闲端口号”的功能
    - 不过 SpringBoot 的一键启动的话应该没办法改？

4. 动态获取本机 ip（可选）

    - 现在写死了

5. 更大更好更强的均衡策略

    - 目前每台 Server 在哪个 Region 是写死的（大雾），可以设计下策略（可能又要大改接口）
    - 目前也写死了只有 2 个 Region
        - 新 Server 上线应该放在哪个 region
        - Copy 2 上线直接把旧表全删了，然后整个 copy 新的
    - 负载太大的时候整张 table 换到另一个 region 去

6. 前端

    - 没事干可以定期向 Master 请求一下 Meta 数据（然后显示一下时间）