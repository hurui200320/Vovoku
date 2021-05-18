# Backend

后端

## 功能

### 普通用户

1. 图片管理功能：上传、删除、修改标定数据、查询
2. 用户管理功能：登录、查询、删除
3. 图片分类功能：选择一副图片和某一个模型，识别图片数字内容
4. 模型训练功能：给定模型训练的批次，轮次，学习率等参数发起训练任务

普通用户使用用户名密码登录，服务端签发Token，通过Token调用API。

上传图片将会被存储到本地文件系统，但可灵活拓展到其他存储上，例如亚马逊S3或阿里云OSS等，模型数据类似。

### 特权用户

系统管理功能，能够查询所有用户的数据。

特权用户需要对请求体进行签名进行鉴权，详见后续超控接口说明。

特权用户仅提供基础的管理功能，诸如图片、用户、模型管理（增删改查），不提供图片上传、模型训练等功能。

### 环境配置

读取YAML文件作为环境配置

## 数据库设计

### User

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

```sql
create table picture_tag
(
    tag_id          serial not null,
    file_path       text   not null,
    user_id         int    not null,
    tag_data        json   not null,
    used_for_train  bool   not null,
    folder_name     text   not null
);

create unique index picture_tag_tag_id_uindex
    on picture_tag (tag_id);

alter table picture_tag
    add constraint picture_tag_pk
        primary key (tag_id);
```

### ModelInfo

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

TODO - 尚未完工

### 公开接口

`/public`

#### token

`POST /public/token`

表单：

+ `username` 登录使用的用户名
+ `password` 登录使用的密码摘要，由密码经过一次MD5运算得到

返回：

+ 200 - 内容即为Token
+ 401 - 凭据不正确

用途：

除了公开接口外，所有发送到用户接口的请求都必须在请求头`Authorization`中附带有效的Token。Token有效期15分钟，每一次成功访问接口将自动重制有效期为15分钟。

### 用户接口

`/user`

必要请求头：

+ `Authorization` - Token

#### account

`/user/account`

##### whoami

`GET /user/account`

返回Token对应的用户的Json信息。对应`DatabaseUserPojo`，示例：

```json
{
    "id": 4,
    "username": "hurui",
    "passwordHash": "bed128365216c019988915ed3add75fb"
}
```

##### delete

`POST /user/delete`

删除Token对应的账户。请求体为表单：

+ `username` - 待删除用户的用户名
+ `password_raw` - 待删除用户的密码明文

服务器将对提交的参数进行检查：用户名需要与Token对应的用户名匹配，密码则需要经过计算MD5后与数据库核验匹配，二者同时满足才会删除账号。

若用户名不匹配，服务器返回400，若密码不匹配，服务器返回401。成功时返回204。

#### picture

`/user/picture`

##### GET



##### POST



##### :picTagId

`/user/picture/:picTagId`，其中`:picTagId`为可变参数，对应图片的id号。

###### PUT

###### GET

###### DELET



### 超控接口



## 线程

### 主线程

初始化资源、拉起下述各线程后退出。

### HTTP服务线程

使用Javalin对外提供HTTP API

### 训练任务下发线程

定期对数据库中待发布的任务进行扫描，并发布到Redis中

### 训练任务锁检测线程

对于待发布任务，检测到对应任务ID的锁之后将更新任务状态为训练中。

对于训练中的任务，检测不到对应任务ID的锁时判定锁丢失，标记任务失败。

### 资源回收线程 - TODO

遍历上传文件夹，根据数据库内容，只保留有引用的文件，删掉未在数据库中出现过的数据。同时清理数据库，删除掉不存在账户创建的图片、模型等。

由于该功能并非刚需，因此暂时还没有实现。

~~没这个功能又不是不能用~~

