package eu.verdelhan.ta4j.trading.rules.selling;

import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TradingRule;
import eu.verdelhan.ta4j.indicators.simple.PreviousIndicator;
import eu.verdelhan.ta4j.indicators.simple.VolumeIndicator;
import eu.verdelhan.ta4j.indicators.trackers.SMAIndicator;
import eu.verdelhan.ta4j.trading.rules.UnderIndicatorRule;

public class VOLMultipleDown extends TradingRule{
	
	/** average period for the first line, i.e. MA5 */ 
	private int param1;     
	/** average period for the second line, i.e. MA10 */
	private int param2;
	/** average period for the third line, i.e MA30  */
	private int param3;
	/** the length of period to consider for each line, the default value is 1 */ 
	private int period = 1;   
	
	
	public VOLMultipleDown(){
		
	}
	
	public VOLMultipleDown(int param1, int param2, int param3){
		this.setParams(param1, param2, param3);
	}
	
	public VOLMultipleDown(int param1, int param2, int param3, int period){
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
	
	public VOLMultipleDown(TimeSeries series, int param1, int param2, int param3){
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
		
		
		Rule rule = new UnderIndicatorRule(volume, volume1)
				.and(new UnderIndicatorRule(volume1, volume2))
				.and(new UnderIndicatorRule(volume2, volume3));
		
		for (int i = 1; i <= period; i++){
			rule = rule.and(new UnderIndicatorRule(new PreviousIndicator(volume, i-1), 
							new PreviousIndicator(volume, i)))
						.and(new UnderIndicatorRule(new PreviousIndicator(volume1, i-1), 
							new PreviousIndicator(volume1, i)))
						.and(new UnderIndicatorRule(new PreviousIndicator(volume2, i-1), 
							new PreviousIndicator(volume2, i)))
						.and(new UnderIndicatorRule(new PreviousIndicator(volume3, i-1), 
							new PreviousIndicator(volume3, i)));
		}
		
		
		return rule;
	}
}
