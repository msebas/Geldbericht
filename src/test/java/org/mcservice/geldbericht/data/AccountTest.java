/*******************************************************************************
 * Copyright (C) 2019 Sebastian Müller <sebastian.mueller@mcservice.de>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.mcservice.geldbericht.data;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.money.MonetaryAmount;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

class AccountTest {

	@SuppressWarnings("unchecked")
	static Stream<Arguments> getSettersGetters() throws Exception{
		ArrayList<Arguments> result=new ArrayList<Arguments>();
		Class<Account> clazz=Account.class;
		String fields[]= {"accountNumber","accountName","balance","initialBalance","company",
				"balanceMonths"};
		for(String fieldName : fields) {
			Field field = clazz.getDeclaredField(fieldName);
			String upperFieldName=field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
			Method setter,getter;
			Object val1=null,val2=null;
			setter=clazz.getMethod("set"+upperFieldName, field.getType());
			getter=clazz.getMethod("get"+upperFieldName);
			if(field.getType()==String.class) {
				val1="Str1";
				val2="Str2";
			} else if(field.getType()==MonetaryAmount.class) {
				val1=Money.of(1.2, "EUR");
				val2=Money.of(3.4, "EUR");
			} else if(field.getType()==Company.class) {
				val1=new Company(1L,  ZonedDateTime.now(), null, "Name1", "Name2", "Name3");
				val2=new Company(1L,  ZonedDateTime.now(), null, "Name4", "Name5", "Name6");
			} else if(field.getType()==List.class) {
				val1=new ArrayList<MonthAccountTurnover>(List.of(Mockito.mock(MonthAccountTurnover.class)));
				val2=new ArrayList<MonthAccountTurnover>();
				when(((ArrayList<MonthAccountTurnover>)val1).get(0).getLastChange()).thenAnswer(i -> ZonedDateTime.now());
				when(((ArrayList<MonthAccountTurnover>)val1).get(0).getMonthBalanceAssets()).thenReturn(Money.of(0, "EUR"));
				when(((ArrayList<MonthAccountTurnover>)val1).get(0).getMonthBalanceDebt()).thenReturn(Money.of(0, "EUR"));
			} else {
				fail(String.format("Type %s not implemented yet",field.getType().getName()));
			}
			result.add(Arguments.of(setter,getter,val1,val2,fieldName.equals("balanceMonths")));
		}
        return result.stream();
    }
    
    @ParameterizedTest
    @MethodSource("getSettersGetters")
    public void checkSetterAndEquals(Method setter, Method getter, 
    		Object val1, Object val2,boolean setNull) throws Exception {
    	ZonedDateTime act=ZonedDateTime.now();
    	Account tstObj1=new Account(null, act, null,null,val1 instanceof List ? Money.of(0,"EUR") : null ,null);
    	Account tstObj2=new Account(null, act, null,null,val1 instanceof List ? Money.of(0,"EUR") : null ,null);
    	if(setNull) {
    		setter.invoke(tstObj1,new Object[] {null});
    		setter.invoke(tstObj2,new Object[] {null});
    	}
    	//Transactions should be equals even if there last reported change is different
    	assertTrue(tstObj1.equals(tstObj2));
    	assertEquals(tstObj1.hashCode(),tstObj2.hashCode());
    	
    	assertFalse(val1==getter.invoke(tstObj1));
    	act=tstObj1.getLastChange();
    	setter.invoke(tstObj1, val1);
    	assertTrue(val1.equals(getter.invoke(tstObj1)));
		assertTrue(act.isBefore(tstObj1.getLastChange()));
		assertTrue(ZonedDateTime.now().isAfter(tstObj1.getLastChange()));
    	act=tstObj1.getLastChange();
    	setter.invoke(tstObj1, val1);
    	assertTrue(val1.equals(getter.invoke(tstObj1)));
    	assertEquals(act,tstObj1.getLastChange());
    	assertFalse(tstObj2.equals(tstObj1,true));
    	assertFalse(tstObj1.equals(tstObj2,true));
    	setter.invoke(tstObj2, val1);
    	assertTrue(tstObj2.equals(tstObj1,true));
    	assertTrue(tstObj1.equals(tstObj2,true));
    	assertEquals(tstObj1.hashCode(),tstObj1.hashCode());
    	setter.invoke(tstObj1, val2);
    	assertFalse(tstObj2.equals(tstObj1,true));
    	assertFalse(tstObj1.equals(tstObj2,true));
    }
    
    @Test
    public void checkEqualsBalance() throws Exception {
    	ZonedDateTime act=ZonedDateTime.now();
    	Account tstObj1=new Account(null, act, null, null, Money.of(0,"EUR"), null);
    	Account tstObj2=new Account(null, act, null, null, Money.of(0,"EUR"), null);
    	
    	Field field = Account.class.getDeclaredField("balance");
    	field.setAccessible(true);
    	
    	assertTrue(tstObj1.equals(tstObj2, false));
    	field.set(tstObj1, Money.of(1, "EUR"));
    	assertFalse(tstObj1.equals(tstObj2, false));
    	field.set(tstObj2, null);
    	assertFalse(tstObj1.equals(tstObj2, false));
    	assertFalse(tstObj2.equals(tstObj1, false));
    	field.set(tstObj1, null);
    	assertTrue(tstObj1.equals(tstObj2, false));
    }
    
    @Test
    public void checkEqualsInitialBalance() throws Exception {
    	ZonedDateTime act=ZonedDateTime.now();
    	Account tstObj1=new Account(null, act, null, null, Money.of(0,"EUR"), null);
    	Account tstObj2=new Account(null, act, null, null, Money.of(0,"EUR"), null);
    	
    	//To prevent the setter from changing the value of the balance field
    	Field field = Account.class.getDeclaredField("initialBalance");
    	field.setAccessible(true);
    	
    	assertTrue(tstObj1.equals(tstObj2, false));
    	field.set(tstObj1, Money.of(1, "EUR"));
    	assertFalse(tstObj1.equals(tstObj2, false));
    	field.set(tstObj2, null);
    	assertFalse(tstObj1.equals(tstObj2, false));
    	assertFalse(tstObj2.equals(tstObj1, false));
    	field.set(tstObj1, null);
    	assertTrue(tstObj1.equals(tstObj2, false));
    }
        
    @Test
    public void checkEqualsUid() throws Exception {
    	ZonedDateTime act=ZonedDateTime.now();
    	Account tstObj1=new Account(1L, act, null,null,null,null);
    	Account tstObj2=new Account(null, act, null,null,null,null);;
    	Account tstObj3=new Account(2L, act, null,null,null,null);
    	Account tstObj4=new Account(1L, act, null,null,null,null);;
    	
    	assertTrue(tstObj1.equals(tstObj1,true));
    	assertTrue(tstObj1.equals(tstObj4,true));
    	assertFalse(tstObj1.equals(Integer.valueOf(0),true));
    	assertFalse(tstObj1.equals(null,true));
    	assertFalse(tstObj1.equals(tstObj2,true));
    	assertFalse(tstObj2.equals(tstObj1,true));
    	assertFalse(tstObj1.equals(tstObj3,true));
	}
    
    @Test
    public void checkUpdateBalanceSingleMonth(){
    	final String e="EUR";
    	Account tstObj=new Account(1L, ZonedDateTime.now(), null,null,Money.of(1, e),null);
    	
    	ZonedDateTime act1 = ZonedDateTime.now();
    	MonthAccountTurnover monthMock1=Mockito.mock(MonthAccountTurnover.class);
    	when(monthMock1.getLastChange()).thenReturn(act1);
		when(monthMock1.getMonthBalanceAssets()).thenReturn(Money.of(0.5, e));
		when(monthMock1.getMonthBalanceDebt()).thenReturn(Money.of(2, e));
		
		tstObj.getBalanceMonths().add(monthMock1);    	
    	tstObj.updateBalance();
    	
    	assertEquals(Money.of(-0.5, e),tstObj.getBalance());
    	assertEquals(act1,tstObj.getLastChange());
    	verify(monthMock1).setInitialAssets(Money.of(1, e));
    	verify(monthMock1).setInitialDebt(  Money.of(0, e));
    	verify(monthMock1).updateBalance();
	}
    
    @Test
    public void checkUpdateBalanceTwoMonths(){
    	final String e="EUR";
    	Account tstObj=new Account(1L, ZonedDateTime.now(), null,null,Money.of(1, e),null);
    	
    	ZonedDateTime act2 = ZonedDateTime.now();
    	ZonedDateTime act1 = ZonedDateTime.now();
    	
    	MonthAccountTurnover monthMock1=Mockito.mock(MonthAccountTurnover.class);
    	MonthAccountTurnover monthMock2=Mockito.mock(MonthAccountTurnover.class);
    	when(monthMock1.getLastChange()).thenReturn(act1);
		when(monthMock1.getMonthBalanceAssets()).thenReturn(Money.of(0.5, e));
		when(monthMock1.getMonthBalanceDebt()).thenReturn(Money.of(2, e));
		
		when(monthMock2.getLastChange()).thenReturn(act2);
		when(monthMock2.getMonthBalanceAssets()).thenReturn(Money.of(4, e));
		when(monthMock2.getMonthBalanceDebt()).thenReturn(Money.of(6, e));
		
		when(monthMock2.compareTo(monthMock1)).thenReturn(-1);
		when(monthMock1.compareTo(monthMock2)).thenReturn(1);
		
		tstObj.getBalanceMonths().add(monthMock1);
    	tstObj.getBalanceMonths().add(monthMock2);
    	tstObj.updateBalance();
    	
    	assertEquals(Money.of(-2.5, e),tstObj.getBalance());
    	assertEquals(act1,tstObj.getLastChange());
    	verify(monthMock2).setInitialAssets(Money.of(1, e));
    	verify(monthMock2).setInitialDebt(  Money.of(0, e));
    	verify(monthMock2).updateBalance();
    	verify(monthMock1).setInitialAssets(Money.of(0, e));
    	verify(monthMock1).setInitialDebt(  Money.of(1, e));
    	verify(monthMock1).updateBalance();
    	assertEquals(monthMock1,tstObj.getBalanceMonths().get(1));
    	assertEquals(monthMock2,tstObj.getBalanceMonths().get(0));
    	
	}
    
    @Test
    public void checkSetInitialBalanceException(){
    	final String e="EUR";
    	Account tstObj=new Account(1L, ZonedDateTime.now(), null,null,null,null);
    	MonthAccountTurnover monthMock1=Mockito.mock(MonthAccountTurnover.class);
		tstObj.setInitialBalance(Money.of(1, e));
		tstObj.getBalanceMonths().add(monthMock1);
		
		assertThrows(RuntimeException.class,()->tstObj.setInitialBalance(Money.of(47, e)));
		assertEquals(Money.of(1, e),tstObj.getBalance());
    }
    
    @Test
    public void checkNullBalanceMonthsUpdate(){
    	Account tstObj=new Account(1L, ZonedDateTime.now(), null,null,null,null);
    	assertNull(tstObj.updateBalance());
    	tstObj.setBalanceMonths(null);
    	assertNull(tstObj.updateBalance());
    }
    
    @Test
    public void checkAddBalanceMonth(){
    	final String e="EUR";
    	Account tstObj=new Account(1L, ZonedDateTime.now(), null,null,Money.of(1, e),null);
    	
    	ZonedDateTime act1 = ZonedDateTime.now();
    	MonthAccountTurnover monthMock1=Mockito.mock(MonthAccountTurnover.class);
    	when(monthMock1.getLastChange()).thenReturn(act1);
		when(monthMock1.getMonthBalanceAssets()).thenReturn(Money.of(0.5, e));
		when(monthMock1.getMonthBalanceDebt()).thenReturn(Money.of(2, e));
		
		tstObj.addBalanceMonth(monthMock1);
    	
    	verify(monthMock1).updateBalance();
    	assertTrue(tstObj.getLastChange().isAfter(act1));
    }
    
    @SuppressWarnings("unlikely-arg-type")
	@Test
    public void checkBaseEquals(){
    	Account tstObj1=new Account(1L, null, null,null,null,null);
    	Account tstObj2=new Account(2L, null, null,null,null,null);
    	Account tstObj3=new Account(1L, null, null,null,null,null);
    	
    	assertTrue(tstObj1.equals(tstObj1));
    	assertTrue(tstObj1.equals(tstObj3));
    	assertFalse(tstObj1.equals(tstObj2));
    	assertFalse(tstObj1.equals(Integer.valueOf(0)));
    }
    
}
