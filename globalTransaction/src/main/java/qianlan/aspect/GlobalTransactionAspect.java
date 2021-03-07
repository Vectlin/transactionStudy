package qianlan.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import qianlan.annotation.GlobalTransaction;
import qianlan.transactional.GlobalTransactionManager;
import qianlan.transactional.QLtransaction;
import qianlan.transactional.TransactionType;

import java.lang.reflect.Method;

@Aspect
@Component
public class GlobalTransactionAspect implements Ordered {

    @Around("@annotation(qianlan.annotation.GlobalTransaction)")
    public void invoke(ProceedingJoinPoint point){

        //before
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        GlobalTransaction globalTransaction = method.getAnnotation(GlobalTransaction.class);

        String groupId = null;
        if (globalTransaction.isStart()) {
            groupId = GlobalTransactionManager.getOrCreateGroup(); //XID
        }

        // 分支事务
        QLtransaction qLtransaction = GlobalTransactionManager.createQLTransaction(groupId);

        try {
            point.proceed();
            qLtransaction.setTransactionType(TransactionType.commit);
        } catch (Throwable throwable) {
            qLtransaction.setTransactionType(TransactionType.rollback);
            throwable.printStackTrace();
        }

        //after
        //注册
        GlobalTransactionManager.addQLTransaction(qLtransaction);

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
