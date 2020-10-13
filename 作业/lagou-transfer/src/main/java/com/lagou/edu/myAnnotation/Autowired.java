package com.lagou.edu.myAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {
}
