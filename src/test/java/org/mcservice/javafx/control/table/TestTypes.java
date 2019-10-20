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

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.money.MonetaryAmount;
import javax.persistence.Convert;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.javamoney.moneta.Money;
import org.mcservice.geldbericht.data.VatType;
import org.mcservice.geldbericht.data.converters.MonetaryAmountConverter;
import org.mcservice.geldbericht.data.converters.VatTypeStringConverter;
import org.mcservice.javafx.TrimStringConverter;
import org.mcservice.javafx.control.date.DayMonthFieldColumnFactory;
import org.mcservice.javafx.control.table.TableViewColumn;
import org.mcservice.javafx.control.table.TableViewColumnOrder;
import org.mcservice.javafx.control.table.TableViewConverter;
import org.mcservice.javafx.control.table.factories.ReflectionColumnFactory;
import org.mcservice.javafx.control.table.factories.SelectorColumnFactory;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;

import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;

public class TestTypes {
	
	public static <S> TableCell<S, ?> getCell(TableView<S> tableView, int columnIndex, int rowIndex) {
        TableRow<S> row = null;
        for (Node actNode : tableView.lookupAll(".table-row-cell")) {
            @SuppressWarnings("unchecked")
			TableRow<S> actRow = (TableRow<S>) actNode;
            if (actRow.getIndex() == rowIndex) {
                row = actRow;
                break;
            }
        }
        for (Node actNode : row.lookupAll(".table-cell")) {
            @SuppressWarnings("unchecked")
			TableCell<S, ?> cell = (TableCell<S, ?>) actNode;
            if (tableView.getColumns().indexOf(cell.getTableColumn()) == columnIndex) {
            	return cell;
            }
        }
        return null;
    }
	
	public static List<Text> getComboPopupList(FxRobot node) {
		Node t=node.lookup(".combo-box-popup").query();
    	Set<Node> b=t.lookupAll(".text");
    	List<Text> l=new ArrayList<Text>();
    	for (Node labeledText : b) {
			if(labeledText instanceof Text && ((Text) labeledText).getText().length()!=0) {
				l.add((Text) labeledText);
			}
		}
		return l;
	}
	
	public static List<Text> getChoicePopupList(FxRobot node) {
		Node t=node.lookup(".choice-box-popup").query();
    	Set<Node> b=t.lookupAll(".text");
    	List<Text> l=new ArrayList<Text>();
    	for (Node labeledText : b) {
			if(labeledText instanceof Text && ((Text) labeledText).getText().length()!=0) {
				l.add((Text) labeledText);
			}
		}
		return l;
	}

	public static class Test3S2I{

    	@Pattern(regexp = "[A-Za-z0-9\\-]{5}")
    	@TableViewColumn(colName="col2")
    	@TableViewColumnOrder(20)
    	private String secondString;
    	
    	@Size(min = 2, max = 256)
    	@TableViewColumn(colName="col1")
    	@TableViewColumnOrder(10)
    	@TableViewConverter(converter=TrimStringConverter.class)
    	private String firstString;

    	@Pattern(regexp = "[0-9]{10}", flags= {Pattern.Flag.CASE_INSENSITIVE,Pattern.Flag.UNICODE_CASE})
    	@TableViewColumn(colName="col3")
    	private String thirdString;

    	@Min(7)
    	@Max(999)
    	@TableViewColumn(colName="col4")
    	private int firstInt;
    	
    	@Max(7)
    	@TableViewColumn(colName="col5")
    	private Integer secondInt;
		
		public Integer getSecondInt() {
			return secondInt;
		}
		public void setSecondInt(Integer secondInt) {
			this.secondInt = secondInt;
		}
		public int getFirstInt() {
			return firstInt;
		}
		public void setFirstInt(int firstInt) {
			this.firstInt = firstInt;
		}
		public String getFirstString() {
			return firstString;
		}
		public String getSecondString() {
			return secondString;
		}
		public String getThirdString() {
			return thirdString;
		}
		public void setFirstString(String firstString) {
			this.firstString = firstString;
		}
		public void setSecondString(String secondString) {
			this.secondString = secondString;
		}
		public void setThirdString(String thirdString) {
			this.thirdString = thirdString;
		}
	}
	
	/**
	 * Fake factory, set the public static mock and define behavior using it.
	 * The factory is only a wrapper for the mock.
	 */
	public static class FakeFactory implements ReflectionColumnFactory<Test1S>{
		
		public static FakeFactory mock=null;
		
		FakeFactory(){
			if(mock==null) {
				mock=Mockito.mock(FakeFactory.class);
			}
		}

		@Override
		public boolean checkConstructable(Field field) {
			return mock.checkConstructable(field);
		}

		@Override
		public MemberVariable<Test1S, ?> addTableColumn(Field field, Class<Test1S> referenceClass,
				TableView<Test1S> table, ObjectProperty<String> memoryKeyCode) {
			return mock.addTableColumn(field, referenceClass, table, memoryKeyCode);
		}
		
	}
	
	public static class Test1SF{
		@TableViewColumn(colName="col2",fieldGenerator=FakeFactory.class)
		@TableViewConverter(converter=TrimStringConverter.class)
    	public String secondString="";
	}
	
	public static class Test3S{

    	@Pattern(regexp = "[A-Za-z0-9\\-]{5}")
    	@TableViewColumn(colName="col2")
    	@TableViewColumnOrder(20)
    	@TableViewConverter(converter=TrimStringConverter.class)
    	private String secondString="";
    	
    	@Size(min = 2, max = 256)
    	@NotNull
    	@TableViewColumn(colName="col1")
    	@TableViewColumnOrder(10)
    	@TableViewConverter(converter=TrimStringConverter.class)
    	private String firstString;

    	@Pattern(regexp = "[0-9]{10}")
    	@NotNull
    	@TableViewColumn(colName="col3")
    	@TableViewConverter(converter=TrimStringConverter.class)
    	private String thirdString;

		public String getFirstString() {
			return firstString;
		}
		public String getSecondString() {
			return secondString;
		}
		public String getThirdString() {
			return thirdString;
		}
		public void setFirstString(String firstString) {
			this.firstString = firstString;
		}
		public void setSecondString(String secondString) {
			this.secondString = secondString;
		}
		public void setThirdString(String thirdString) {
			this.thirdString = thirdString;
		}
	}

	public static class Test1S1M{

    	@NotNull
    	@TableViewColumn(colName="col1")
    	@TableViewColumnOrder(10)
    	private String firstString;
    	
    	@NotNull
    	@TableViewColumn(colName="col2")
    	@TableViewColumnOrder(20)
    	private MonetaryAmount firstMoney;

		public MonetaryAmount getFirstMoney() {
			return firstMoney;
		}
		public void setFirstMoney(MonetaryAmount firstMoney) {
			this.firstMoney = firstMoney;
		}
		public String getFirstString() {
			return firstString;
		}
		public void setFirstString(String firstString) {
			this.firstString = firstString;
		}
	}
	
	public static class Test1S{

    	@NotNull
    	@TableViewColumn(colName="col1")
    	@TableViewColumnOrder(10)
    	private String firstString;

    	public String getFirstString() {
			return firstString;
		}
		public void setFirstString(String firstString) {
			this.firstString = firstString;
		}
	}
	
	public static class Test2O{

    	private Object firstObject;
    	
    	@TableViewConverter(converter=Test3S.class)
    	private Object secondObject;

		public Object getFirstObject() {
			return firstObject;
		}

		public void setFirstObject(Object firstObject) {
			this.firstObject = firstObject;
		}
	}
	
	public static class Test1T1S1S1I1L1M1B1B{
		
		@TableViewColumn
		private Test2O var1;
		@TableViewColumn
		private String var2;
		@TableViewColumn
		private Short var3;
		@TableViewColumn
		private short var4;
		@TableViewColumn
		private Integer var5;
		@TableViewColumn
		private int var6;
		@TableViewColumn(editable=false)
		private Long var7;
		@TableViewColumn
		private long var8;
		@TableViewColumn
		private Money var9;
		@TableViewColumn
		private BigDecimal var10;
		@TableViewColumn(editable=false)
		private Boolean var11;
		@TableViewColumn(editable=true)
		private boolean var12;
		
		public Test2O getVar1() {
			return var1;
		}
		public String getVar2() {
			return var2;
		}
		public Short getVar3() {
			return var3;
		}
		public short getVar4() {
			return var4;
		}
		public Integer getVar5() {
			return var5;
		}
		public int getVar6() {
			return var6;
		}
		public Long getVar7() {
			return var7;
		}
		public long getVar8() {
			return var8;
		}
		public Money getVar9() {
			return var9;
		}
		public BigDecimal getVar10() {
			return var10;
		}
		public Boolean getVar11() {
			return var11;
		}
		public boolean isVar12() {
			return var12;
		}
		public void setVar1(Test2O var1) {
			this.var1 = var1;
		}
		public void setVar2(String var2) {
			this.var2 = var2;
		}
		public void setVar3(Short var3) {
			this.var3 = var3;
		}
		public void setVar4(short var4) {
			this.var4 = var4;
		}
		public void setVar5(Integer var5) {
			this.var5 = var5;
		}
		public void setVar6(int var6) {
			this.var6 = var6;
		}
		public void setVar7(Long var7) {
			this.var7 = var7;
		}
		public void setVar8(long var8) {
			this.var8 = var8;
		}
		public void setVar9(Money var9) {
			this.var9 = var9;
		}
		public void setVar10(BigDecimal var10) {
			this.var10 = var10;
		}
		public void setVar11(Boolean var11) {
			this.var11 = var11;
		}
		public void setVar12(boolean var12) {
			this.var12 = var12;
		}
		
		
		
	}
	
	public static class Test2I1S1M1D1V{

		@TableViewColumn(editable = false)
		private int firstInt=0;
		@TableViewColumn
		private int secondInt=0;
		@TableViewColumn
		@TableViewConverter(converter=TrimStringConverter.class)
		private String firstStr=null;
		@TableViewColumn
		@Convert(converter = MonetaryAmountConverter.class)
		private MonetaryAmount firstMoney;
		@TableViewColumn(colName="firstDay",fieldGenerator=DayMonthFieldColumnFactory.class)
		private LocalDate firstDay=null;
		@TableViewColumn(fieldGenerator=SelectorColumnFactory.class)
		@TableViewConverter(converter=VatTypeStringConverter.class)
		private VatType firstVat=null;

		public int getFirstInt() {
			return firstInt;
		}

		public MonetaryAmount getFirstMoney() {
			return firstMoney;
		}

		public String getFirstStr() {
			return firstStr;
		}

		public LocalDate getFirstDay() {
			return firstDay;
		}

		public VatType getFirstVat() {
			return firstVat;
		}

		public void setFirstInt(int firstInt) {
			this.firstInt = firstInt;
		}

		public void setFirstMoney(MonetaryAmount firstMoney) {
			this.firstMoney = firstMoney;
		}

		public void setFirstStr(String firstStr) {
			this.firstStr = firstStr;
		}

		public void setFirstDay(LocalDate firstDay) {
			this.firstDay = firstDay;
		}

		public void setFirstVat(VatType firstVat) {
			this.firstVat = firstVat;
		}

		public int getSecondInt() {
			return secondInt;
		}

		public void setSecondInt(int secondInt) {
			this.secondInt = secondInt;
		}
	}
}
