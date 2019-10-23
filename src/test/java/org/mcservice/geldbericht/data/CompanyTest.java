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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mcservice.geldbericht.data.AbstractDataObject.AbstractDataObjectDatabaseQueueEntry;
import org.mcservice.geldbericht.data.Account.AccountDatabaseQueueEntry;
import org.mcservice.geldbericht.data.MonthAccountTurnover.MonthAccountTurnoverDatabaseQueueEntry;

class CompanyTest {

	static Stream<Arguments> getSettersGetters() throws Exception{
		ArrayList<Arguments> result=new ArrayList<Arguments>();
		Class<Company> clazz=Company.class;
		String fields[]= {"accounts","companyName","companyNumber","companyBookkeepingAppointment"};
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
			} else if(field.getType()==List.class) {
				val1=new ArrayList<Account>(List.of(mock(Account.class)));
				val2=new ArrayList<Account>();
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
    	Company tstObj1=new Company(null, ZonedDateTime.now(), null,null,null,null);
    	Company tstObj2=new Company(null, ZonedDateTime.now().minusMinutes(4), null,null,null,null);
    	//Companys should be equals even if there last reported change is different
    	assertTrue(tstObj1.equals(tstObj2,false));
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
    	getter.invoke(tstObj1);
    	getter.invoke(tstObj2);
    	act=tstObj1.getLastChange();
    	setter.invoke(tstObj1, val1);
    	assertTrue(val1.equals(getter.invoke(tstObj1)));
    	assertEquals(act,tstObj1.getLastChange());
    	assertFalse(tstObj2.equals(tstObj1,true));
    	assertFalse(tstObj1.equals(tstObj2,true));
    	setter.invoke(tstObj2, val1);
    	assertTrue(tstObj2.equals(tstObj1,true));
    	assertTrue(tstObj1.equals(tstObj2,true));
    	assertEquals(tstObj1.hashCode(),tstObj2.hashCode());
    	setter.invoke(tstObj1, val2);
    	assertFalse(tstObj2.equals(tstObj1,true));
    	assertFalse(tstObj1.equals(tstObj2,true));
    }
    
    @SuppressWarnings("unlikely-arg-type")
	@Test
    public void checkEqualsUid() throws Exception {
    	Company tstObj1=new Company(1L, ZonedDateTime.now(), null,null,null,null);
    	Company tstObj2=new Company(null, ZonedDateTime.now().minusMinutes(4), null,null,null,null);
    	Company tstObj3=new Company(2L, ZonedDateTime.now().minusMinutes(4), null,null,null,null);
    	Company tstObj4=new Company(1L, ZonedDateTime.now().minusMinutes(4), null,null,null,null);
    	
    	assertTrue(tstObj1.equals(tstObj1));
    	assertTrue(tstObj1.equals(tstObj4));
    	assertFalse(tstObj1.equals(Integer.valueOf(0)));
    	assertFalse(tstObj1.equals(null));
    	assertFalse(tstObj1.equals(tstObj2));
    	assertFalse(tstObj2.equals(tstObj1));
    	assertFalse(tstObj1.equals(tstObj3));
	}
    
    @Test
    public void checkGetDeleteListNull() {
    	Company tstObj=new Company(null, ZonedDateTime.now(), new ArrayList<>(),null,null,null);
    	assertNull(tstObj.getDeleteList());
    }
    
    @Test
    public void checkGetDeleteListAccounts() {
    	Account accountMock=mock(Account.class);
    	List<AbstractDataObjectDatabaseQueueEntry> fakeList=List.of(mock(AccountDatabaseQueueEntry.class));
		when(accountMock.getDeleteList()).thenReturn(fakeList).thenReturn(null);
    	
    	Company tstObj=new Company(1L, ZonedDateTime.now(), new ArrayList<>(List.of(accountMock,accountMock)),null,null,null);
    	List<AbstractDataObjectDatabaseQueueEntry> resultList = tstObj.getDeleteList();
    	
    	verify(accountMock,times(2)).getDeleteList();
    	assertEquals(2,resultList.size());
    	assertTrue(resultList.get(0)==fakeList.get(0));
    	assertTrue(resultList.get(1).isDelete());
    	
    	AbstractDataObject persistState = resultList.get(1).getStateToPersist();
    	assertTrue(persistState.getUid()==tstObj.getUid());
    	assertEquals(2,tstObj.getAccounts().size());
    	assertTrue(((Company) persistState).getAccounts().isEmpty());
    	assertFalse(persistState==tstObj);
    }
    
    @Test
    public void checkGetPersistingListNoAccounts() {
    	Company tstObj=new Company(1L, ZonedDateTime.now(), new ArrayList<>(),null,null,null);
    	List<AbstractDataObjectDatabaseQueueEntry> resultList = tstObj.getPersistingList();
    	
    	AbstractDataObject persistState = resultList.get(0).getStateToPersist();
    	assertTrue(persistState.equals(tstObj));
    	assertEquals(1,resultList.size());
    	assertFalse(((Company) persistState).getAccounts()==tstObj.getAccounts());
    	assertFalse(persistState==tstObj);
    }
    
    @Test
    public void checkGetPersistingListAccountsNotLoaded() throws Exception{
    	@SuppressWarnings("unchecked")
		Company tstObj=new Company(1L, ZonedDateTime.now(), mock(List.class),null,null,null);
    	Company.class.getDeclaredField("accountsLoaded").setAccessible(true);
    	Company.class.getDeclaredField("accountsLoaded").set(tstObj,false);
    	Company.class.getDeclaredField("accountsLoaded").setAccessible(false);
    	
    	List<AbstractDataObjectDatabaseQueueEntry> resultList = tstObj.getPersistingList();
    	
    	AbstractDataObject persistState = resultList.get(0).getStateToPersist();
    	assertTrue(persistState.equals(tstObj));
    	assertEquals(1,resultList.size());
    	assertTrue(((Company) persistState).getAccounts()==tstObj.getAccounts());
    	assertFalse(persistState==tstObj);
    }
    
    @Test
    public void checkGetPersistListAccounts() {
    	Account accountMock=mock(Account.class);
    	List<AbstractDataObjectDatabaseQueueEntry> fakeList1=List.of(mock(AccountDatabaseQueueEntry.class));
    	List<AbstractDataObjectDatabaseQueueEntry> fakeList2=List.of(mock(MonthAccountTurnoverDatabaseQueueEntry.class));
		when(accountMock.getPersistingList(true)).thenReturn(fakeList1).thenReturn(fakeList2);
    	
    	Company tstObj=new Company(1L, ZonedDateTime.now(), new ArrayList<>(List.of(accountMock,accountMock)),null,null,null);
    	List<AbstractDataObjectDatabaseQueueEntry> resultList = tstObj.getPersistingList();
    	
    	verify(accountMock,times(2)).getPersistingList(true);
    	assertEquals(3,resultList.size());
    	assertTrue(resultList.get(0)==fakeList1.get(0));
    	assertTrue(resultList.get(1)==fakeList2.get(0));
    	verify((AccountDatabaseQueueEntry) fakeList1.get(0)).addToCompany(tstObj);
    	assertTrue(resultList.get(2).isMerge());
    }
}
