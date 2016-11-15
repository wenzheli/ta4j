package com.quant.trading.rules.buying;

import com.quant.Decimal;
import com.quant.Rule;
import com.quant.TimeSeries;
import com.quant.TradingRule;
import com.quant.indicators.simple.HotRankIndicator;
import com.quant.trading.rules.UnderIndicatorRule;

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
