package org.mcservice.javafx;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Pattern.Flag;


import javafx.scene.control.TextFormatter;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.IntegerStringConverter;

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
			org.mcservice.javafx.table.TableViewConverter converterClass=
					field.getAnnotation(org.mcservice.javafx.table.TableViewConverter.class);
			if (converterClass != null) {
				return (StringConverter<V>) converterClass.converter().getConstructor().newInstance();
			}
		} catch (Throwable e) {
			ClassCastException loc = new ClassCastException("Casting or creating annotatated converter failed.");
			loc.initCause(e);
			throw loc;
		}

		if(field.getType()==String.class) {
			return (StringConverter<V>) new DefaultStringConverter();
		}
		if(field.getType()==Integer.class || field.getType()==Integer.TYPE) {
			return (StringConverter<V>) new IntegerStringConverter();
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
			} else if(field.getType()==Integer.class || field.getType()==Integer.TYPE) {
				p = Pattern.compile("[1-9][0-9]{0,}");
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

