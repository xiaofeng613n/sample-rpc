package com.rpc.annotation;

import java.lang.annotation.*;

/**
 * Created by xiao on 2017/8/24.
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface API
{
	String value();
}