package org.mcservice.geldbericht.data;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.stream.Stream;

import javax.money.MonetaryAmount;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class VatTypeTest {

	static Stream<Arguments> getSettersGetters() throws Exception{
		ArrayList<Arguments> result=new ArrayList<Arguments>();
		Class<VatType> clazz=VatType.class;
		String fields[]= {"name", "shortName", "value", "defaultVatType", "disabledVatType"};
		for(String fieldName : fields) {
			Field field = clazz.getDeclaredField(fieldName);
			String upperFieldName=field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
			Method setter,getter;
			Object val1=null,cval1=null,val2=null;
			setter=clazz.getMethod("set"+upperFieldName, field.getType());
			try {
				getter=clazz.getMethod("get"+upperFieldName);
			} catch (NoSuchMethodException e) {
				getter=clazz.getMethod("is"+upperFieldName);
			}
			boolean updateChanged=fieldName.equals("value");
			if(field.getType()==String.class) {
				val1="Str1";
				val2="Str2";
			} else if(field.getType()==MonetaryAmount.class) {
				val1=Money.of(1.2, "EUR");
				val2=Money.of(3.4, "EUR");
			} else if(field.getType()==Boolean.class || field.getType()==Boolean.TYPE) {
				val1=Boolean.valueOf(true);
				val2=Boolean.valueOf(false);
			} else if(field.getType()==BigDecimal.class) {
				val1=BigDecimal.valueOf(0);
				val2=BigDecimal.valueOf(1);
			} else {
				assertTrue(false,"Not implemented yet");
			}
			result.add(Arguments.of(field,setter,getter,val1,cval1,val2,updateChanged));
		}
        return result.stream();
    }
    
    @ParameterizedTest
    @MethodSource("getSettersGetters")
    public void checkSetterAndEquals(Field field,Method setter, Method getter, 
    		Object val1, Object cval1, Object val2, boolean updateChanged) throws Exception {
    	VatType tstObj1=new VatType(null, ZonedDateTime.now(), null,null,null,null,null);
    	VatType tstObj2=new VatType(null, ZonedDateTime.now().minusMinutes(4), null,null,null,null,null);
    	assertFalse(tstObj1.equals(tstObj2));
    	tstObj2=new VatType(tstObj1);
    	
    	assertTrue(tstObj1.equals(tstObj2));
    	assertEquals(tstObj1.hashCode(),tstObj2.hashCode());
    	
    	assertFalse(val1==getter.invoke(tstObj1));
    	ZonedDateTime act=tstObj1.getLastChange();
    	setter.invoke(tstObj1, val1);
    	assertTrue(val1.equals(getter.invoke(tstObj1)));
    	if(updateChanged) {
    		assertTrue(act.isBefore(tstObj1.getLastChange()));
    		assertTrue(ZonedDateTime.now().isAfter(tstObj1.getLastChange()));
    		tstObj2=new VatType(tstObj1);
    		field.setAccessible(true);
    		field.set(tstObj2, null);
    	} else {
    		assertEquals(act,tstObj1.getLastChange());
    	}
    	act=tstObj1.getLastChange();
    	setter.invoke(tstObj1, val1);
    	assertTrue(val1.equals(getter.invoke(tstObj1)));
    	assertEquals(act,tstObj1.getLastChange());
    	assertFalse(tstObj2.equals(tstObj1));
    	assertFalse(tstObj1.equals(tstObj2));
    	if(updateChanged) {
    		field.set(tstObj2, val1);
    		field.setAccessible(false);
    	} else {
    		setter.invoke(tstObj2, val1);
    	}
    	assertTrue(tstObj2.equals(tstObj1));
    	assertTrue(tstObj1.equals(tstObj2));
    	assertEquals(tstObj1.hashCode(),tstObj2.hashCode());
    	if(updateChanged) {
    		field.setAccessible(true);
    		field.set(tstObj1, val2);
    		field.setAccessible(false);
    	} else {
    		setter.invoke(tstObj1, val2);
    	}    	
    	assertFalse(tstObj2.equals(tstObj1));
    	assertFalse(tstObj1.equals(tstObj2));
    }
    
    @SuppressWarnings("unlikely-arg-type")
	@Test
    public void checkEqualsSuper() throws Exception {
    	ZonedDateTime act=ZonedDateTime.now();
    	VatType tstObj1=new VatType(1L, act, null,null,null,null,null);
    	VatType tstObj2=new VatType(null, act, null,null,null,null,null); 
    	VatType tstObj3=new VatType(2L, act, null,null,null,null,null);
    	VatType tstObj4=new VatType(1L, act, null,null,null,null,null);
    	VatType tstObj5=new VatType(1L, act.minusMinutes(4), null,null,null,null,null); 
    	
    	assertTrue(tstObj1.equals(tstObj1));
    	assertTrue(tstObj1.equals(tstObj4));
    	assertFalse(tstObj1.equals(tstObj5));
    	assertFalse(tstObj1.equals(Integer.valueOf(0)));
    	assertFalse(tstObj1.equals(null));
    	assertFalse(tstObj1.equals(tstObj2));
    	assertFalse(tstObj2.equals(tstObj1));
    	assertFalse(tstObj1.equals(tstObj3));
	}
    
    @Test
    public void checkSetValueException() {
    	VatType tstObj=new VatType(1L, ZonedDateTime.now(), null,null,null,null,null);
    	assertThrows(RuntimeException.class, ()->{ tstObj.setValue(BigDecimal.valueOf(0));});
    }
    
}
