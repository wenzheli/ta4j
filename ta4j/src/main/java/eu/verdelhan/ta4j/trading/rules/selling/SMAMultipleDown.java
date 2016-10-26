package eu.verdelhan.ta4j.trading.rules.selling;

import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.simple.PreviousIndicator;
import eu.verdelhan.ta4j.indicators.trackers.SMAIndicator;
import eu.verdelhan.ta4j.trading.rules.OverIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.UnderIndicatorRule;

public class SMAMultipleDown {
	
	private TimeSeries series; 
	private final int param1;
	private final int param2;
	private final int param3;
	
	public SMAMultipleDown(TimeSeries series, int param1, int param2, int param3){
		this.series = series;
		this.param1 = param1;
		this.param2 = param2;
		this.param3 = param3;
	}
	
	public Rule buildRule(){
		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		SMAIndicator smaIndicator_5 = new SMAIndicator(closePrice, param1);
		SMAIndicator smaIndicator_10 = new SMAIndicator(closePrice, param2);
		SMAIndicator smaIndicator_30 = new SMAIndicator(closePrice,param3);
		
		PreviousIndicator smaPrevIndicator_5 = new PreviousIndicator(smaIndicator_5, 1);
		PreviousIndicator smaPrevIndicator_10 = new PreviousIndicator(smaIndicator_10, 1);
		PreviousIndicator smaPrevIndicator_30 = new PreviousIndicator(smaIndicator_30, 1);
		
		Rule rule = new UnderIndicatorRule(smaIndicator_5, smaIndicator_10)
				.and(new UnderIndicatorRule(smaIndicator_10, smaIndicator_30))
				.and(new UnderIndicatorRule(smaIndicator_5, smaPrevIndicator_5))
				.and(new UnderIndicatorRule(smaIndicator_10, smaPrevIndicator_10))
				.and(new UnderIndicatorRule(smaIndicator_30, smaPrevIndicator_30)); 
		
		return rule;
	}
}
