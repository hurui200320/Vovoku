# Backend

后端

## 功能

### 普通用户

1. 图片管理功能：上传、删除、修改标定数据、查询
2. 用户管理功能：登录、查询、删除
3. 图片分类功能：选择一副图片和某一个模型，识别图片数字内容
4. 模型训练功能：给定模型训练的批次，轮次，学习率等参数发起训练任务

普通用户使用用户名密码登录，服务端签发Token，通过Token调用API。

图片上传到某路径下，每用户一个文件夹，文件名对应数据库中全局唯一的主键。文件名生成：用户ID、当前时间戳、文件Hash 标定数据在数据库中每条一个数据。

模型数据类似，每用户一个文件夹，每个模型一个zip文件，DL4J格式。文件名：用户ID、时间戳。 模型在数据库中存储创建时的信息，时间、训练参数等。

### 特权用户 - Done

系统管理功能，能够查询所有用户的数据。

特权用户需要对请求体进行签名进行鉴权。

特权用户仅提供基础的管理功能，诸如图片、用户、模型管理（增删改查），不提供图片上传、模型训练等功能。

### 环境配置

读取YAML文件作为环境配置

## 数据库实体

### User

| 用户ID | 用户名 | 密码 | 
| :----: | :----: | :----: |
| integer  | varchar(50) | varchar(50) |
| 非空、主键、唯一、自增  | 唯一、非空 | 非空 |

初始化SQL：

```sql
create table "user"
(
    user_id  serial      not null
        constraint user_pk
            primary key,
    username varchar(50) not null,
    password varchar(50) not null
);

alter table "user"
    owner to postgres;

create unique index user_user_id_uindex
    on "user" (user_id);

create unique index user_username_uindex
    on "user" (username);
```

### PictureTag

| TagID | FilePath | 所属用户Id | 标定数据 | 
| :----: | :----: | :----: | :----: |
| integer  | json | integer | json |
| 非空、主键、唯一、自增  | 非空 | 非空 | 非空 |

初始化SQL：

```sql
create table picture_tag
(
    tag_id    serial not null,
    file_path text   not null,
    user_id   int    not null,
    tag_data  json   not null,
    used_for_train  bool   not null,
    folder_name  text   not null
);

create unique index picture_tag_tag_id_uindex
    on picture_tag (tag_id);

alter table picture_tag
    add constraint picture_tag_pk
        primary key (tag_id);
```

### ModelInfo

| ModelID | FilePath | 所属用户Id | 创建信息 | 训练状态 |
| :----: | :----: | :----: | :----: | :----: |
| integer  | text | integer | json | json |
| 非空、主键、唯一、自增  |  | 非空 | 非空 | 非空 |

初始化SQL：

```sql
create table model_info
(
    model_id      serial not null,
    file_path     text,
    user_id       int    not null,
    create_info   json   not null,
    training_info json   not null,
    last_status   int    not null
);

create unique index model_info_model_id_uindex
    on model_info (model_id);

alter table model_info
    add constraint model_info_pk
        primary key (model_id);
```

## API说明

API遵循REST规范：GET为无副作用获取数据，POST为有副作用地创建新的资源，PUT用于无副作用的修改已有数据，DELETE用于删除。

## 线程

### 主线程

初始化资源、拉起下述各线程后退出。

### HTTP服务线程

使用Javalin对外提供HTTP API

### 训练任务下发线程

定期对数据库中未发布的任务进行扫描，并发布到Redis中

### 训练任务进度检测线程

检测训练中的任务的Redis锁，若锁失效，且状态未更新，则训练标记为失败

### 推理任务执行线程池

线程池内可控执行利用模型对单张图片进行推理得到结果的操作

### 磁盘清理线程

遍历上传文件夹，根据数据库内容，只保留有引用的文件，删掉未在数据库中出现过的数据

