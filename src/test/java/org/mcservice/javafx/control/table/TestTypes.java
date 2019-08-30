package org.mcservice.javafx.control.table;

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

import org.mcservice.geldbericht.data.VatType;
import org.mcservice.geldbericht.data.converters.MonetaryAmountConverter;
import org.mcservice.geldbericht.data.converters.VatTypeStringConverter;
import org.mcservice.javafx.TrimStringConverter;
import org.mcservice.javafx.control.date.DayMonthFieldColumnFactory;
import org.mcservice.javafx.control.table.TableViewColumn;
import org.mcservice.javafx.control.table.TableViewColumnOrder;
import org.mcservice.javafx.control.table.TableViewConverter;
import org.mcservice.javafx.control.table.factories.SelectorColumnFactory;
import org.testfx.api.FxRobot;

import com.sun.javafx.scene.control.LabeledText;

import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

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
	
	public static List<LabeledText> getComboPopupList(FxRobot node) {
		Node t=node.lookup(".combo-box-popup").query();
    	Set<Node> b=t.lookupAll(".text");
    	List<LabeledText> l=new ArrayList<LabeledText>();
    	for (Node labeledText : b) {
			if(labeledText instanceof LabeledText && ((LabeledText) labeledText).getText().length()!=0) {
				l.add((LabeledText) labeledText);
			}
		}
		return l;
	}
	
	public static List<LabeledText> getChoicePopupList(FxRobot node) {
		Node t=node.lookup(".choice-box-popup").query();
    	Set<Node> b=t.lookupAll(".text");
    	List<LabeledText> l=new ArrayList<LabeledText>();
    	for (Node labeledText : b) {
			if(labeledText instanceof LabeledText && ((LabeledText) labeledText).getText().length()!=0) {
				l.add((LabeledText) labeledText);
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
