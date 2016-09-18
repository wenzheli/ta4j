package eu.verdelhan.ta4j.indicators.simple;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.CachedIndicator;


/**
 * Hot rank indicator
 * <p>
 */
public class HotRankIndicator extends CachedIndicator<Decimal>{
	
	private TimeSeries series;
	
	/**
	 * Constructor
	 * @param series  the input time series
	 */
	public HotRankIndicator(TimeSeries series) {
		super(series);
		this.series = series;
	}
	
	@Override
	protected Decimal calculate(int index){
		return series.getTick(index).getHotRank();
	}
}
