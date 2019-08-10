package org.mcservice.javafx.control.table;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface TableViewColumn {
	String colName() default "";
	boolean editable() default true;
	String pattern() default "";
}
