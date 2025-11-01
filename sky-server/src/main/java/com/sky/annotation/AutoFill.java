package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * 自定义注解：用于标记需要自动填充字段的方法
 */
@Target(ElementType.METHOD)//作用于方法
@Retention(RetentionPolicy.RUNTIME)//运行时注解
public @interface AutoFill {
    OperationType value();//操作类型

}
