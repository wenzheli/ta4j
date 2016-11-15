package com.quant.trading.rules.buying;


import com.quant.Rule;
import com.quant.TimeSeries;
import com.quant.TradingRule;
import com.quant.indicators.simple.PreviousIndicator;
import com.quant.indicators.simple.VolumeIndicator;
import com.quant.indicators.trackers.SMAIndicator;
import com.quant.trading.rules.OverIndicatorRule;

public class VOLMultipleUp extends TradingRule{
	
	/** average period for the first line, i.e. MA5 */ 
	private int param1;     
	/** average period for the second line, i.e. MA10 */
	private int param2;
	/** average period for the third line, i.e MA30  */
	private int param3;
	/** the length of period to consider for each line, the default value is 1 */ 
	private int period = 1;   
	
	
	public VOLMultipleUp(){
		
	}
	
	public VOLMultipleUp(int param1, int param2, int param3){
		this.setParams(param1, param2, param3);
	}
	
	public VOLMultipleUp(int param1, int param2, int param3, int period){
		this.setParams(param1, param2, param3);
		this.setPeriod(period);
	}
	
	private void setPeriod(int period){
		this.period = period;
	}
	
	public void setParams(int param1, int param2, int param3){
		this.param1 = param1;
		this.param2 = param2;
		this.param3 = param3;
	}
	
	public void setTimeSeries(TimeSeries series){
		this.series = series;
	}
	
	public VOLMultipleUp(TimeSeries series, int param1, int param2, int param3){
		this.series = series;
		this.setParams(param1, param2, param3);
	}
	
	@Override
	public Rule buildRule() throws Exception{
		
		if (param1 == 0 || param2 == 0 || param3 == 0 || series == null){
			throw new Exception("There are one or more params are not initialzied!");
		}
		
		VolumeIndicator volume = new VolumeIndicator(series);
		SMAIndicator volume1 = new SMAIndicator(volume, param1);
		SMAIndicator volume2 = new SMAIndicator(volume, param2);
		SMAIndicator volume3 = new SMAIndicator(volume,param3);
		
		
		Rule rule = new OverIndicatorRule(volume, volume1)
				.and(new OverIndicatorRule(volume1, volume2))
				.and(new OverIndicatorRule(volume2, volume3));
		
		for (int i = 1; i <= period; i++){
			rule = rule.and(new OverIndicatorRule(new PreviousIndicator(volume, i-1), 
							new PreviousIndicator(volume, i)))
						.and(new OverIndicatorRule(new PreviousIndicator(volume1, i-1), 
							new PreviousIndicator(volume1, i)))
						.and(new OverIndicatorRule(new PreviousIndicator(volume2, i-1), 
							new PreviousIndicator(volume2, i)))
						.and(new OverIndicatorRule(new PreviousIndicator(volume3, i-1), 
							new PreviousIndicator(volume3, i)));
		}
		
		
		return rule;
	}
}
