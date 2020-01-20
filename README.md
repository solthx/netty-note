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
Channel是对socket的抽象 , socket的基本操作有: `bind()`, `connect()`, `read()`, `write()`. 预定义的常用Channel:
1. `EmbeddedChannel`;
2. `LocalServerChannel`;
3. `NioDatagramChannel`;
4. `NioSctpChannel`;
5. `NioSocketChannel`;

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

### 2. ChannelPipeline接口
此接口是`ChannelHandler`链的容器 , Channel被创建时，会被放到专属的`ChannelPipeline`中. 下面说一下 `ChannelHandler`被注册到`ChannelPipeline`的过程:
1. 一个`ChannelInitializer`的实例被注册到`ServerBootstrap中`;
2. 通过调用`ChannelInitializer`的`initChannel()`方法来在`ChannelPipline`中安装一组自定义的`ChannelHandler`;
3.  `ChannelInitializer`讲自己从`ChannelPipline`中移除;