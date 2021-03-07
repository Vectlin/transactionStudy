package qianlan.netty;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.core.annotation.Order;
import qianlan.transactional.GlobalTransactionManager;
import qianlan.transactional.QLtransaction;
import qianlan.transactional.TransactionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private ChannelHandlerContext channelHandlerContext;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        channelHandlerContext = ctx;
    }

    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("接受数据：" + msg.toString());
        JSONObject jsonObject = JSON.parseObject((String) msg);
        String command = jsonObject.getString("command"); // create-开启全局事务，regist-注册分支事务，commit-提交全局事务
        String groupId = jsonObject.getString("groupId"); // 事务组id

        System.out.println("接收command:" + command);
        // 对事务进行操作
        QLtransaction qLtransaction =GlobalTransactionManager.getQLTransaction(groupId);
        if ("commit".equals(command)) {
            qLtransaction.setTransactionType(TransactionType.commit);
        } else {
            qLtransaction.setTransactionType(TransactionType.rollback);
        }

        qLtransaction.getTask().signalTask();
    }

    public synchronized Object call(JSONObject data) throws Exception {
        channelHandlerContext.writeAndFlush(data.toJSONString()).channel().newPromise();
        return null;
    }
}
