package chapter1.echo_server_and_client.Server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

//@Sharable 使得当前ChannelHandler可以被多个Channel安全共享
@ChannelHandler.Sharable
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    /**
     * channelRead —— 对每个传入的消息都调用该方法
     *
     * 当channel上面有数据到来时会触发channelRead事件，
     * 当数据到来时，eventLoop被唤醒继而调用channelRead方法处理数据。
     *
     * @param ctx
     * @param msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf)msg;
        System.out.println("Server receive: " + in.toString(CharsetUtil.UTF_8));
        ctx.write(in);
    }

    /**
     * eventLoop被到来的数据唤醒后read数据并包装成msg,然后将msg作为参数调用channelRead方法，
     * 期间做个判断，read到0个字节或者是read到的字节数小于buffer的容量，
     * 满足以上条件就会调用channelReadComplete方法。
     *
     * 因此，就是当channelRead()读完一批数据的时候，会调用该方法
     *
     * @param ctx
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx){
        // 将未决消息冲刷到远程结点.. 并关闭该Channel
        System.out.println("读取结束!");
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
