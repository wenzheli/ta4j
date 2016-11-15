package com.quant;

public abstract class TradingRule {
	protected TimeSeries series;
	
	public void setTimeSeries(TimeSeries series){
		this.series = series;
	}
	
	public TradingRule(TimeSeries series){
		this.series = series;
	}
	
	public TradingRule(){}
	
	public abstract Rule buildRule() throws Exception; 	
}
