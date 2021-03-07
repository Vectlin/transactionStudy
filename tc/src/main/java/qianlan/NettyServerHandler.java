package qianlan;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static Map<String, List<String>> transactionIdMap = new HashMap<>();

    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("接受数据：" + msg.toString());
        JSONObject jsonObject = JSON.parseObject((String) msg);
        String command = jsonObject.getString("command"); // create-开启全局事务，regist-注册分支事务，commit-提交全局事务
        String groupId = jsonObject.getString("groupId"); // 事务组id
        String transactionType = jsonObject.getString("transactionType"); // 分支事务类型,commit一待提交,rollback-待回滚
        String transactionId = jsonObject.getString("transactionId"); // 分支事务ID

        if ("create".equals(command)) {
            // 1.开启全局事务
            transactionIdMap.put(groupId, new ArrayList<String>());
        } else if ("regist".equals(command)) {
            // 2.注册分支事务
            transactionIdMap.get(groupId).add(transactionId);
            if("rollback".equals(transactionType)) {
                // 3.注册过程中发现有事务要回滚
                System.out.println("接收到了一个回滚状态");
                sendMsg(groupId, "rollback");
            }
        } else if ("commit".equals(command)) {
            System.out.println("全局事务提交");
            sendMsg(groupId, "commit");
        }

    }


    private void sendMsg(String groupId, String command) {
        // 调用rpc发送指令给客户端
        return;
    }
}
