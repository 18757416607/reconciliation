package com.reconciliation.task;

import com.reconciliation.pojo.Config;
import com.reconciliation.util.DateUtils;
import com.reconciliation.util.UrlConnectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by Administrator on 2018/3/22.
 * 招商任务
 */
@Component
public class ZsJob {

    @Autowired
    private Config config;

    private final static Logger logger = LoggerFactory.getLogger(ZsJob.class);

    /**
     *
     * @throws Exception
     */
    @Scheduled(cron="0 10 1 * * ?")
    public void zsJob() throws  Exception{
        logger.info("获取招商对账单定时任务执行中");
        String result = UrlConnectUtil.postRtnStr("http://api.cabin-app.com/duizhang/cmbduizhang.php?date="+ DateUtils.formatYYYYMMDD(DateUtils.addOneDay(new Date(),-1)), "");
        logger.info("获取招商对账单定时任务执行完毕->"+result);
    }

}
