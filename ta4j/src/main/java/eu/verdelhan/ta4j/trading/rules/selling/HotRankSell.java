package eu.verdelhan.ta4j.trading.rules.selling;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TradingRule;
import eu.verdelhan.ta4j.indicators.simple.HotRankIndicator;
import eu.verdelhan.ta4j.trading.rules.OverIndicatorRule;

public class HotRankSell extends TradingRule{

	private int param;
	
	public HotRankSell(int param){
		this.param = param;
	}
	
	public void setHotRankSell(TimeSeries series){
		this.series = series;
	}
	
	@Override
	public Rule buildRule() throws Exception {
		HotRankIndicator hotRankIndicator = new HotRankIndicator(series);
		Rule rule = new OverIndicatorRule(hotRankIndicator, Decimal.valueOf(param));
		return rule;
	}
}
