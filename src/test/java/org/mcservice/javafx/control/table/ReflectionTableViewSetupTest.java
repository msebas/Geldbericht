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
package org.mcservice.javafx.control.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mcservice.javafx.control.table.MemberVariable;
import org.mcservice.javafx.control.table.ReflectionTableView;
import org.mcservice.javafx.control.table.TestTypes.FakeFactory;
import org.mockito.Mockito;

import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TableColumn;

@Tag("Table")
@Tag("GUI")
class ReflectionTableViewSetupTest{
	
	ReflectionTableView<?> tableView=null;
	
	@BeforeEach
	public void setUp() {
		@SuppressWarnings("unused")
		JFXPanel dummy=new JFXPanel(); 
	}
        
    @Test
    public void checkStructure3S2I() throws Exception {
    	
		tableView = new ReflectionTableView<TestTypes.Test3S2I>(TestTypes.Test3S2I.class);
		
    	assertEquals(5,this.tableView.getColumns().size());
    	Object act=tableView.getColumns().get(0).getOnEditCommit();
    	assertTrue(act instanceof MemberVariable);
		assertEquals(TestTypes.Test3S2I.class.getDeclaredField("firstString"),((MemberVariable<?,?>) act).getField());
		//Could only be tested by reflection, skip it
		//assertEquals(TestTypes.Test3S2I.class.getMethod("getFirstString"),((MemberVariable<?,?>) act).getter);
		//assertEquals(TestTypes.Test3S2I.class.getMethod("setFirstString",String.class),((MemberVariable<?,?>) act).setter);
		
		act=tableView.getColumns().get(1).getOnEditCommit();
    	assertTrue(act instanceof MemberVariable);
		assertEquals(TestTypes.Test3S2I.class.getDeclaredField("secondString"),((MemberVariable<?,?>) act).getField());
		
		act=tableView.getColumns().get(2).getOnEditCommit();
    	assertTrue(act instanceof MemberVariable);
		assertEquals(TestTypes.Test3S2I.class.getDeclaredField("thirdString"),((MemberVariable<?,?>) act).getField());
		
		act=tableView.getColumns().get(3).getOnEditCommit();
    	assertTrue(act instanceof MemberVariable);
		assertEquals(TestTypes.Test3S2I.class.getDeclaredField("firstInt"),((MemberVariable<?,?>) act).getField());
		
		act=tableView.getColumns().get(4).getOnEditCommit();
    	assertTrue(act instanceof MemberVariable);
		assertEquals(TestTypes.Test3S2I.class.getDeclaredField("secondInt"),((MemberVariable<?,?>) act).getField());
		
    }
    
    @Test
    public void checkThrowNotConstructable() throws Exception{
    	@SuppressWarnings("unused")
		TestTypes.FakeFactory factory=new TestTypes.FakeFactory();
    	Field field=TestTypes.Test1SF.class.getField("secondString");
    	when(FakeFactory.mock.checkConstructable(field)).thenReturn(false);    	    	
    	assertThrows(RuntimeException.class,
    			()->{new ReflectionTableView<TestTypes.Test1SF>(TestTypes.Test1SF.class);});    	
    }
    
    @Test
    public void checkThrowChangesExceptionType() throws Exception{
    	@SuppressWarnings("unused")
		TestTypes.FakeFactory factory=new TestTypes.FakeFactory();
    	Field field=TestTypes.Test1SF.class.getField("secondString");
    	when(FakeFactory.mock.checkConstructable(field)).thenThrow(SecurityException.class);    	    	
    	assertThrows(RuntimeException.class,
    			()->{new ReflectionTableView<TestTypes.Test1SF>(TestTypes.Test1SF.class);}); 	
    }
    
    @Test
    public void checkStructure1S1M() throws Exception {
    	
		tableView = new ReflectionTableView<TestTypes.Test1S1M>(TestTypes.Test1S1M.class);
		
    	assertEquals(2,this.tableView.getColumns().size());
    	Object act=tableView.getColumns().get(0).getOnEditCommit();
    	assertTrue(act instanceof MemberVariable);
		assertEquals(TestTypes.Test1S1M.class.getDeclaredField("firstString"),((MemberVariable<?,?>) act).getField());
		//Could only be tested by reflection, skip it
		//assertEquals(TestTypes.Test1S1M.class.getMethod("getFirstString"),((MemberVariable<?,?>) act).getter);
		//assertEquals(TestTypes.Test1S1M.class.getMethod("setFirstString",String.class),((MemberVariable<?,?>) act).setter);
				
		act=tableView.getColumns().get(1).getOnEditCommit();
    	assertTrue(act instanceof MemberVariable);
		assertEquals(TestTypes.Test1S1M.class.getDeclaredField("firstMoney"),((MemberVariable<?,?>) act).getField());
		
    }
    
    @Test
    public void checkAddListener() throws Exception {
    	
		tableView = new ReflectionTableView<TestTypes.Test1S1M>(TestTypes.Test1S1M.class);
		
		ItemUpdateListener actListener = Mockito.mock(ItemUpdateListener.class);
		
		tableView.addEditCommitListener(actListener);
		
		Field field=MemberVariable.class.getDeclaredField("listeners");
		field.setAccessible(true);
		for(TableColumn<?, ?> act:tableView.getColumns()) {
			Object member=act.getOnEditCommit();
			assertTrue(member instanceof MemberVariable);
			@SuppressWarnings("rawtypes")
			List listeners=(List) field.get(member);
			assertTrue(listeners.contains(actListener));
		}
		field.setAccessible(false);
    }
    
    @Test
    public void checkDeleteListener() throws Exception {
    	
		tableView = new ReflectionTableView<TestTypes.Test1S1M>(null,TestTypes.Test1S1M.class);
		
		ItemUpdateListener actListener1 = Mockito.mock(ItemUpdateListener.class);
		ItemUpdateListener actListener2 = Mockito.mock(ItemUpdateListener.class);
		
		tableView.addEditCommitListener(actListener1);
		tableView.addEditCommitListener(actListener2);
		
		tableView.removeEditCommitListener(actListener1);
		
		Field field=MemberVariable.class.getDeclaredField("listeners");
		field.setAccessible(true);
		for(TableColumn<?, ?> act:tableView.getColumns()) {
			Object member=act.getOnEditCommit();
			assertTrue(member instanceof MemberVariable);
			@SuppressWarnings("rawtypes")
			List listeners=(List) field.get(member);
			assertEquals(1,listeners.size());
			assertTrue(listeners.contains(actListener2));
			assertFalse(listeners.contains(actListener1));
		}
		field.setAccessible(false);
    }    
    
    
}
