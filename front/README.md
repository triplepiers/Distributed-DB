# 前端项目文档

## 1 需求

1. 显示与 master 的连接状态

    - load 时自动尝试一次
    - 设置 btn，失败时允许手动重连

2. 直接往页面上放一个伪终端得了（找了个高亮的插件）

3. 返回结果用 UI 组件库呈现（打算写个分页查询，两边压力都小一点）

4. （暂时忽略）显示已存在表格的 meta 信息

    - 可以用 toggle 的形式，最开始只显示所有已经存在的 tableName 信息（连接建立时 Master 返回）

    - 选择展开时和对应服务器请求相应表格的具体字段信息

> 感觉没啥必要？信息过期的时候 Master 也不能主动让前端更新信息

---

其他也没啥的样子？这东西就不用做登录注册了吧...