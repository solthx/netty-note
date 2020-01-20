# 实现一个Echo服务器和一个Echo客户端
## 1. Echo服务器
### 1. 至少包括：

1. 一个ChannelHandler. 
2. BootStrap引导程序

### 2. 主要代码组件:

1. EchoServerHandler实现了业务逻辑(把接受到的信息原封不动的传回去)
2. main()方法引导(BootStrap)了服务器.

在引导过程中需要的步骤如下:

1. 创建一个ServerBootStrap的实例以引导和绑定服务器;
2. 创建并分配一个NioEventLoopGroup实例以进行事件的处理，如接受新链接以及读/写数据;
3. 指定服务器绑定的本地`InetSocketAddress`;
4. 使用一个EchoServerHandler的实例初始化每一个新的Channel(加到childHander的pipline里去) ;
5. 调用ServerBootStrap实例的bind()方法绑定服务器.

## 2. Echo客户端

### 1. 客户端做的事:

1. 连接到服务器;
2. 发送一个或多个消息;
3. 对于每个消息，等待并接受才能够服务器发回的相同消息;
4. 关闭连接;

### 2. 主要代码构建:
1. 为了初始化客户端，创建一个BootStrap的实例;
2. 为事件处理分配了一个`NioEventLoopGroup`实例，其中事件处理包括创建新的连接以及处理入站和出站数据;
3. 为连接服务器创建了一个InetSocketAddress实例(键入服务器的ip与端口号);
4. 连接建立时， 一个EchoClientHandler实例会被安装到`ChannelPipline`中;
5. 设置完成后，通过connect()连接远程服务器.
