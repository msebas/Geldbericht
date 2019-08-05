package org.mcservice.javafx.control.table;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.mcservice.javafx.TrimStringConverter;
import org.mcservice.javafx.control.table.TableViewColumn;
import org.mcservice.javafx.control.table.TableViewColumnOrder;
import org.mcservice.javafx.control.table.TableViewConverter;

public class TestTypes {

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
}
