package com.quant.indicators.simple;

import com.quant.Decimal;
import com.quant.Indicator;
import com.quant.indicators.CachedIndicator;

public class PreviousIndicator extends CachedIndicator<Decimal>{
	private Indicator<Decimal> indicator;
	private int days_before;
	
	public PreviousIndicator(Indicator<Decimal> indicator, int days_before){
		super(indicator);
		this.indicator = indicator;
		this.days_before = days_before;
	}
	
	@Override
	public Decimal calculate(int index){
		return indicator.getValue(Math.max(0, index - days_before));
	}
}
