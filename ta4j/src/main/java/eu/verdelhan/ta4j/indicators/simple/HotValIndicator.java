package eu.verdelhan.ta4j.indicators.simple;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.CachedIndicator;


/**
 * Hot Value Indicator
 * <p>
 */
public class HotValIndicator extends CachedIndicator<Decimal>{
	
	private TimeSeries series;
	
	/**
	 * Constructor 
	 * @param series  the input time series
	 */
	public HotValIndicator(TimeSeries series) {
		super(series);
		this.series = series;
	}
	

	@Override
	protected Decimal calculate(int index) {
		return series.getTick(index).getHotVal();
	}
}
