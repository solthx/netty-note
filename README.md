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

# 2. 第一个Netty程序
内容部分在README和代码注释里.
[experiment1](https://github.com/solthx/netty-note/tree/master/experiment1/echo_server_and_client)

# 3. Netty的组件和设计

## 1. Channel接口
Channel是对socket的抽象 , socket的基本操作有: `bind()`, `connect()`, `read()`, `write()`. 
### 1. 预定义的常用Channel:
1. `EmbeddedChannel`;
2. `LocalServerChannel`;
3. `NioDatagramChannel`;
4. `NioSctpChannel`;
5. `NioSocketChannel`;

### 2. Channel的方法:
1. `eventLoop()`:
返回Channel处于的EventLoop
2. `pipline()`:
返回Channel处于的ChannelPipline
3. `isActive()`:
返回Channel是否处于活动状态; 活动的意义依赖于底层的传输协议. 例如, 一个socket传输一旦连接到了远程结点，那么就是活动的; 但一个Datagram传输一旦打开就是活动的;
4. `locaolAddress()`:
返回本地的SocketAddress
5. `remoteAddress()`:
返回远程的SocketAddress
6. `write()`:
将数据写到远程结点. 这个数据被打包成msg传递给ChannelPipline，并且排队直到被冲刷(flush)
7. `flush()`:
将之前已写的数据冲刷到底层传输.
8. `writeAndFlush()`:
6+7=8

### 3. Channel是线程安全的
```java
// 多个线程使用同一个Channel
final Channel channel = ...;
final ByteBuf buf = unPooled.copiedBuffer("your data", CharsetUtil.UTF_8).retain();

Runnable writer = new Runnable(){
    @Override
    public void run(){
        channel.writeAndFlush(buf.duplicate());
    }
}

// 开个线程池
Executor executor = Executors.newCachedThreadPool();

// 多线程发送数据
executor.execute(writer);
executor.execute(writer);
executor.execute(writer);
```



## 2. EventLoop接口
1. 一个`EventLoopGroup`包含一个或多个`EventLoop`;
2. 一个`EventLoop`在它的生命周期内只和一个`Thread`绑定;
3. 所有由`EventLoop`处理的I/O事件都将在它专有的`Thread`上被处理;
4. 一个`Channel`在它的生命周期内只注册于一个`EventLoop`;
5. 一个`EventLoop`可能会被分配给一个或多个`Channel`

## 3. ChannelFuture接口
上面解释过了, 通过这个接口实现异步, 更详细的后面会讲.

## 4. ChannelHandler和ChannelPipline
### 1. ChannelHandler接口
网络事件触发时调用该接口的方法, 主要通过重写这类子接口(例如`ChannelInboundHandler`)的方法，来写我们的业务逻辑. 
- ChannelHandler的典型用途包括:
    1. 将数据从一种格式转换为另一种格式(decoder, encoder)
    2. 提供异常的通知
    3. 提供Channel变为活动的或者非活动的通知
    4. 提供当CHannel注册到EventLoop或者从EventLoop注销时的通知;
    4. 提供有关用户自定义事件的通知

### 2. ChannelPipeline接口
此接口是`ChannelHandler`链的容器 , Channel被创建时，会被放到专属的`ChannelPipeline`中. 下面说一下 `ChannelHandler`被注册到`ChannelPipeline`的过程:
1. 一个`ChannelInitializer`的实例被注册到`ServerBootstrap中`;
2. 通过调用`ChannelInitializer`的`initChannel()`方法来在`ChannelPipline`中安装一组自定义的`ChannelHandler`;
3.  `ChannelInitializer`讲自己从`ChannelPipline`中移除;

![channelpipline](https://github.com/solthx/netty-note/blob/master/pic/channelPipline.png)

> netty提供了抽象基类 `ChannelInboundHandlerAdapter` 和 `ChannelOutboundHandlerAdapter` 来提供过滤掉不感兴趣事件的手段. 其中,  `ChannelHandlerContext`上的对应方法都提供了将事件传递给下一个ChannelHandler的方法.

<br>
<br>
<br>

**在Netty中，有两种发送消息的方式:**

1. 直接写到Channel中.
    - 这种方式会导致发送的消息直接从Pipline的尾部开始流动。(依次经过`ChannelOutboundHandler`)
2. 写到和`ChannelHandler`关联的`ChannelHandlerContext`中. 
    - 这种方式会导致发送的消息从下一个`ChannelHandler`开始流动..

ps: 当ChannelHandler被注册到Pipline的时候，会为其分配一个`CHannelHandlerContext`, 代表了`ChannelHandler`和`ChannelPipline`之间的绑定. 这个对象可以获取底层的Channel，其主要被用于写出站数据.


# 4. 传输
## 1. Netty提供的传输

|  名称   | 包  |   描述   |  应用 |
|  ----  | ----  |  --  | --  |
| NIO  | `io.netty.channel.socket.nio` |  使用`java.nio.channels`包作为基础——基于Selector方式   | 非阻塞代码库或一个常规的起点 |
| Epoll  | `io.netty.channel.epoll` |   由JNI驱动的`epoll()`和非阻塞IO。 这个传输支持 只有在Linux上可用的多种特性, 例如 SO_REUSEPORT,比NIO传输更快，并且是非阻塞的    | 非阻塞代码库或一个常规的起点(且在linux系统上)
|  OIO |  `io.netty.channel.socket.io` | 使用`java.net`包作为基础——使用阻塞流 | 阻塞代码库|
| Local| `io.netty.channel.local` | 可以在VM内部通过管道进行通信的**本地传输**|在同一个JVM内进行通信|
| Embedded | `io.netty.channel.embedded`| Embedded传输, 允许使用ChannelHandler而又不需要一个真正的基于网络的传输. 在测试`ChannelHandler`的实现时非常有用 | 进行`ChannelHandler`的测试|




