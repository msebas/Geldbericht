package org.mcservice.javafx.control.table;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.mcservice.javafx.control.table.factories.DefaultReflectionColumnFactory;
import org.mcservice.javafx.control.table.factories.ReflectionColumnFactory;

@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface TableViewColumn {
	String colName() default "";
	boolean editable() default true;
	String pattern() default "";
	@SuppressWarnings("rawtypes")
	Class<? extends ReflectionColumnFactory> fieldGenerator() default DefaultReflectionColumnFactory.class;
}
