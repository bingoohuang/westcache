package com.github.bingoohuang.westcache.utils;

import com.github.bingoohuang.westcache.cglib.Cglibs;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.regex.PatternSyntaxException;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/30.
 */
@UtilityClass @Slf4j
public class Keys {
    public static String joinArgs(Object[] args) {
        val argsPart = new StringBuilder();

        for (val arg : args) {
            argsPart.append("_").append(arg == null ? "null" : arg);
        }

        return argsPart.toString();
    }

    public static String removePackage(String className) {
        int lastDotPos = className.lastIndexOf('.');
        if (lastDotPos == -1) return className;
        return className.substring(lastDotPos + 1);
    }

    public static boolean matchRegex(String str, String regex) {
        try {
            return str.matches(regex);
        } catch (PatternSyntaxException e) {
            log.error("regex syntax error : {}", regex);
            return false;
        }
    }

    public static boolean isPrefix(String str, String prefix) {
        if (!str.startsWith(prefix)) return false;
        if (str.length() == prefix.length()) return true;

        char nextChar = str.charAt(prefix.length());
        return nextChar == '.' || nextChar == '_';
    }

    public static String createKeyMainPart(String methodName,
                                           Object bean,
                                           boolean removePackage) {
        val superClass = Cglibs.getSuperClassName(bean);
        val simpleName = removePackage ? removePackage(superClass) : superClass;
        val dottedClassName = simpleName.replace('$', '.');
        return dottedClassName + "." + methodName;
    }

}
