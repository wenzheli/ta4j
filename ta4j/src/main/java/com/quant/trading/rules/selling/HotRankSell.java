package com.quant.trading.rules.selling;

import com.quant.Decimal;
import com.quant.Rule;
import com.quant.TimeSeries;
import com.quant.TradingRule;
import com.quant.indicators.simple.HotRankIndicator;
import com.quant.trading.rules.OverIndicatorRule;

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
