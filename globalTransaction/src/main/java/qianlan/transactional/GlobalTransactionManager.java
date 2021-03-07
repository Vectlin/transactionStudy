package qianlan.transactional;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import qianlan.annotation.GlobalTransaction;
import qianlan.netty.NettyClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GlobalTransactionManager {

    private static NettyClient nettyClient;

    private static ThreadLocal<QLtransaction> current = new ThreadLocal<>();
    private static ThreadLocal<String> currentGroupId = new ThreadLocal<>();

    @Autowired
    public void setNettyClient(NettyClient nettyClient) {
        GlobalTransactionManager.nettyClient = nettyClient;
    }

    public static Map<String, QLtransaction> QLJTRANSACION_MAP = new HashMap<>();


    /**
     *创建事务组，并且返回grouped
     *
     */
    public static String getOrCreateGroup() {
        if (currentGroupId.get() != null) {
            return currentGroupId.get();
        }
        String groupId = UUID.randomUUID().toString();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("groupId", groupId);
        jsonObject.put("command", "create");
        nettyClient.send(jsonObject);
        currentGroupId.set(groupId);

        System.out.println("创建事务组");
        return groupId;
    }

    /**
     * 创建分支事务
     * @param groupId
     * @return
     */
    public static QLtransaction createQLTransaction(String groupId) {
        String transactionId = UUID.randomUUID().toString();
        QLtransaction qLtransaction = new QLtransaction(groupId, transactionId);
        QLJTRANSACION_MAP.put(groupId, qLtransaction);
        current.set(qLtransaction);

        System.out.println("创建事务");
        return qLtransaction;
    }

    /**
     * 注册分支事务
     * @param qLtransaction
     * @return
     */
    public static QLtransaction addQLTransaction(QLtransaction qLtransaction) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", "regist");
        jsonObject.put("groupId", qLtransaction.getGroupId());
        jsonObject.put("transactionId", qLtransaction.getTransactionId());
        jsonObject.put("transactionType", qLtransaction.getTransactionType());
        nettyClient.send(jsonObject);

        System.out.println("添加事务");
        return qLtransaction;
    }

    public static void commitGlobalTransaction(String groupId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("groupId", groupId);
        jsonObject.put("command", "commit");
        nettyClient.send(jsonObject);

        System.out.println("提交全局事务");
    }

    public static QLtransaction getQLTransaction(String groupId) {
        return QLJTRANSACION_MAP.get(groupId);
    }
}
