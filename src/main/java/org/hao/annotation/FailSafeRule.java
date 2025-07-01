package org.hao.annotation;


import org.hao.core.failsafe.DemoFailSafeHandler;
import org.hao.core.failsafe.FailSafeHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FailSafeRule {
    Class<? extends FailSafeHandler> handler() ;//default DemoFailSafeHandler.class;
}
