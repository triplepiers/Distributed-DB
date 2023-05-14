# README

---

## UPDATE @2023/05/14

- RegionServer 基本完工，工程见 `/regionServer`，接口见 md 文件

    - 把一些操作移到 `@PostConstruct / @PreDestroy` 以确保能够正常访问 dataSource 并在推出前自动关闭 Zookeeper 会话

    - 整体处于 **单机能动** 的状态

- 前端基本接上了（完全没考虑美感和用户胡乱操作的可能性）

- TODO

    1. 写一下运行指南

## UPDATE @2023/05/13

1. MASTER 基本完工，工程见 `/master` ，接口和 Zookeeper 节点设计见 md 文件（节点可能不太准）
2. RegionServer 开了个头

> 痛苦面具

## UPDATE @2023/05/10

本仓库结构调整：

1. `sample` 之前的简单终端连接 zookeeper 样例
2. `front` 一个草率的 Vue 项目（默认端口：8080）
3. `master` 一个草率的 SpringBoot 项目
    - 默认端口 8088，开启了 debug 模式
    - 访问 `localhost:8088/` 应该会显示 `hello world`
    - 在启动类的 main 函数里添加了连接 zookeeper、读取 `/test` 节点信息的操作
        - 具体实现在 Zookeeper 类里
        - 正常连接会在终端打印读取结果

---

> Curator 这什么狗屎依赖关系啊 ！

## 1 Zookeeper 运行环境

1. Windows 10 （问就是不会配虚拟机的端口）
2. JRE 8 （JDK 1.8及以上）
3. Zookeeper 3.8.0

> 至少在上述环境下能动

### 1.1 Zookeeper 安装与运行
> 资源在 `/dependencies` 目录下

1. 将 `apache-zookeeper-3.8.0-bin.tar.gz` 挪到合适的安装路径下
2. 使用 Powershell 解压至当前目录，命令如下：
    ```shell
    tar -zxvf apache-zookeeper-3.8.0-bin.tar.gz
    ```
3. 在根目录下（与 `/bin` 同级）创建 `data` & `log`路径，用于存储数据和日志
4. 复制 `/dependencies` 下的 `zoo.cfg` 至安装目录的 `/conf` 文件夹下

    请根据实际情况修改 `dataDir` 与 `dataLogDir` 配置项为实际绝对路径

5. 把 `/bin/zkServer.cmd` 拖到命令提示符中运行

### 1.2 JRE8 安装与环境变量配置

- 在运行 `zkServer.cmd` 的过程中，您往往会遇到各种迷幻的错误 —— 这往往是由于未配置环境变量 or JDK 版本不匹配导致的
- 为此，您需要安装 JDK1.8（或者仅 JRE8）来保证 Zookeeper 3.8.0 的正确运行）
- `/dependencies` 下提供了 JRE8 的安装程序，请按需取用
- 安装完毕后，请设置系统变量 `JAVA_HOME` 至 JRE8 的安装根路径（`/bin` 的上一级目录）

## 2 项目开发环境

1. open JDK 20
2. Curator 5.2.1
3. Zookeeper 3.8.0

> `pom.xml` 里已经写好了，直接用就行

## 3 可供参考的 GitHub 仓库

1. https://github.com/Zhang-Each/Distributed-MiniSQL
2. https://github.com/zwc233/LargeScaleSystem
