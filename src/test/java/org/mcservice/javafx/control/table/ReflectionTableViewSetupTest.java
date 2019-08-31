package org.mcservice.javafx.control.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mcservice.javafx.control.table.MemberVariable;
import org.mcservice.javafx.control.table.ReflectionTableView;

import javafx.embed.swing.JFXPanel;

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
    
    
    
}
