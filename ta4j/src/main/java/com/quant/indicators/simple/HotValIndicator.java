package com.quant.indicators.simple;

import com.quant.Decimal;
import com.quant.TimeSeries;
import com.quant.indicators.CachedIndicator;


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
