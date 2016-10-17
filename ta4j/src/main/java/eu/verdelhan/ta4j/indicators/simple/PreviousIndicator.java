package eu.verdelhan.ta4j.indicators.simple;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.indicators.CachedIndicator;

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
