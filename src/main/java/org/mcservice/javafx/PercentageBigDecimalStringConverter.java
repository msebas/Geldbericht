package org.mcservice.javafx;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;

import javafx.util.StringConverter;

/**
 * {@link StringConverter} implementing a convrsion from {@link String} to 
 * {@link BigDecimal} assuming that the {@link BigDecimal} is a percentage
 * value. If converted from {@link BigDecimal} the value is multiplied by
 * 100 and presented with an appended %. In the other direction the formats
 * ""
 */
public class PercentageBigDecimalStringConverter extends StringConverter<BigDecimal> {
	
	private static DecimalFormat subFormatter = (DecimalFormat) DecimalFormat.getInstance();
	private static String perCent=String.valueOf(subFormatter.getDecimalFormatSymbols().getPercent());
	private static String perMill=String.valueOf(subFormatter.getDecimalFormatSymbols().getPerMill());
	private static String divider=String.valueOf(subFormatter.getDecimalFormatSymbols().getDecimalSeparator());

	/** {@inheritDoc} */
    @Override 
    public String toString(BigDecimal value) {
    	return subFormatter.format(value.multiply(new BigDecimal(100))).concat(" %");
    }

    /** {@inheritDoc} */
    @Override 
    public BigDecimal fromString(String value) {
    	if(value==null)
    		return null;
    	value=value.trim();
    	int fact=1;
    	if (value.endsWith(perCent)) {
    		fact=100;
    		value=value.substring(0, value.length()-1);
    	} else if (value.endsWith(perMill)) {
    		fact=1000;
    		value=value.substring(0, value.length()-1);
    	}
    	//For anyone asking why not use directly the formatter: The precision might be undefined
    	String[] valueParts=value.split(divider);
    	if(valueParts.length>2) {
    		throw new NumberFormatException("Decimal seperator found too often.");
    	}
    	try {
	    	Number fullNumber=subFormatter.parse(valueParts[0]);
	    	BigDecimal result=new BigDecimal(fullNumber.longValue());
	    	if(valueParts.length>1) {
	    		Number decimalNumber=subFormatter.parse(valueParts[1]);
	    		BigDecimal decimalResult=new BigDecimal(decimalNumber.longValue());
	    		decimalResult=decimalResult.divide(
	    				new BigDecimal(String.format("1e%d",valueParts[1].strip().length())));
	    		
	    		result=result.add(decimalResult);
	    	}
	    	result=result.divide(new BigDecimal(fact));
	    	return result;
    	} catch(ParseException e) {
    		throw new RuntimeException(e);
    	}
    	
    	
    }

}
