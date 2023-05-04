# README

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