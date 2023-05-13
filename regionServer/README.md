# SpringBoot 项目

目前的依赖主要是 mybatis

## 1 配置

- 数据库配置

    - 创建 database `distributed`
    - 创建 table `users`，字段如下：
        ```text
        +----------+-------------+------+-----+---------+----------------+
        | Field    | Type        | Null | Key | Default | Extra          |
        +----------+-------------+------+-----+---------+----------------+
        | username | varchar(20) | NO   |     | NULL    |                |
        | id       | int         | NO   | PRI | NULL    | auto_increment |
        | password | varchar(20) | NO   |     | NULL    |                |
        +----------+-------------+------+-----+---------+----------------+
        ```
        然后随便插一点数据

- 数据库连接 `src/main/resources`

    ```yml
    # 方括号内为待修改任务
    spring:
        datasource:
            driver-class-name: com.mysql.cj.jdbc.Driver
            url: jdbc:mysql://localhost:[PortNumber]/[dbName]?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&useSSL=false&serverTimezone=GMT%2b8&allowPublicKeyRetrieval=true
            username: [your username]
            password: [your password]
    ```


## 2 错误解决
> 可能会遇到 `lombok` 依赖一直飘红的问题

- 在“设置-插件” 中安装 lombok 插件，并重启 idea
- 在“设置-编译器-注解处理器”中，启用“注解处理”功能

## 3 理想输出

- 启动项目时

    计划使用 Zookeeper 类连接集群服务器，读取 /test 节点的数据
    
    连接失败会反复报错，不想管可以把 MasterApplication 里的后两行注释掉

- 成功启动后

    - 访问 `localhost:8088/` ，计划返回 'hello world'
    - 访问 `localhost:8088/user` ，计划返回所有用户信息构成的 JSON

## 4 项目结构
> 我 MVC 学的很烂，请大家自行搜一下 mybatis 的相关操作

- MasterApplication 启动类

    这里调了一下 zookeeper 类的方法短暂建立了连接

- controller

    定义了路由（class前是一级路由，function前是二级路由），以及对应路由的返回结果

- entity

    实体类（对应一张 table）

- mapper

    大概是定义了 function 与 SQL 的对应关系？

- util

    工具类，现在只写了 zookeeper 的连接功能
