package com.quant;

import java.util.List;
import java.util.Map;

/**
 * An analysis criterion. This is used for analyzing 
 * the strategies for multiple stocks.  The difference from 
 * {@link AnalysisCriterion} is that the latter is only used 
 * for single {@link TimeSeries}.
 * <p>
 * Can be used to:
 * <ul>
 * <li>Analyze the performance of a {@link Strategy strategy}
 * <li>Compare several {@link Strategy strategies} together
 * </ul>
 */
public interface AnalysisCriterionCol {
	 /**
     * @param timeSeriesCol the collection of time series
     * @param trade a trade
     * @return the criterion value for the trade
     */
    double calculate(Map<String, TimeSeries> timeSeriesCol, Trade trade);
    
    
    /**
     * @param timeSeriesCol the collection of time series
     * @param tradingRecord a trading record
     * @return the criterion value for the trades
     */
    double calculate(Map<String, TimeSeries> timeSeriesCol, TradingRecord tradingRecrod);
    
   
    /**
     * @param timeSeriesCol  the collection of time series
     * @param strategies a list of strategies
     * @return the best strategy (among the provided ones) according to the criterion
     */
    Strategy chooseBest(TimeSeries series, List<Strategy> strategies);

    /**
     * @param criterionValue1 the first value
     * @param criterionValue2 the second value
     * @return true if the first value is better than (according to the criterion) the second one, false otherwise
     */
    boolean betterThan(double criterionValue1, double criterionValue2);
}
