package com.github.bingoohuang.westcache.batch;

import com.github.bingoohuang.westcache.WestCacheable;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static java.util.concurrent.Executors.newScheduledThreadPool;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/20.
 */
public class BatchService {
    private Batcher<String, String> batcher = BatcherBuilder.newBuilder(
            new BatcherJob<String, String>() {
                @Override
                public List<String> doBatchJob(List<String> batchArgs) {
                    val results = new ArrayList<String>(batchArgs.size());
                    for (String arg : batchArgs) {
                        val result = RandomStringUtils.randomAlphanumeric(20);
                        System.out.println(result);
                        results.add(result);
                    }

                    return results;
                }
            })
            .executor(newScheduledThreadPool(10))
            .maxWaitItems(3) // 达到3个就开工
            .maxWaitMillis(1000) // 或者累计满1秒钟也开工
            .maxBatchNum(10) // 一批最多10个
            .build();

    @WestCacheable(manager = "expiring", specs = "expireAfterWrite=2s")
    public Future<String> getToken(String tokenId) {
        return batcher.submit(tokenId);
    }


    private Batcher<String, String> batcher2 = BatcherBuilder.newBuilder(
            new BatcherJob<String, String>() {
                @Override
                public List<String> doBatchJob(List<String> batchArgs) {
                    val results = new ArrayList<String>(batchArgs.size());
                    for (String arg : batchArgs) {
                        val result = RandomStringUtils.randomAlphanumeric(20);
                        System.out.println(result);
                        results.add(result);
                    }

                    if (batchArgs.get(0).equals("bad")) {
                        results.remove(0);
                    }

                    return results;
                }
            })
            .maxWaitItems(3) // 达到3个就开工
            .maxWaitMillis(1000) // 或者累计满1秒钟也开工
            .maxBatchNum(10) // 一批最多10个
            .build();

    @WestCacheable(manager = "expiring", specs = "expireAfterWrite=2s")
    public Future<String> getToken2(String tokenId) {
        return batcher2.submit(tokenId);
    }


    private Batcher<String, String> batcher3 = BatcherBuilder.newBuilder(
            new BatcherJob<String, String>() {
                @Override
                public List<String> doBatchJob(List<String> batchArgs) {
                    val results = new ArrayList<String>(batchArgs.size());
                    for (String arg : batchArgs) {
                        val result = RandomStringUtils.randomAlphanumeric(20);
                        System.out.println(result);
                        results.add(result);
                    }

                    results.remove(0);

                    return results;
                }
            })
            .maxWaitItems(3) // 达到3个就开工
            .maxWaitMillis(1000) // 或者累计满1秒钟也开工
            .maxBatchNum(10) // 一批最多10个
            .build();

    @WestCacheable(manager = "expiring", specs = "expireAfterWrite=2s")
    public Future<String> getToken3(String tokenId) {
        return batcher3.submit(tokenId);
    }

    private Batcher<String, String> batcher4 = BatcherBuilder.newBuilder(
            new BatcherJob<String, String>() {
                @Override
                public List<String> doBatchJob(List<String> batchArgs) {
                    throw new RuntimeException("dingoo here");
                }
            })
            .maxWaitItems(3) // 达到3个就开工
            .maxWaitMillis(1000) // 或者累计满1秒钟也开工
            .maxBatchNum(10) // 一批最多10个
            .build();

    @WestCacheable(manager = "expiring", specs = "expireAfterWrite=2s")
    public Future<String> getToken4(String tokenId) {
        return batcher4.submit(tokenId);
    }
}
