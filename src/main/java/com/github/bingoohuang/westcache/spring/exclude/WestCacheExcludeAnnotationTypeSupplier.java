package com.github.bingoohuang.westcache.spring.exclude;

import java.lang.annotation.Annotation;
import java.util.List;

public interface WestCacheExcludeAnnotationTypeSupplier {

    List<Class<? extends Annotation>> get();
}
