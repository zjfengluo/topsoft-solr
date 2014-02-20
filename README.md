### 一个基于maven结构的solr项目，主要应用于公司项目中的搜索功能

* **[solrj-client](https://github.com/wangyingang/topsoft-solr/tree/master/solrj-client)** 是一个基于solrj再次封装的库，主要目的如下：
    1. 缓存solrj server实例，以提升查询性能。
    2. 支持一个查询同时在多核(multi cores)上进行，支持串行和并行两种查询模式。并行模式会启动多线程模式，建议配置线程池以达到复用，避免浪费系统资源（如不配置，会创建新线程池）。
    3. 支持强类型（泛型）查询，并提供了支持guava funtional式的查询接口，方便数据类型自动转换。
* **[icic-dataimport-scheduler](https://github.com/wangyingang/topsoft-solr/tree/master/icis-dataimport-scheduler)** 是一个用于同步solr索引的库，以配置的方式可以定义各种细粒度的定时调度作业。
* **[icis-nameverify](https://github.com/wangyingang/topsoft-solr/tree/master/icis-nameverify)**是一个solr 4.3多核结构的web示例程序，里面配置了工商企业名称查重的索引结构，其主要目的是将**icis-dataimport-scheduler**打包到该web应用程序中，可以看作是一个提供了自动同步索引功能的**solr.war**。
* **[icis-nameverify-client](https://github.com/wangyingang/topsoft-solr/tree/master/icis-nameverify-client)**是一个工商企业名称查重的客户端，提供了综合业务系统使用，依赖于**solrj-client**库。
* **[icis-ecps-client](https://github.com/wangyingang/topsoft-solr/tree/master/icis-ecps-client)**是企业信用信息公示系统的企业查询的客户端，包含了给国家系统和公司系统使用的两套接口，依赖于**solrj-client**库。
