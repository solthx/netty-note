# netty-note

# 1. Netty Intro
## 1. Netty构建块
### 1. Channel
和Java NIO的一个基本构造, 即可以看作是 输入，输出 的载体. 它可以被打开或关闭.

### 2. CallBack
就是平时认识的回调。 （JDK8 通过实现函数式接口来做回调）

### 3. Future
JDK的concurrent包里有个Future对象，但是:
1. 是阻塞的.
2. 需要手动检测任务是否完成.
因此, Netty重写了Future，即:`ChannelFuture`.  每次`Channel`的出站口都是返回一个`ChannelFuture`. 异步使用方法:
- 向ChannelFuture中添加一个 `ChannelFutureListener` 对象:
    ```java
    Channel channel = ...;
    ChannelFuture future = channel.connect( new InetSocketAdress("127.0.0.1", 8080) );
    future.addListener( new ChannelFutureListener(){
        // 继承了ChannelFutureListener的匿名类
        @Override
        public void operationComplete( ChannelFuture future ){
            if ( future.isSuccess() ){
                ...
            }else{
                ...
                // 如果发生错误， 则打印错误信息
                Throwable cause = new future.cause();
                cause.printStackTrace();
            }
        }
    } );
    ```

### 4. Event 和 ChannelHandler
netty使用不同的事件来通知我们状态的改变(即Event事件的发生，Reactor模型). 而每个`ChannelHandler`的实例都类似于一种为了响应特定事件而被执行的回调！