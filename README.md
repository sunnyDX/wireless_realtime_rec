### 无线准实时推荐：

1. 离线计算好cf相似度矩阵，保存于mysql中.
2. 通过kafka获取用户的行为日志流，解码，json解析
3. 基于用户点击的item，通过周期刷新cf相似度矩阵，推荐该item相似的item

***

#### 功能说明：
* 替换原有附加线程更新mysql，并发送给task的方式
* 采用定时更新广播变量的方式，并发送给executor

***
#### 接口说明：
1. config: 配置文件加载
2. formator: kafka日志流解析，以及json转化
3. storage: 计算hdfs数据同步到mysql
4. streaming: kafka日志实时解析，并处理
5. util: 相关工具接口




