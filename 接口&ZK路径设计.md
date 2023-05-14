# 前后端接口 + Zk 路径

## 1 前后端接口

### 1.1 Master 接口

#### 1.1.1 获取 Meta 信息

返回所有 Region 上的 Table Name 及其状态（是否可写）

- 方式：GET

- 路径：`/meta`

- 返回样例

    ```text
    {
        "data":[
            {"name":"passwords","writable":true},
            {"name":"users","writable":true},
            {"name":"t2-1","writable":false}]
    }
    ```

#### 1.1.2 Create Table 目标地址

- 方式：GET

- 路径：`/create`

- Query 参数：`tableName`

- 返回样例

    ```text
    # 正常返回
    {
        "status": 200,
        "addr": "127.0.0.1:8080"
    }

    # 异常返回
    {
        "status": 204,
        "msg": "报错信息"
    }
    ```

#### 1.1.3 Select 操作目标地址

- 方式：GET

- 路径：`/read`

- Query 参数：`tableName`

- 返回样例

    ```text
    # 正常返回
    {
        "status": 200,
        "addr": "127.0.0.1:8080"
    }

    # 异常返回
    {
        "status": 204,
        "msg": "表XXX不存在"
    }
    ```


#### 1.1.4 Write 操作目标地址

Write 操作包括：建立索引、INSERT、UPDATE、DELETE、DROP

- 方式：GET

- 路径：`/write`

- Query 参数：`tableName`


- 返回样例

    ```text
    # 正常返回
    {
        "status": 200,
        "addr": "127.0.0.1:8080"
    }

    # 异常返回
    {
        "status": 204,
        "msg": "表XXX不存在"
    }
    ```

### 1.2 RegionServer 接口

#### 1.2.1 Create Table 接口
> 需要往 zookeeper 里插入数据，所以单列

- 方法：POST

- 路径：`/new`

- 参数
    ```json
    {
        "tableName": "users",
        "sql": "建表的完整 sql"
    }
    ```
- 返回样例

    ```text
    # 正常返回
    {
        "status": 200
    }

    # 异常返回
    {
        "status": 204,
        "msg": "SQL 执行失败"
    }
    ```

#### 1.2.2 Select 接口
> 需要返回查询结果，单列

- 方法：POST

- 路径：`/select`

- 参数
    ```json
    {
        "sql": "查询的完整 sql"
    }
    ```
- 返回样例

    ```text
    # 正常返回
    {
        // 列名信息
        "meta": [
            "username",
            "id",
            "password"
        ],
        // 查询结果 - 每一条记录都是一个 record
        "data": [
            [
                "root",
                "1",
                "123456"
            ],
            [
                "admin",
                "2",
                "123"
            ]
        ],
        "status": 200
    }

    # 异常返回
    {
        "status": 204,
        "msg": "SQL 执行失败"
    }
    ```

#### 1.2.3 其他 SQL 语句
> 只返回执行 成功/失败

- 方法：POST

- 路径：`/execute`

- 参数
    ```json
    {
        "sql": "完整 sql"
    }
    ```
- 返回样例

    ```text
    # 正常返回
    {
        "status": 200
    }

    # 异常返回
    {
        "status": 204,
        "msg": "SQL 执行失败"
    }
    ```


## 2 Zookeeper 节点路径

> 非常的简单捏！

```text
- region1
    -(tmp) master: "ip:port"
    - (default=0)slaves "n_slaves"
        - (tmp)[id]: "ip:port"
        - (tmp)[id]: "ip:port"
    - (default=0)tables "n_tables"
        - (tmp)t1 "table_name"
        - (tmp)t2 "table_name"
- region2
    - （下略，结构相同）
```

## 3 Zookeeper Meta 结构

> MD 不能直接写 JSON 是一件多么痛苦的事情 => 单独放到 Meta 类里了

```text
hashMap

- "r1" hashMap
    - "master" String "connectString"
    - "slaves" List<String>
        - connectString 1
    - "tables" List<String>
        - tableName 1
    - "router" int[2]
        - n_slave
        - cur_slave (累加实现轮询)
```