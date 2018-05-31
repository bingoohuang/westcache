package com.github.bingoohuang.westcache.utils;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/30.
 */
@UtilityClass
public class Specs {
    private static final Splitter.MapSplitter SPECS_SPLITTER = Splitter.on(',').withKeyValueSeparator('=');

    public static Map<String, String> parseSpecs(String specs) {
        if (StringUtils.isEmpty(specs)) return Maps.newHashMap();

        return Maps.newHashMap(SPECS_SPLITTER.split(specs));
    }
}
