package com.github.bingoohuang.westcache.spring.exclude;

import com.github.bingoohuang.utils.lang.Clz;
import com.google.auto.service.AutoService;
import org.n3r.eql.eqler.annotations.Eqler;
import org.n3r.eql.eqler.annotations.EqlerConfig;

import java.lang.annotation.Annotation;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;

@AutoService(WestCacheExcludeAnnotationTypeSupplier.class)
public class ExcludeEqlerSupplier implements WestCacheExcludeAnnotationTypeSupplier {

    private static final boolean hasEqler;

    static {
        hasEqler = Clz.classExists("org.n3r.eql.eqler.annotations.Eqler");
    }

    @Override
    public List<Class<? extends Annotation>> get() {
        if (hasEqler) return newArrayList(Eqler.class, EqlerConfig.class);
        return emptyList();
    }
}
