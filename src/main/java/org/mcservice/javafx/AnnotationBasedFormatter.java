package org.mcservice.javafx;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Pattern.Flag;


import javafx.scene.control.TextFormatter;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

public class AnnotationBasedFormatter<S,V> extends TextFormatter<V> {

	private AnnotationBasedFilter filter = null;
	protected static Validator validator=Validation.buildDefaultValidatorFactory().getValidator();
	protected Field field;
	protected Class<S> fieldClass;
	
	public AnnotationBasedFormatter(Field field,Class<S> fieldClass,V defaultValue) {
		super(createConverter(field,fieldClass),
				defaultValue,
				new AnnotationBasedFilter(field));
		this.filter=(AnnotationBasedFilter) super.getFilter();
		this.field=field;
		this.fieldClass=fieldClass;
	}
	
	public void setCallback(Consumer<String> callback) {
		this.filter.setEndEditCallback(callback);
	}
	
	public Callback<V, Boolean> getVerificator(){
		return (V item) -> validator.validateValue(this.fieldClass, this.field.getName(),item).isEmpty();
	}
	
	@SuppressWarnings("unchecked")
	protected static <S,V> StringConverter<V> createConverter(Field field,Class<S> baseClass) {
		try {
			if(field.getType()==String.class) {
				org.mcservice.javafx.Converter converterClass=
						field.getAnnotation(org.mcservice.javafx.Converter.class);
				if (converterClass != null) {
					return (StringConverter<V>) converterClass.converter().getConstructor().newInstance();
				}
				return (StringConverter<V>) new DefaultStringConverter();
			} 
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Casting annotation failed.");
		}	
		throw new RuntimeException("No converter for this class yet implemented.");
	}
	
	protected static class AnnotationBasedFilter implements UnaryOperator<Change>{
		private AdvancedMatcher matcher = null;
		private Consumer<String> endEditCallback = null;
		
		protected AnnotationBasedFilter(Field field) {
			Pattern p = null;
			
			if(field.getAnnotation(javax.validation.constraints.Pattern.class) != null) {
				Flag[] flags=field.getAnnotation(javax.validation.constraints.Pattern.class).flags();
				int flagsInt=0;
				for(Flag flag:flags) {
					flagsInt=flagsInt | flag.getValue();
				}
				p = Pattern.compile(field.getAnnotation(javax.validation.constraints.Pattern.class).regexp(),flagsInt);
			} else if (field.getAnnotation(javax.validation.constraints.Size.class) != null) {
				javax.validation.constraints.Size constraint=field.getAnnotation(javax.validation.constraints.Size.class);
				p = Pattern.compile(String.format(".{%s,%s}",constraint.min(),constraint.max()));				
			}
			
			if(p!=null) {
				setMatcher(new AdvancedMatcher(p));
			}
		}
		
		@Override
		public Change apply(Change change) {
			if (null == this.matcher) {
				return change;
			}
			
			matcher.reset(change.getControlNewText());
			
			if(matcher.matches()) {
				if(matcher.requireEnd() && endEditCallback!=null) {
					endEditCallback.accept(change.getControlNewText());
				}
			}
			
			if(matcher.hitEnd() || matcher.matches()) {
				return change;
			}
			
			return null;
		}

		/**
		 * @param matcher the matcher to set
		 */
		public final void setMatcher(AdvancedMatcher matcher) {
			this.matcher = matcher;
		}

		/**
		 * @param endEditCallback the endEditCallback to set
		 */
		public final void setEndEditCallback(Consumer<String> endEditCallback) {
			this.endEditCallback = endEditCallback;
		}
		
	}

}

