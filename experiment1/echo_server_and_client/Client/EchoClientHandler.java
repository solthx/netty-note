package chapter1.echo_server_and_client.Client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;

@ChannelHandler.Sharable
public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        /**
         *  当被通知Channel是活跃（active）的时候，发送一条信息
         *
         *  在建立连接的时候调用!
         */
        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8));
    }

    @Override
    public void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        /**
         *  每当接受数据的时候，会调用这个方法. 因为是分块接受，因此即使对于少量的数据，
         *  这个方法也可能会被调用多次.
         */
        System.out.println(
                "Client received: " + byteBuf.toString(CharsetUtil.UTF_8)
        );
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
