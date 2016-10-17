package eu.verdelhan.ta4j.trading.rules.buying;


import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.simple.VolumeIndicator;
import eu.verdelhan.ta4j.indicators.trackers.SMAIndicator;
import eu.verdelhan.ta4j.trading.rules.OverIndicatorRule;

public class VOLBuying1 {
	
	private TimeSeries series; 
	private final int param1;
	private final int param2;
	private final int param3;
	
	public VOLBuying1(TimeSeries series, int param1, int param2, int param3){
		this.series = series;
		this.param1 = param1;
		this.param2 = param2;
		this.param3 = param3;
	}
	
	public Rule buildRule(){
		VolumeIndicator volume = new VolumeIndicator(series);
		SMAIndicator volume_5 = new SMAIndicator(volume, param1);
		SMAIndicator volume_10 = new SMAIndicator(volume, param2);
		SMAIndicator volume_30 = new SMAIndicator(volume,param3);
		
		
		Rule rule = new OverIndicatorRule(volume, volume_5)
				.and(new OverIndicatorRule(volume_5, volume_10))
				.and(new OverIndicatorRule(volume_10, volume_30));
		
		return rule;
	}
}
