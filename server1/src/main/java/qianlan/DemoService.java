package qianlan;

import org.springframework.transaction.annotation.Transactional;
import qianlan.annotation.GlobalTransaction;
import qianlan.util.HttpClient;

import javax.annotation.Resource;

public class DemoService {

    @Resource
    private DemaoDao demaoDao;

    @Transactional
    @GlobalTransaction(isStart = true )
    public void test() {
        demaoDao.insert("server1");
        HttpClient.doExecute("http://localhsot:8082/server2/test");
    }


}
