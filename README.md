# Vovoku
软件工程上机实验


## 命名
由 [UNIQ](https://uniq.site/zh) 名生成器产生。


## 实验要求

能够熟练运用一种开发工具和数据库开发与测试一个基于mnist的分类系统。

要求细则：
1. 图片的管理功能，图片上传、删除和标定
2. 用户管理功能，用户登录，建立，查询，修改和删除
3. 图片分类功能实现，用户选择一副图片能识别图片数字内容
4. 模型训练参数设定，系统给出模型训练的批次，轮次，学习率等参数设定界面


## 系统设计

采用Gradle的模块设计，`backend`为服务端（后端），`frontend`为客户端（前端）。

后端技术栈如下：
+ Java/Kotlin（主要开发语言）
+ Javalin（提供HTTP API框架）
+ Ktorm（ORM数据库支持）
+ PostgreSQL（数据库）
+ Docker compose（环境治理）

前端技术栈如下：
+ Java/Kotlin（主要开发语言）
+ JavaFX（GUI框架）
+ CSS（美化视觉效果）

