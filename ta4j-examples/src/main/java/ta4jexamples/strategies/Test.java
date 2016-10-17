package ta4jexamples.strategies;

import java.io.IOException;

/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Marc de Verdelhan & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Order.OrderType;
import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TimeSeriesRepo;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.analysis.criteria.TotalProfitCriterion;
import eu.verdelhan.ta4j.factory.TimeSeriesRepoBuilder;
import eu.verdelhan.ta4j.indicators.oscillators.CCIIndicator;
import eu.verdelhan.ta4j.trading.rules.OverIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.UnderIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.WaitForRule;
import eu.verdelhan.ta4j.trading.rules.buying.SMABuying1;
import eu.verdelhan.ta4j.trading.rules.buying.VOLBuying1;
import eu.verdelhan.ta4j.trading.rules.selling.SMASelling1;
import ta4jexamples.loaders.CsvTradesLoader;

/**
 * CCI Correction Strategy
 * <p>
 * @see http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:cci_correction
 */
public class Test {

    /**
     * @param series a time series
     * @return a CCI correction strategy
     */
    public static Strategy buildStrategy(TimeSeries series) {
        
        Rule entryRule = new VOLBuying1(series, 5, 10, 30).buildRule();
        
        //Rule exitRule = new WaitForRule(OrderType.BUY, 2);
        
        Rule exitRule = new SMASelling1(series, 5, 10, 30).buildRule();
        
        Strategy strategy = new Strategy(entryRule, exitRule);
        strategy.setUnstablePeriod(5);
        return strategy;  
    } 

    public static void main(String[] args) throws IOException {
    	
    	String path = "/Users/liwenzhe/Documents/workspace/DataWrapper/data/stocks";
		TimeSeriesRepoBuilder builder = new TimeSeriesRepoBuilder(path);
		TimeSeriesRepo repo = builder.build();
        // Getting the time series
        //TimeSeries series = CsvTradesLoader.loadBitstampSeries();
        //TimeSeries series = repo.get(0);
        // Building the trading strategy
        
		for (TimeSeries series : repo.getTimeSeries()){
			Strategy strategy = buildStrategy(series);
	        
	        // Running the strategy
	        TradingRecord tradingRecord = series.run(strategy);
	        System.out.println("Number of trades for the strategy: " + tradingRecord.getTradeCount());
	        // Analysis
	        System.out.println("Total profit for the strategy: " + new TotalProfitCriterion().calculate(series, tradingRecord));
		}
		
		
        
    }
}
