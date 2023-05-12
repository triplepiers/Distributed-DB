# 前后端接口 + Zk 路径

## 1 前后端接口

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