# Worker
工作线程

## 功能
根据Redis传递的消息调用DL4J模型训练，并将结果通过Redis返回给后端。

## 配置
读取YAML文件作为环境配置。目前仅需要配置Redis服务器的主机名和端口号。

## 简述
系统中可以有多个工作线程，后端通过向Redis的`info.skyblond.vovoku.commons.RedisTaskDistributionChannel`频道中推送`info.skyblond.vovoku.commons.TrainingTaskDistro`的Json消息即可向Worker发起任务。Worker收到消息后以`task.$id`为key试图加锁，锁保证同一时间有唯一的Worker能够竞争到锁，并继续执行，未竞争到锁的Worker将放弃执行本次任务。

竞争到锁之后Worker将试图拉取模型训练使用的资源，通过不同的路径前缀可以实现对不同资源的访问，例如`file://`表示直接在文件系统中查找文件，未来可以实现通过`https://`通过网络请求文件，或`s3://`以在亚马逊S3存储中读取数据文件。

训练文件为Raw Byte，通过继承`BaseDataFetcher`，可以实现从Raw Byte到DL4J需要的数据格式，具体可以参考`info.skyblond.vovoku.worker.datavec.CustomDataFetcher`，该类实现了将Raw Byte文件转换为MNIST格式的需求。

训练后将对模型进行评测，使用用户提供的测试集，模型存储在临时文件中，写入到指定的位置后即删除。然后Worker向Redis发出训练完成的消息，同时释放已有的锁，等待下一个任务的到来。

训练中产生错误则直接捕捉并通过Redis发送给后端，后端收到消息后更新对应任务的状态。Worker随后继续等待任务。

## 异常情况

除了可捕捉的训练时错误，意料外的错误有可能导致Worker不能正常接受或接受后不能正常完成训练任务。设计上后端只发布状态为`DISTRIBUTING`的任务，并通过监测Redis锁来判断某任务是否被Worker认领。若Worker未能在指定时间内更新锁，则锁失效，后端将任务更新为`ERROR`状态，并记录错误原因为Worker丢失锁。如果Worker成功更新了锁，但是未能完成训练任务，此时现象是任务停在`TRAINING`的时间过长，此时可以通过管理员强行替换锁，使Worker在下一次更新锁时发生错误，使Worker下线。