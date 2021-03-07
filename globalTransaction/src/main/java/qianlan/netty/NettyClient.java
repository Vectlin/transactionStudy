package qianlan.netty;

import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NettyClient implements InitializingBean {
    public NettyClientHandler client = null;

    private static ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void afterPropertiesSet() throws Exception {
        start("localhost", 8080);
    }

    public void start(String hostName, Integer port) {
        client = new NettyClientHandler();
        Bootstrap b = new Bootstrap();
        EventLoopGroup group = new NioEventLoopGroup();
        b.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("decoder", new StringDecoder());
                        pipeline.addLast("encoder", new StringEncoder());
                        pipeline.addLast("handler", client);
                    }
                });
        try {
            b.connect(hostName, port).sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(JSONObject jsonObject) {
        try {
            client.call(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
