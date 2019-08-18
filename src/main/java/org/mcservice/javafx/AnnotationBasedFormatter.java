package org.mcservice.javafx;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.money.MonetaryAmount;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import javax.money.format.MonetaryParseException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.Pattern.Flag;

import org.javamoney.moneta.Money;
import org.mcservice.javafx.control.table.DefaultTableMonetaryAmountConverter;
import org.mcservice.javafx.control.table.TableViewFinalIfNotNull;

import javafx.scene.control.TextFormatter;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LongStringConverter;
import javafx.util.converter.ShortStringConverter;

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
	
	public Callback<S, Boolean> getEditableCallback(){
		if(!field.isAnnotationPresent(TableViewFinalIfNotNull.class)){
			return null;
		}
		return val -> {
			try {
				return null==val ? true : null==fieldClass.getMethod(field.getAnnotation(TableViewFinalIfNotNull.class).value()).invoke(val);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e); //If this ever happens it is an implementation error
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	protected static <S,V> StringConverter<V> createConverter(Field field,Class<S> baseClass) {
		try {
			org.mcservice.javafx.control.table.TableViewConverter converterClass=
					field.getAnnotation(org.mcservice.javafx.control.table.TableViewConverter.class);
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
		} else if(field.getType()==Short.class || field.getType()==Short.TYPE) {
			return (StringConverter<V>) new ShortStringConverter();
		} else if(field.getType()==Integer.class || field.getType()==Integer.TYPE) {
			return (StringConverter<V>) new IntegerStringConverter();
		} else if(field.getType()==Long.class || field.getType()==Long.TYPE) {
			return (StringConverter<V>) new LongStringConverter();
		} else if(field.getType()==MonetaryAmount.class || field.getType()==Money.class) {
			return (StringConverter<V>) new DefaultTableMonetaryAmountConverter();
		}			
		throw new RuntimeException("No converter for this class yet implemented.");
	}
	
	protected static class AnnotationBasedFilter extends BaseMatcherCallbackFilter{
		
		protected AnnotationBasedFilter(Field field) {
			super(null);
			Pattern p = null;
			List<String> completions = null;
			
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
				completions=List.of("0","1","2","8","9");
			} else if(field.getType()==MonetaryAmount.class || field.getType()==Money.class) {
				p = Pattern.compile("(([1-9][0-9]{0,2}([\\. ][0-9]{3}){0,})||0)(,[0-9]{0,2})?( ([A-Z]{3}||\\p{Sc}))?");
				completions=List.of("0","1","2","8","9"," ",".",",","€","$","A","B","Y","Z");
			}
			
			if(null!=p) {
				AdvancedMatcher matcher=new AdvancedMatcher(p);
				if(null!=completions) {
					matcher.setCompletions(completions);
				}
				setMatcher(matcher);
			}
		}		
	}

}

