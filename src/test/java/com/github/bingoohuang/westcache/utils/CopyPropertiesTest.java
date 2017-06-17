package com.github.bingoohuang.westcache.utils;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class CopyPropertiesTest {
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class SourceBean {
        private String a;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class TargetBean {
        private String a;
    }

    @Test
    public void copyProperties() {
        val sourceBean = new SourceBean("bingoo");
        val sourceJSON = JSON.toJSONString(sourceBean);
        val targetBean = JSON.parseObject(sourceJSON, TargetBean.class);
        assertThat(targetBean).isEqualTo(new TargetBean("bingoo"));
    }
}
