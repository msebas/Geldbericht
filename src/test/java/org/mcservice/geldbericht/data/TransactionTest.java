package org.mcservice.geldbericht.data;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.stream.Stream;

import javax.money.MonetaryAmount;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TransactionTest {

	static Stream<Arguments> getSettersGetters() throws Exception{
		ArrayList<Arguments> result=new ArrayList<Arguments>();
		Class<Transaction> clazz=Transaction.class;
		String fields[]= {"number","receipts","spending","accountingContraAccount","accountingCostGroup",
				"accountingCostCenter","voucher","transactionDate","vat","inventoryNumber","descriptionOfTransaction"};
		for(String fieldName : fields) {
			Field field = clazz.getDeclaredField(fieldName);
			String upperFieldName=field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
			Method setter,getter;
			Object val1=null,cval1=null,val2=null;
			boolean updateChanged=!(upperFieldName.equals("Number"));
			setter=clazz.getMethod("set"+upperFieldName, field.getType());
			getter=clazz.getMethod("get"+upperFieldName);
			if(field.getType()==String.class) {
				val1="Str1";
				val2="Str2";
			} else if(field.getType()==MonetaryAmount.class) {
				val1=Money.of(1.2, "EUR");
				val2=Money.of(3.4, "EUR");
			} else if(field.getType()==Integer.class || field.getType()==Integer.TYPE) {
				val1=Integer.valueOf(4);
				val2=Integer.valueOf(0);
			} else if(field.getType()==LocalDate.class) {
				val1=LocalDate.now().minusDays(4);
				val2=LocalDate.now().minusDays(3);
			} else if(field.getType()==VatType.class) {
				val1=new VatType(1L,  ZonedDateTime.now(), "Name1", "Name2", BigDecimal.valueOf(0.12), false,false);
				val2=new VatType(2L,  ZonedDateTime.now(), "Name3", "Name4", BigDecimal.valueOf(0.13), false,false);
			} else {
				assertTrue(false,"Not implemented yet");
			}
			result.add(Arguments.of(setter,getter,val1,cval1,val2,updateChanged));
		}
        return result.stream();
    }
    
    @ParameterizedTest
    @MethodSource("getSettersGetters")
    public void checkSetterAndEquals(Method setter, Method getter, 
    		Object val1, Object cval1, Object val2, boolean updateChanged) throws Exception {
    	Transaction tstObj1=new Transaction(null, ZonedDateTime.now(), 0, 
    			null,null,null,null,null,null,null,null,null,null);
    	Transaction tstObj2=new Transaction(null, ZonedDateTime.now().minusMinutes(4), 0, 
    			null,null,null,null,null,null,null,null,null,null);
    	//Transactions should be equals even if there last reported change is different
    	assertTrue(tstObj1.equals(tstObj2));
    	//but there hash codes should not match
    	assertEquals(tstObj1.hashCode(),tstObj2.hashCode());
    	
    	assertFalse(val1==getter.invoke(tstObj1));
    	ZonedDateTime act=tstObj1.getLastChange();
    	setter.invoke(tstObj1, val1);
    	assertTrue(val1.equals(getter.invoke(tstObj1)));
    	if(updateChanged) {
    		assertTrue(act.isBefore(tstObj1.getLastChange()));
    		assertTrue(ZonedDateTime.now().isAfter(tstObj1.getLastChange()));
    	} else {
    		assertEquals(act,tstObj1.getLastChange());
    	}
    	act=tstObj1.getLastChange();
    	setter.invoke(tstObj1, val1);
    	assertTrue(val1.equals(getter.invoke(tstObj1)));
    	assertEquals(act,tstObj1.getLastChange());
    	assertFalse(tstObj2.equals(tstObj1));
    	assertFalse(tstObj1.equals(tstObj2));
    	setter.invoke(tstObj2, val1);
    	assertTrue(tstObj2.equals(tstObj1));
    	assertTrue(tstObj1.equals(tstObj2));
    	assertEquals(tstObj1.hashCode(),tstObj2.hashCode());
    	setter.invoke(tstObj1, val2);
    	assertFalse(tstObj2.equals(tstObj1));
    	assertFalse(tstObj1.equals(tstObj2));
    }
    
    @SuppressWarnings("unlikely-arg-type")
	@Test
    public void checkEqualsUid() throws Exception {
    	Transaction tstObj1=new Transaction(1L, ZonedDateTime.now(), 0, 
    			null,null,null,null,null,null,null,null,null,null);
    	Transaction tstObj2=new Transaction(null, ZonedDateTime.now().minusMinutes(4), 0, 
    			null,null,null,null,null,null,null,null,null,null);
    	Transaction tstObj3=new Transaction(2L, ZonedDateTime.now().minusMinutes(4), 0, 
    			null,null,null,null,null,null,null,null,null,null);
    	Transaction tstObj4=new Transaction(1L, ZonedDateTime.now().minusMinutes(4), 0, 
    			null,null,null,null,null,null,null,null,null,null);
    	
    	assertTrue(tstObj1.equals(tstObj1));
    	assertTrue(tstObj1.equals(tstObj4));
    	assertFalse(tstObj1.equals(Integer.valueOf(0)));
    	assertFalse(tstObj1.equals(null));
    	assertFalse(tstObj1.equals(tstObj2));
    	assertFalse(tstObj2.equals(tstObj1));
    	assertFalse(tstObj1.equals(tstObj3));
	}
    
}
