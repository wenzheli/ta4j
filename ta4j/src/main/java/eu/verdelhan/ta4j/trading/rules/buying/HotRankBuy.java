package eu.verdelhan.ta4j.trading.rules.buying;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TradingRule;
import eu.verdelhan.ta4j.indicators.simple.HotRankIndicator;
import eu.verdelhan.ta4j.trading.rules.UnderIndicatorRule;

public class HotRankBuy extends TradingRule{
	
	/** once the stock appears within top K positions, then buy it */
	private int param;  
	
	public HotRankBuy(int param){
		this.param = param;
	}
	
	public void setTimeSeries(TimeSeries series){
		this.series = series;
	}
	
	@Override
	public Rule buildRule() throws Exception {
		HotRankIndicator hotRankIndicator = new HotRankIndicator(series);
		Rule rule = new UnderIndicatorRule(hotRankIndicator, Decimal.valueOf(param));
		return rule;
	}
}
