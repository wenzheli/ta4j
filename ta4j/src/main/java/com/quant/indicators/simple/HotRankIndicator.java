package com.quant.indicators.simple;

import com.quant.Decimal;
import com.quant.TimeSeries;
import com.quant.indicators.CachedIndicator;


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
