package com.github.bingoohuang.westcache.utils;

import com.google.common.collect.Lists;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class BlackListClass {
    public static final List<String> blacklists = parseBlackLists("westcache.blacklist");


    private static List<String> parseBlackLists(String configClassPathFile) {
        List<String> configFiles = Envs.loadClasspathResources(configClassPathFile,
                BlackListClass.class.getClassLoader());
        List<String> result = Lists.newArrayList();
        for (String configFile : configFiles) {
            String[] lines = configFile.split("\\r?\\n");
            for (String line : lines) {
                if (StringUtils.isBlank(line)) continue;

                String trim = line.trim();
                if (trim.startsWith("#")) continue; // ignore comment
                result.add(trim);
            }
        }

        return result;
    }


    public static boolean inBlackList(String targetClassName) {
        for (val blacklist : blacklists) {
            if (AntPathMatcher.match(blacklist, targetClassName, "."))
                return true;
        }

        return false;
    }
}
