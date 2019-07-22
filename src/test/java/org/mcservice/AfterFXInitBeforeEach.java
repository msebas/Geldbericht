package org.mcservice;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * {@code @AfterFXinitBeforeEach} is used to split JUnit {@code @BeforeEach}
 * and should behave like it. It is called after the Initialization of the 
 * application, so FX components are available during methods marked this way.
 * 
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AfterFXInitBeforeEach {

}
