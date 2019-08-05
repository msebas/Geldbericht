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
	
	ReflectionTableView<TestTypes.Test3S2I> tableView=null;
	
	@BeforeEach
	public void setUp() {
		@SuppressWarnings("unused")
		JFXPanel dummy=new JFXPanel(); 
	}
        
    @Test
    public void checkStructure() throws Exception {
    	
		tableView = new ReflectionTableView<TestTypes.Test3S2I>(TestTypes.Test3S2I.class);
		
    	assertEquals(5,this.tableView.getColumns().size());
    	Object act=tableView.getColumns().get(0).getOnEditCommit();
    	assertTrue(act instanceof MemberVariable);
		assertEquals(TestTypes.Test3S2I.class.getDeclaredField("firstString"),((MemberVariable<?,?>) act).field);
		assertEquals(TestTypes.Test3S2I.class.getMethod("getFirstString"),((MemberVariable<?,?>) act).getter);
		assertEquals(TestTypes.Test3S2I.class.getMethod("setFirstString",String.class),((MemberVariable<?,?>) act).setter);
		
		act=tableView.getColumns().get(1).getOnEditCommit();
    	assertTrue(act instanceof MemberVariable);
		assertEquals(TestTypes.Test3S2I.class.getDeclaredField("secondString"),((MemberVariable<?,?>) act).field);
		
		act=tableView.getColumns().get(2).getOnEditCommit();
    	assertTrue(act instanceof MemberVariable);
		assertEquals(TestTypes.Test3S2I.class.getDeclaredField("thirdString"),((MemberVariable<?,?>) act).field);
		
		act=tableView.getColumns().get(3).getOnEditCommit();
    	assertTrue(act instanceof MemberVariable);
		assertEquals(TestTypes.Test3S2I.class.getDeclaredField("firstInt"),((MemberVariable<?,?>) act).field);
		
		act=tableView.getColumns().get(4).getOnEditCommit();
    	assertTrue(act instanceof MemberVariable);
		assertEquals(TestTypes.Test3S2I.class.getDeclaredField("secondInt"),((MemberVariable<?,?>) act).field);
		
    	//tableView.getColumns().get(0).getCellValueFactory(new PropertyValueFactory<S,String>(field.getName()));
    	//tableView.getColumns().get(0).getCellFactory(ValidatingTextFieldTableCell.forTableColumn(formatter,this.memoryKeyCode));
    }
    
    
    
}
