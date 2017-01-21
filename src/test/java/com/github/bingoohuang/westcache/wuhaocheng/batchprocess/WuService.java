package com.github.bingoohuang.westcache.wuhaocheng.batchprocess;

import com.github.bingoohuang.westcache.WestCacheable;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static com.github.bingoohuang.westcache.wuhaocheng.batchprocess.BatchTaskManagerBuilder.newBuilder;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/20.
 */
public class WuService implements BatchTaskWorker<String, String> {
    BatchTaskManager<String, String> manager = newBuilder(this)
            .maxWaitItems(3) // 达到3个就开工
            .maxWaitMillis(1000) // 或者累计满1秒钟也开工
            .maxBatchNum(10) // 一批最多10个
            .build();

    @Override
    public List<String> doBatchTasks(List<String> batchArgs) {
        val results = new ArrayList<String>(batchArgs.size());
        for (String arg : batchArgs) {
            val result = RandomStringUtils.randomAlphanumeric(20);
            System.out.println(result);
            results.add(result);
        }

        return results;
    }

    @WestCacheable(manager = "expiring", specs = "expireAfterWrite=2s")
    public Future<String> getToken(String tokenId) {
        return manager.enroll(tokenId);
    }

}
