package org.mcservice.javafx.control.table.factories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mcservice.javafx.control.table.ItemUpdateProvider;
import org.mcservice.javafx.control.table.MemberVariable;
import org.mcservice.javafx.control.table.TableBooleanPropertyFactory;
import org.mcservice.javafx.control.table.TestTypes;
import org.mcservice.javafx.control.table.TestTypes.Test1T1S1S1I1L1M1B1B;
import org.mcservice.javafx.control.table.factories.DefaultReflectionColumnFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

class DefaultReflectionColumnFactoryTest extends ApplicationTest{
	
	@Override 
	public void start(Stage stage) throws Exception {
		Scene scene = new Scene(new Group());
				
        stage.setScene(scene);
        stage.show();
    }
	
	public static Stream<Arguments> getTypes() {
		Arguments[] args=new Arguments[Test1T1S1S1I1L1M1B1B.class.getDeclaredFields().length];
		for (int i = 0; i < args.length; i++) {
			if (Test1T1S1S1I1L1M1B1B.class.getDeclaredFields()[i].getName().startsWith("var")) {
				args[i]=Arguments.of(Test1T1S1S1I1L1M1B1B.class.getDeclaredFields()[i]);
			}
		}
		
		return Stream.of(args);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ParameterizedTest
    @MethodSource("getTypes")
	void testAddTableColumn(Field field) {
		if(null==field) {
			//This happens if some hidden special fields are present in the class
			return;
		}
		DefaultReflectionColumnFactory<Test1T1S1S1I1L1M1B1B> factory=
				new DefaultReflectionColumnFactory<Test1T1S1S1I1L1M1B1B>();
		ItemUpdateProvider member=null;
		TableView<Test1T1S1S1I1L1M1B1B> table=Mockito.mock(TableView.class);
		ObservableList<TableColumn<Test1T1S1S1I1L1M1B1B, ?>> colList=Mockito.mock(ObservableList.class);
		Mockito.when(table.getColumns()).thenReturn(colList);
		
		if(field.getType()==TestTypes.Test2O.class) {
			assertThrows(RuntimeException.class,() -> factory.addTableColumn(field, Test1T1S1S1I1L1M1B1B.class, table, null));
			Mockito.verifyNoMoreInteractions(colList,table);
		} else {
			member=factory.addTableColumn(field, Test1T1S1S1I1L1M1B1B.class, table, null);
			Mockito.verify(table).getColumns();
			
			
			ArgumentCaptor<TableColumn> addCaptor = ArgumentCaptor.forClass(TableColumn.class);
			Mockito.verify(colList).add(addCaptor.capture());

			List<TableColumn> adds = addCaptor.getAllValues();
			assertEquals(1,adds.size());
			assertTrue(adds.get(0).getCellValueFactory() instanceof PropertyValueFactory);
			assertEquals(field.getName(),((PropertyValueFactory) adds.get(0).getCellValueFactory()).getProperty());
			
			
			if(field.getName().equals("var11") || field.getName().equals("var12")) {
				assertEquals(field.getName().equals("var12"),adds.get(0).isEditable());
				assertNotNull(member);
				assertTrue(member instanceof TableBooleanPropertyFactory);
			} else {
				if(field.getName().equals("var7")) {
					assertNull(member);
					assertFalse(adds.get(0).isEditable());
				} else {
					assertTrue(adds.get(0).isEditable());
					assertNotNull(member);
					assertEquals(field,((MemberVariable) member).getField());
				}
				
			}
		}
	}

}
