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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mcservice.geldbericht.data.AbstractDataObject.AbstractDataObjectDatabaseQueueEntry;

class UserTest {

	static Stream<Arguments> getSettersGetters() throws Exception{
		ArrayList<Arguments> result=new ArrayList<Arguments>();
		Class<User> clazz=User.class;
		String fields[]= {"userName","passwordHash"};
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
			} else {
				assertTrue(false,"Not implemented yet");
			}
			result.add(Arguments.of(field, setter,getter,val1,cval1,val2,updateChanged));
		}
        return result.stream();
    }
    
    @ParameterizedTest
    @MethodSource("getSettersGetters")
    public void checkSetter(Field field, Method setter, Method getter, 
    		Object val1, Object cval1, Object val2, boolean updateChanged) throws Exception {
    	ZonedDateTime act = ZonedDateTime.now();
    	User tstObj1=new User(null, act, null,null);
    	User tstObj2=new User(null, act, null,null);
    	assertTrue(tstObj1.equals(tstObj2));
    	assertEquals(tstObj1.hashCode(),tstObj2.hashCode());
    	
    	assertTrue(tstObj1.equals(tstObj2));
    	assertEquals(tstObj1.hashCode(),tstObj2.hashCode());
    	
    	assertFalse(val1==getter.invoke(tstObj1));
    	act=tstObj1.getLastChange();
    	setter.invoke(tstObj1, val1);
    	assertTrue(val1.equals(getter.invoke(tstObj1)));
		assertTrue(act.isBefore(tstObj1.getLastChange()));
		assertTrue(ZonedDateTime.now().isAfter(tstObj1.getLastChange()));
		tstObj2=new User(tstObj1);
		field.setAccessible(true);
		field.set(tstObj2, null);
    	act=tstObj1.getLastChange();
    	setter.invoke(tstObj1, val1);
    	assertTrue(val1.equals(getter.invoke(tstObj1)));
    	assertEquals(act,tstObj1.getLastChange());
    	assertFalse(tstObj2.equals(tstObj1));
    	assertFalse(tstObj1.equals(tstObj2));
		field.set(tstObj2, val1);
		field.setAccessible(false);
    	assertTrue(tstObj2.equals(tstObj1));
    	assertTrue(tstObj1.equals(tstObj2));
    	assertEquals(tstObj1.hashCode(),tstObj2.hashCode());
		field.setAccessible(true);
		field.set(tstObj1, val2);
		field.setAccessible(false);
    	assertFalse(tstObj2.equals(tstObj1));
    	assertFalse(tstObj1.equals(tstObj2));
    }
    
    @SuppressWarnings("unlikely-arg-type")
	@Test
    public void checkEqualsSuper() throws Exception {
    	ZonedDateTime act=ZonedDateTime.now();
    	User tstObj1=new User(1L, act, null,null);
    	User tstObj2=new User(null, act, null,null); 
    	User tstObj3=new User(2L, act, null,null);
    	User tstObj4=new User(1L, act, null,null);
    	User tstObj5=new User(1L, act.minusMinutes(4), null,null); 
    	
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
    public void checkStaticValues() throws Exception {
    	try {
    		//This only checks if the value is not far too high
    		//It could not really check if the value is the correct one...
    		@SuppressWarnings("unused")
			char vals[]=new char[(int) (User.passwordMemory*1024)];
    	} catch (OutOfMemoryError e){
    		fail();
    	}
    	//This should be a more precise check, which does not copy the code...
    	assertTrue(User.passwordCpuThreads>=1);
	}
    
    @Test
    public void checkSetPassword() throws Exception {
    	User tstObj=new User(1L, ZonedDateTime.now(), null,null);
    	User.passwordCpuUsageTime=100;
    	User.passwordMemory=4096;
    	
    	tstObj.setPassword("pass1234word".toCharArray());
    	
    	Instant start=Instant.now();
    	assertTrue(tstObj.verifyPassword("pass1234word".toCharArray()));
    	Instant end=Instant.now();
    	Duration diff=Duration.between(end, start);
    	assertTrue(diff.toNanos()<0.2e9);
    }
    
    @Test
    public void checkGetPersistList() {
    	User tstObj=new User(1L, ZonedDateTime.now(), null,null);
    	List<AbstractDataObjectDatabaseQueueEntry> persList=tstObj.getPersistingList();
    	
    	User persState=(User) persList.get(0).getStateToPersist();
    	
    	assertTrue( persList.get(0).isMerge());
    	assertFalse(persState==tstObj);
    	assertTrue( persState.equals(tstObj));
    }
    
    @Test
    public void checkGetDeleteList() {
    	User tstObj=new User(1L, ZonedDateTime.now(), null,null);
    	List<AbstractDataObjectDatabaseQueueEntry> persList=tstObj.getPersistingList();
    	
    	User persState=(User) persList.get(0).getStateToPersist();
    	
    	assertTrue( persList.get(0).isMerge());
    	assertFalse(persState==tstObj);
    	assertTrue( persState.equals(tstObj));
    }
}
