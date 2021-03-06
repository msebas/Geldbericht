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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.reset;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import javax.money.MonetaryAmount;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mcservice.geldbericht.data.AbstractDataObject.AbstractDataObjectDatabaseQueueEntry;
import org.mcservice.geldbericht.data.MonthAccountTurnover.MonthAccountTurnoverDatabaseQueueEntry;
import org.mockito.Mockito;

class MonthAccountTurnoverTest {
	
	int count=0;
	
	static Stream<Arguments> getArgsSetters() throws Exception{
		String fields[]= {"initialAssets","initialDebt","monthBlocked"};
		return getSettersGetters(fields);
	}
	
	static Stream<Arguments> getArgsEquals() throws Exception{
		String fields[]= {"month","account","monthBalanceAssets","initialAssets","finalAssets"
				,"initialDebt","monthBalanceDebt","finalDebt","monthBlocked"};
		return getSettersGetters(fields);
	}
	
	static Stream<Arguments> getSettersGetters(String[] fields) throws Exception{
		ArrayList<Arguments> result=new ArrayList<Arguments>();
		Class<MonthAccountTurnover> clazz=MonthAccountTurnover.class;
		for(String fieldName : fields) {
			final Field field = clazz.getDeclaredField(fieldName);
			String upperFieldName=field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
			BiConsumer<Object,Object> setter;
			Method getter;
			Object val1=null,val2=null;
			try {
				final Method actSetter=clazz.getMethod("set"+upperFieldName, field.getType());
				setter= (Object obj, Object val) -> {try {
					actSetter.invoke(obj, val);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}};
			} catch (NoSuchMethodException e ) {
				field.setAccessible(true);
				setter= (Object obj, Object val) -> {try {
					field.set(obj, val);
				} catch (Exception e1) {
					throw new RuntimeException(e1);
				}};
			}
			try {
				getter=clazz.getMethod("get"+upperFieldName);
			} catch (NoSuchMethodException e ) {
				getter=clazz.getMethod("is"+upperFieldName);
			}			
			if(field.getType()==String.class) {
				val1="Str1";
				val2="Str2";
			} else if(field.getType()==LocalDate.class) {
				val1=LocalDate.now().minusDays(4);
				val2=LocalDate.now().minusDays(3);
			} else if(field.getType()==Boolean.class || field.getType()==Boolean.TYPE) {
				val1=Boolean.valueOf(true);
				val2=Boolean.valueOf(false);
			} else if(field.getType()==MonetaryAmount.class) {
				val1=Money.of(1.2, "EUR");
				val2=Money.of(3.4, "EUR");
			} else if(field.getType()==Account.class) {
				val1=new Account(2L,  ZonedDateTime.now(), "Name 1", "Name 2", null, null,null);
				val2=new Account(1L,  ZonedDateTime.now(), "Name 3", "Name 4", null, null,null);
			} else {
				fail(String.format("Type %s not implemented yet",field.getType().getName()));
			}
			result.add(Arguments.of(setter,getter,val1,val2));
		}
        return result.stream();
    }
    
    @ParameterizedTest
    @MethodSource("getArgsSetters")
    public void checkSetter(BiConsumer<Object,Object> setter, Method getter, 
    		Object val1, Object val2) throws Exception {
    	ZonedDateTime act=ZonedDateTime.now();
    	MonthAccountTurnover tstObj1=new MonthAccountTurnover(null, act, null,null,null,null, null, null, null, null, null);
    	MonthAccountTurnover tstObj2=new MonthAccountTurnover(null, act, null,null,null,null, null, null, null, null, null);
    	//Transactions should be equals even if there last reported change is different
    	assertTrue(tstObj1.equals(tstObj2));
    	
    	assertFalse(val1==getter.invoke(tstObj1));
    	act=tstObj1.getLastChange();
    	setter.accept(tstObj1, val1);
    	assertTrue(val1.equals(getter.invoke(tstObj1)));
		assertTrue(act.isBefore(tstObj1.getLastChange()));
		assertTrue(ZonedDateTime.now().isAfter(tstObj1.getLastChange()));
    	act=tstObj1.getLastChange();
    	setter.accept(tstObj1, val1);
    	assertTrue(val1.equals(getter.invoke(tstObj1)));
    	assertEquals(act,tstObj1.getLastChange());
    }
    
    @ParameterizedTest
    @MethodSource("getArgsEquals")
    public void checkEquals(BiConsumer<Object,Object> setter, Method getter, 
    		Object val1, Object val2) throws Exception {
    	ZonedDateTime act=ZonedDateTime.now();
    	MonthAccountTurnover tstObj1=new MonthAccountTurnover(null, act, null,null,null,null, null, null, null, null, null);
    	MonthAccountTurnover tstObj2=new MonthAccountTurnover(null, act, null,null,null,null, null, null, null, null, null);
    	assertTrue(tstObj1.equals(tstObj2));
    	assertEquals(tstObj2.hashCode(),tstObj1.hashCode());
    	
    	setter.accept(tstObj1, val1);
    	assertTrue(val1.equals(getter.invoke(tstObj1)));
		assertFalse(tstObj2.equals(tstObj1));
    	assertFalse(tstObj1.equals(tstObj2));
    	setter.accept(tstObj2, val1);
    	assertTrue(tstObj2.equals(tstObj1));
    	assertTrue(tstObj1.equals(tstObj2));
    	assertEquals(tstObj2.hashCode(),tstObj1.hashCode());
    	setter.accept(tstObj1, val2);
    	assertFalse(tstObj2.equals(tstObj1));
    	assertFalse(tstObj1.equals(tstObj2));
    }
    
    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void checkEqualsUid() throws Exception {
    	ZonedDateTime act=ZonedDateTime.now();
    	MonthAccountTurnover tstObj1=new MonthAccountTurnover(1L, act, null,null,null,null, null, null, null, null, null);
    	MonthAccountTurnover tstObj2=new MonthAccountTurnover(null, act, null,null,null,null, null, null, null, null, null);;
    	MonthAccountTurnover tstObj3=new MonthAccountTurnover(2L, act, null,null,null,null, null, null, null, null, null);
    	MonthAccountTurnover tstObj4=new MonthAccountTurnover(1L, act, null,null,null,null, null, null, null, null, null);;
    	
    	assertTrue(tstObj1.equals(tstObj1));
    	assertTrue(tstObj1.equals(tstObj4));
    	assertFalse(tstObj1.equals(Integer.valueOf(0)));
    	assertFalse(tstObj1.equals(null));
    	assertFalse(tstObj1.equals(tstObj2));
    	assertFalse(tstObj2.equals(tstObj1));
    	assertFalse(tstObj1.equals(tstObj3));
	}
    
    public Transaction getTransaction(LocalDate date, double amount) {
    	return new Transaction(Long.valueOf(count), ZonedDateTime.now(), 0, 
    					amount>0?Money.of(amount, "EUR"):Money.of(0, "EUR"),
    					amount<0?Money.of(-amount, "EUR"):Money.of(0, "EUR"),
    							null,null,null,null,date,null,null,null);
    }

    @Test
    public void checkIsInMonth() throws Exception {
    	LocalDate act=LocalDate.of(2019, 1, 1);
    	MonthAccountTurnover tstObj=new MonthAccountTurnover(1L, ZonedDateTime.now(), null,act,
    			null,null, null, null, null, null, null);
    	
    	for (int i = -500; i < 500; i++) {
			if(2019==act.minusDays(i).getYear() && 1==act.minusDays(i).getMonthValue()) {
				assertTrue(tstObj.isInMonth(act.minusDays(i)));
			} else {
				assertFalse(tstObj.isInMonth(act.minusDays(i)));
			}
		}
    	
	}
    
    @Test
    public void checkEqualsTransactions() throws Exception {
    	ZonedDateTime act=ZonedDateTime.now();
    	MonthAccountTurnover tstObj1=new MonthAccountTurnover(null, act, null,null,null,null, null, null, null, null, null);
    	MonthAccountTurnover tstObj2=new MonthAccountTurnover(null, act, null,null,null,null, null, null, null, null, null);
    	
    	Field field=MonthAccountTurnover.class.getDeclaredField("transactions");
    	field.setAccessible(true);
    	field.set(tstObj1, List.of(getTransaction(LocalDate.now(), -4.2)));
    	
    	assertFalse(tstObj1.equals(tstObj2));
    	assertFalse(tstObj2.equals(tstObj1));    	
    	field.setAccessible(false);
    }

    @Test
    public void checkCompareTo() throws Exception {
    	LocalDate act=LocalDate.of(2019, 1, 1);
    	MonthAccountTurnover tstObj1=new MonthAccountTurnover(1L,   ZonedDateTime.now(), null,act.minusMonths(1),null,null, null, null, null, null, null);
    	MonthAccountTurnover tstObj2=new MonthAccountTurnover(null, ZonedDateTime.now(), null,act.minusMonths(2),null,null, null, null, null, null, null);;
    	MonthAccountTurnover tstObj3=new MonthAccountTurnover(2L,   ZonedDateTime.now(), null,act.minusMonths(1),null,null, null, null, null, null, null);
    	
    	assertTrue(0<tstObj1.compareTo(tstObj2));
    	assertTrue(0>tstObj2.compareTo(tstObj1));
    	assertTrue(0==tstObj1.compareTo(tstObj3));
	}
    
    @Test
    public void checkUpdateBalance() throws Exception {
    	LocalDate date = LocalDate.of(2019, 1, 1);
    	Account acc=new Account(2L,  ZonedDateTime.now(), "Name 1", "Name 2", Money.of(2, "EUR"),null,null);
    	MonthAccountTurnover tstObj=MonthAccountTurnover.getEmptyMonthAccountTurnover(date,acc);
    	tstObj.appendTranaction(getTransaction(date.plusDays(1), -4.2));
    	ZonedDateTime act = ZonedDateTime.now();
    	
    	//The time should be the same as the transaction creation date
    	assertTrue(tstObj.getLastChange().isBefore(act));
    	assertEquals(tstObj.getTransactions().get(0).getLastChange(), tstObj.getLastChange());
    	assertEquals(Money.of(2.2, "EUR"),tstObj.getFinalDebt());
    	assertEquals(Money.of(0.0, "EUR"),tstObj.getFinalAssets());
    	assertEquals(Money.of(4.2, "EUR"),tstObj.getMonthBalanceDebt());
    	assertEquals(Money.of(0.0, "EUR"),tstObj.getMonthBalanceAssets());
    	
    	tstObj.appendTranaction(getTransaction(date.plusDays(2), 0.2));
    	tstObj.appendTranaction(getTransaction(date.plusDays(0), 2.5));
    	act = ZonedDateTime.now();
    	
    	assertTrue(tstObj.getLastChange().isBefore(act));
    	assertEquals(tstObj.getTransactions().get(0).getLastChange(), tstObj.getLastChange());
    	assertEquals(Money.of(0.0, "EUR"),tstObj.getFinalDebt());
    	assertEquals(Money.of(0.5, "EUR"),tstObj.getFinalAssets());
    	assertEquals(Money.of(4.2, "EUR"),tstObj.getMonthBalanceDebt());
    	assertEquals(Money.of(2.7, "EUR"),tstObj.getMonthBalanceAssets());
    }
        
    @Test
    public void checkRemoveTransaction() throws Exception {
    	LocalDate date = LocalDate.of(2019, 1, 1);
    	Account acc=new Account(2L,  ZonedDateTime.now(), "Name 1", "Name 2", Money.of(2, "EUR"),null,null);
    	MonthAccountTurnover tstObj=Mockito.spy(MonthAccountTurnover.getEmptyMonthAccountTurnover(date,acc));
    	Transaction actTransaction = getTransaction(date.plusDays(2), 2.2);
    	tstObj.appendTranaction(getTransaction(date.plusDays(1), -4.2));
    	tstObj.appendTranaction(actTransaction);
    	Field field=MonthAccountTurnover.class.getDeclaredField("transactionsLoaded");
    	field.setAccessible(true);
    	field.set(tstObj,false);
    	field.setAccessible(false);
    	reset(tstObj);
    	    	
    	tstObj.removeTranaction(0);
    	
    	verify(tstObj).updateBalance();
    	assertTrue(tstObj.getTransactions().get(0).equals(actTransaction));
    	assertTrue(tstObj.getTransactions().size()==1);
    	assertTrue(tstObj.isTransactionsLoaded());
    }
    
    @Test
    public void checkRemoveAllTransactions() throws Exception {
    	LocalDate date = LocalDate.of(2019, 1, 1);
    	Account acc=new Account(2L,  ZonedDateTime.now(), "Name 1", "Name 2", Money.of(2, "EUR"),null,null);
    	MonthAccountTurnover tstObj=Mockito.spy(MonthAccountTurnover.getEmptyMonthAccountTurnover(date,acc));
    	Transaction actTransaction = getTransaction(date.plusDays(2), 2.2);
    	tstObj.appendTranaction(getTransaction(date.plusDays(1), -4.2));
    	tstObj.appendTranaction(actTransaction);
    	Field field=MonthAccountTurnover.class.getDeclaredField("transactionsLoaded");
    	field.setAccessible(true);
    	field.set(tstObj,false);
    	field.setAccessible(false);
    	reset(tstObj);
    	    	
    	tstObj.removeAllTranactions();
    	
    	verify(tstObj).updateBalance();
    	assertTrue(tstObj.getTransactions().isEmpty());
    	assertTrue(tstObj.isTransactionsLoaded());
    }

    @Test 
    public void checkGetPersistingListChanged() {
    	Money zero=Money.of(0,"EUR"),one=Money.of(1,"EUR");
    	Account accountMock=Mockito.mock(Account.class);
    	MonthAccountTurnover tstObj=new MonthAccountTurnover(1L, ZonedDateTime.now(), new ArrayList<>(), 
    			null, accountMock,one,zero,one,zero,one,one);
    	List<AbstractDataObjectDatabaseQueueEntry> answerList=List.of(Mockito.mock(MonthAccountTurnoverDatabaseQueueEntry.class));
    	Mockito.when(accountMock.getPersistingList()).thenReturn(answerList);
    	List<AbstractDataObjectDatabaseQueueEntry> resList=tstObj.getPersistingList();
    	
    	Mockito.verify(accountMock).getPersistingList();
    	assertEquals(1,resList.size());
    	assertEquals(answerList.get(0),resList.get(0));
    	assertEquals(zero,tstObj.getFinalAssets());
    	assertEquals(one,tstObj.getFinalDebt());
    }
    
    @Test 
    public void checkGetPersistingListNoChangeTransactionsLoaded() throws Exception{
    	Money zero=Money.of(0,"EUR");
    	List<AbstractDataObjectDatabaseQueueEntry> answerList=List.of(Mockito.mock(MonthAccountTurnoverDatabaseQueueEntry.class));
    	ArrayList<Transaction> transactionList=new ArrayList<>(
    			List.of(Mockito.mock(Transaction.class),Mockito.mock(Transaction.class)));
    	Mockito.when(transactionList.get(0).getPersistingList()).thenReturn(answerList);
    	Mockito.when(transactionList.get(1).getPersistingList()).thenReturn(null);
    	Mockito.when(transactionList.get(0).getReceipts()).thenReturn(zero);
    	Mockito.when(transactionList.get(1).getReceipts()).thenReturn(zero);
    	Mockito.when(transactionList.get(0).getSpending()).thenReturn(zero);
    	Mockito.when(transactionList.get(1).getSpending()).thenReturn(zero);
    	Mockito.when(transactionList.get(0).getLastChange()).thenReturn(ZonedDateTime.now());
    	Mockito.when(transactionList.get(1).getLastChange()).thenReturn(ZonedDateTime.now());
    	Mockito.when(transactionList.get(0).compareTo(transactionList.get(1))).thenReturn(1);
    	Mockito.when(transactionList.get(1).compareTo(transactionList.get(0))).thenReturn(-1);
    	Account accountMock=Mockito.mock(Account.class);
    	MonthAccountTurnover tstObj=new MonthAccountTurnover(1L, ZonedDateTime.now(), transactionList, 
    			null, accountMock,zero,zero,zero,zero,zero,zero);
    	tstObj.getTransactions();
    	
    	List<AbstractDataObjectDatabaseQueueEntry> resList=tstObj.getPersistingList();
    	
    	Mockito.verify(transactionList.get(0)).getPersistingList();
    	Mockito.verify(transactionList.get(1)).getPersistingList();
    	Mockito.verify(transactionList.get(0)).getReceipts();
    	Mockito.verify(transactionList.get(1)).getReceipts();
    	assertEquals(1,resList.size());
    	assertEquals(answerList.get(0),resList.get(0));
    }
    
    @Test 
    public void checkGetPersistingListNoChange() {
    	Money zero=Money.of(0,"EUR");
    	Account accountMock=Mockito.mock(Account.class);
    	MonthAccountTurnover tstObj=new MonthAccountTurnover(1L, ZonedDateTime.now(), new ArrayList<>(), 
    			null, accountMock,zero,zero,zero,zero,zero,zero);
    	
    	Mockito.verify(accountMock,Mockito.times(0)).getPersistingList();
    	assertNull(tstObj.getPersistingList());
    }

    @Test 
    public void checkApplyPersistedStateNullNotChanged() {
    	Money zero=Money.of(0,"EUR");
    	Account accountMock=Mockito.mock(Account.class);
    	MonthAccountTurnover month=new MonthAccountTurnover(null, ZonedDateTime.now(), new ArrayList<>(), 
    			null, accountMock,zero,zero,zero,zero,zero,zero);
    	MonthAccountTurnover persistedState=new MonthAccountTurnover(1L, ZonedDateTime.now(), new ArrayList<>(), 
    			null, accountMock,zero,zero,zero,zero,zero,zero);
    	
    	MonthAccountTurnover.MonthAccountTurnoverDatabaseQueueEntry tstObj=
    			month.new MonthAccountTurnoverDatabaseQueueEntry(month, false);
    	assertEquals(month,tstObj.getStateToPersist());
    	
    	tstObj.applyPersistedState(persistedState);
    	assertEquals(1L,month.getUid());
    }

    @Test 
    public void checkApplyPersistedStateChangedNoTransactionsLoadedNoChangedState() throws Exception{
    	Money zero=Money.of(0,"EUR"),one=Money.of(1,"EUR");
    	Account accountMock=Mockito.mock(Account.class);
    	MonthAccountTurnover month=new MonthAccountTurnover(1L, ZonedDateTime.now(), null, 
    			null, accountMock,zero,zero,zero,zero,zero,zero);
    	month.setInitialAssets(one);
    	disableTransactionsLoaded(month);
    	
    	MonthAccountTurnover.MonthAccountTurnoverDatabaseQueueEntry tstObj=
    			month.new MonthAccountTurnoverDatabaseQueueEntry(month, false);
    	assertEquals(month,tstObj.getStateToPersist());
    	
    	assertTrue(month.isChanged());
    	assertFalse(month.isTransactionsLoaded());
    	tstObj.applyPersistedState(month);
    	assertFalse(month.isChanged());
    }

    static void disableTransactionsLoaded(MonthAccountTurnover month)
			throws Exception{
		MonthAccountTurnover.class.getDeclaredField("transactionsLoaded").setAccessible(true);
    	MonthAccountTurnover.class.getDeclaredField("transactionsLoaded").set(month,false);
    	MonthAccountTurnover.class.getDeclaredField("transactionsLoaded").setAccessible(false);
	}
        
    @Test 
    public void checkApplyPersistedStateChangedNoTransactionsLoadedChangedState()  throws Exception{
    	Money zero=Money.of(0,"EUR"),one=Money.of(1,"EUR");
    	Account accountMock=Mockito.mock(Account.class);
    	MonthAccountTurnover month=new MonthAccountTurnover(1L, ZonedDateTime.now(), null, 
    			null, accountMock,zero,zero,zero,zero,zero,zero);
    	month.setInitialAssets(one);
    	disableTransactionsLoaded(month);
    	MonthAccountTurnover persistedState=new MonthAccountTurnover(1L, ZonedDateTime.now(), null, 
    			null, accountMock,zero,zero,zero,zero,zero,zero);
    	
    	MonthAccountTurnover.MonthAccountTurnoverDatabaseQueueEntry tstObj=
    			month.new MonthAccountTurnoverDatabaseQueueEntry(persistedState, false);
    	assertEquals(persistedState,tstObj.getStateToPersist());
    	
    	assertTrue(month.isChanged());
    	assertFalse(month.isTransactionsLoaded());
    	tstObj.applyPersistedState(persistedState);
    	assertTrue(month.isChanged());
    }
    
    @Test 
    public void checkApplyPersistedStateChangedTransactionsLoaded() {
    	Money zero=Money.of(0,"EUR"),one=Money.of(1,"EUR");
    	Account accountMock=Mockito.mock(Account.class);
    	MonthAccountTurnover month=new MonthAccountTurnover(1L, ZonedDateTime.now(), new ArrayList<>(), 
    			null, accountMock,zero,zero,zero,zero,zero,zero);
    	month.setInitialAssets(one);
    	
    	MonthAccountTurnover.MonthAccountTurnoverDatabaseQueueEntry tstObj=
    			month.new MonthAccountTurnoverDatabaseQueueEntry(month, false);
    	assertEquals(month,tstObj.getStateToPersist());
    	
    	assertTrue(month.isChanged());
    	assertTrue(month.isTransactionsLoaded());
    	tstObj.applyPersistedState(month);
    	assertFalse(month.isChanged());
    }
    
    @Test
    public void checkGetDeleteListNullEmpty() {
    	Money zero=Money.of(0,"EUR");
    	Account accountMock=Mockito.mock(Account.class);
    	MonthAccountTurnover month=new MonthAccountTurnover(null, ZonedDateTime.now(), new ArrayList<>(), 
    			null, accountMock,zero,zero,zero,zero,zero,zero);
    	
    	assertNull(month.getDeleteList());
    }
    
    @Test
    public void checkGetDeleteListNullTransactions() {
    	Money zero=Money.of(0,"EUR");
    	List<AbstractDataObjectDatabaseQueueEntry> answerList=List.of(Mockito.mock(MonthAccountTurnoverDatabaseQueueEntry.class));
    	ArrayList<Transaction> transactionList=new ArrayList<>(
    			List.of(Mockito.mock(Transaction.class),Mockito.mock(Transaction.class)));
    	Mockito.when(transactionList.get(0).getDeleteList()).thenReturn(answerList);
    	Mockito.when(transactionList.get(1).getDeleteList()).thenReturn(null);
    	MonthAccountTurnover tstObj=new MonthAccountTurnover(null, ZonedDateTime.now(), transactionList, 
    			null, null,zero,zero,zero,zero,zero,zero);
    	
    	List<AbstractDataObjectDatabaseQueueEntry> resList=tstObj.getDeleteList();
    	
    	Mockito.verify(transactionList.get(0)).getDeleteList();
    	Mockito.verify(transactionList.get(1)).getDeleteList();
    	assertEquals(1,resList.size());
    	assertEquals(answerList.get(0),resList.get(0));
    }
    
    @Test
    public void checkGetDeleteListIdTransactions() {
    	Money zero=Money.of(0,"EUR");
    	List<AbstractDataObjectDatabaseQueueEntry> answerList=List.of(Mockito.mock(MonthAccountTurnoverDatabaseQueueEntry.class));
    	ArrayList<Transaction> transactionList=new ArrayList<>(
    			List.of(Mockito.mock(Transaction.class),Mockito.mock(Transaction.class)));
    	Mockito.when(transactionList.get(0).getDeleteList()).thenReturn(answerList);
    	Mockito.when(transactionList.get(1).getDeleteList()).thenReturn(null);
    	MonthAccountTurnover tstObj=new MonthAccountTurnover(1L, ZonedDateTime.now(), transactionList, 
    			null, null,zero,zero,zero,zero,zero,zero);
    	
    	List<AbstractDataObjectDatabaseQueueEntry> resList=tstObj.getDeleteList();
    	
    	Mockito.verify(transactionList.get(0)).getDeleteList();
    	Mockito.verify(transactionList.get(1)).getDeleteList();
    	assertEquals(2,resList.size());
    	assertEquals(answerList.get(0),resList.get(0));
    	assertTrue(resList.get(1).isDelete());
    	MonthAccountTurnover resState = (MonthAccountTurnover) resList.get(1).getStateToPersist();
    	assertEquals(1L,resState.getUid());
    	assertTrue(resState.getTransactions().isEmpty());
    	resList.get(1).applyPersistedState(null);
    }
    
    //TODO Add missing tests
}
