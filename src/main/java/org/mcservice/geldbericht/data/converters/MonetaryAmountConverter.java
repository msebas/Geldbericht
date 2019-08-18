package org.mcservice.geldbericht.data.converters;

import java.math.BigDecimal;

import javax.money.MonetaryAmount;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import javax.money.NumberValue;
import org.javamoney.moneta.Money;

@Converter
public class MonetaryAmountConverter implements AttributeConverter<MonetaryAmount, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(MonetaryAmount attribute) {
    	NumberValue num = attribute.getNumber();
		return num.numberValue(BigDecimal.class);    	
    }

    @Override
    public MonetaryAmount convertToEntityAttribute(BigDecimal dbData) {
		return Money.of(dbData,"EUR");
    	
    }
}