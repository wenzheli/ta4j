package eu.verdelhan.ta4j.indicators.simple;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.indicators.CachedIndicator;

public class PreviousIndicator extends CachedIndicator<Decimal>{
	private Indicator<Decimal> indicator;
	
	public PreviousIndicator(Indicator<Decimal> indicator){
		super(indicator);
		this.indicator = indicator;
	}
	
	@Override
	public Decimal calculate(int index){
		return indicator.getValue(index-1);
	}
}
