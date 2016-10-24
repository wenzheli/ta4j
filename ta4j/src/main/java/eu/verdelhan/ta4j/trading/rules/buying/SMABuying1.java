package eu.verdelhan.ta4j.trading.rules.buying;

import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.TradingRule;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.simple.PreviousIndicator;
import eu.verdelhan.ta4j.indicators.trackers.SMAIndicator;
import eu.verdelhan.ta4j.trading.rules.AbstractRule;
import eu.verdelhan.ta4j.trading.rules.OverIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.UnderIndicatorRule;

public class SMABuying1 extends TradingRule{
	
	/** average period for the first line, i.e. MA5 */ 
	private int param1;     
	/** average period for the second line, i.e. MA10 */
	private int param2;
	/** average period for the third line, i.e MA30  */
	private int param3;
	/** the length of period to consider for each line, the default value is 1 */ 
	private int period = 1;   
	
	public SMABuying1(){
	}
	
	public SMABuying1(int param1, int param2, int param3){
		this.setParams(param1, param2, param3);
	}
	
	public SMABuying1(int param1, int param2, int param3, int period){
		this.setParams(param1, param2, param3);
		this.setPeriod(period);
	}
	
	public SMABuying1(TimeSeries series, int param1, int param2, int param3){
		super(series);
		this.setParams(param1, param2, param3);
	}
	
		
	public void setParams(int param1, int param2, int param3){
		this.param1 = param1;
		this.param2 = param2;
		this.param3 = param3;
	}

	
	public void setTimeSeries(TimeSeries series){
		this.series = series;
	}
	
	public void setPeriod(int period){
		this.period = period;
	}
	
	
	@Override
	public Rule buildRule() throws Exception {
		
		if (param1 == 0 || param2 == 0 || param3 == 0 || series == null){
			throw new Exception("There are one or more params are not initialzied!");
		}
		
		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		SMAIndicator smaIndicator1 = new SMAIndicator(closePrice, param1);
		SMAIndicator smaIndicator2 = new SMAIndicator(closePrice, param2);
		SMAIndicator smaIndicator3 = new SMAIndicator(closePrice,param3);
			
		Rule rule = new OverIndicatorRule(smaIndicator1, smaIndicator2)
				.and(new OverIndicatorRule(smaIndicator2, smaIndicator3));
		
		for (int i = 1; i <= period; i++){
			rule = rule.and(new OverIndicatorRule(new PreviousIndicator(smaIndicator1, i-1), 
							new PreviousIndicator(smaIndicator1, i)))
						.and(new OverIndicatorRule(new PreviousIndicator(smaIndicator2, i-1), 
							new PreviousIndicator(smaIndicator3, i)))
						.and(new OverIndicatorRule(new PreviousIndicator(smaIndicator3, i-1), 
							new PreviousIndicator(smaIndicator3, i)));
		}
		
		return rule;
	}
}
