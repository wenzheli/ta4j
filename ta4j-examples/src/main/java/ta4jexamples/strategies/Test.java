package ta4jexamples.strategies;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.quant.Decimal;
import com.quant.Rule;
import com.quant.Strategy;
import com.quant.TimeSeries;
import com.quant.TimeSeriesRepo;
import com.quant.TradingRecord;
import com.quant.Order.OrderType;
import com.quant.analysis.criteria.TotalProfitCriterion;
import com.quant.factory.TimeSeriesRepoBuilder;
import com.quant.indicators.helpers.HighestValueIndicator;
import com.quant.indicators.helpers.LowestValueIndicator;
import com.quant.indicators.oscillators.CCIIndicator;
import com.quant.indicators.oscillators.StochasticOscillatorKIndicator;
import com.quant.indicators.simple.ClosePriceIndicator;
import com.quant.indicators.simple.MaxPriceIndicator;
import com.quant.indicators.simple.MinPriceIndicator;
import com.quant.indicators.simple.MultiplierIndicator;
import com.quant.indicators.trackers.EMAIndicator;
import com.quant.indicators.trackers.MACDIndicator;
import com.quant.indicators.trackers.RSIIndicator;
import com.quant.indicators.trackers.SMAIndicator;
import com.quant.trading.rules.CrossedDownIndicatorRule;
import com.quant.trading.rules.CrossedUpIndicatorRule;
import com.quant.trading.rules.OverIndicatorRule;
import com.quant.trading.rules.UnderIndicatorRule;
import com.quant.trading.rules.WaitForRule;
import com.quant.trading.rules.buying.SMAMultipleUp;
import com.quant.trading.rules.buying.VOLMultipleUp;
import com.quant.trading.rules.selling.SMAMultipleDown;

import ta4jexamples.loaders.CsvTradesLoader;

/**
 * CCI Correction Strategy
 * <p>
 * @see http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:cci_correction
 */
public class Test {
	
	// We assume that there were at least one trade every 5 minutes during the whole week
    private static final int NB_TICKS_PER_WEEK = 12 * 24 * 7;

	
    /**
     * Build list of strategies. 
     * @param series   the time series
     * @return
     * @throws Exception 
     */
	public static List<Strategy> buildStrategies(TimeSeries series) throws Exception{
		List<Strategy> strategies = new ArrayList<Strategy>();
		strategies.add(SMAStrategy(series));
		strategies.add(VOLStrategy(series));
		strategies.add(RSI2Strategy(series));
		strategies.add(MovingMomentumStrategy(series));
		strategies.add(GlobalExtremaStrategy(series));
		strategies.add(CCICorrectionStrategy(series));
		
		return strategies;
		
	}
		
	
	public static Strategy RSI2Strategy(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
        SMAIndicator longSma = new SMAIndicator(closePrice, 200);

        // We use a 2-period RSI indicator to identify buying
        // or selling opportunities within the bigger trend.
        RSIIndicator rsi = new RSIIndicator(closePrice, 2);
        
        // Entry rule
        // The long-term trend is up when a security is above its 200-period SMA.
        Rule entryRule = new OverIndicatorRule(shortSma, longSma) // Trend
                .and(new CrossedDownIndicatorRule(rsi, Decimal.valueOf(5))) // Signal 1
                .and(new OverIndicatorRule(shortSma, closePrice)); // Signal 2
        
        // Exit rule
        // The long-term trend is down when a security is below its 200-period SMA.
        Rule exitRule = new UnderIndicatorRule(shortSma, longSma) // Trend
                .and(new CrossedUpIndicatorRule(rsi, Decimal.valueOf(95))) // Signal 1
                .and(new UnderIndicatorRule(shortSma, closePrice)); // Signal 2
        
        // TODO: Finalize the strategy
        
        return new Strategy(entryRule, exitRule);
	}
        		
	
	public static Strategy MovingMomentumStrategy(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        
        // The bias is bullish when the shorter-moving average moves above the longer moving average.
        // The bias is bearish when the shorter-moving average moves below the longer moving average.
        EMAIndicator shortEma = new EMAIndicator(closePrice, 9);
        EMAIndicator longEma = new EMAIndicator(closePrice, 26);

        StochasticOscillatorKIndicator stochasticOscillK = new StochasticOscillatorKIndicator(series, 14);

        MACDIndicator macd = new MACDIndicator(closePrice, 9, 26);
        EMAIndicator emaMacd = new EMAIndicator(macd, 18);
        
        // Entry rule
        Rule entryRule = new OverIndicatorRule(shortEma, longEma) // Trend
                .and(new CrossedDownIndicatorRule(stochasticOscillK, Decimal.valueOf(20))) // Signal 1
                .and(new OverIndicatorRule(macd, emaMacd)); // Signal 2
        
        // Exit rule
        Rule exitRule = new UnderIndicatorRule(shortEma, longEma) // Trend
                .and(new CrossedUpIndicatorRule(stochasticOscillK, Decimal.valueOf(80))) // Signal 1
                .and(new UnderIndicatorRule(macd, emaMacd)); // Signal 2
        
        return new Strategy(entryRule, exitRule);
    }
	
	
	public static Strategy GlobalExtremaStrategy(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrices = new ClosePriceIndicator(series);

        // Getting the max price over the past week
        MaxPriceIndicator maxPrices = new MaxPriceIndicator(series);
        HighestValueIndicator weekMaxPrice = new HighestValueIndicator(maxPrices, NB_TICKS_PER_WEEK);
        // Getting the min price over the past week
        MinPriceIndicator minPrices = new MinPriceIndicator(series);
        LowestValueIndicator weekMinPrice = new LowestValueIndicator(minPrices, NB_TICKS_PER_WEEK);

        // Going long if the close price goes below the min price
        MultiplierIndicator downWeek = new MultiplierIndicator(weekMinPrice, Decimal.valueOf("1.004"));
        Rule buyingRule = new UnderIndicatorRule(closePrices, downWeek);

        // Going short if the close price goes above the max price
        MultiplierIndicator upWeek = new MultiplierIndicator(weekMaxPrice, Decimal.valueOf("0.996"));
        Rule sellingRule = new OverIndicatorRule(closePrices, upWeek);

        return new Strategy(buyingRule, sellingRule);
    }
	
	public static Strategy CCICorrectionStrategy(TimeSeries series){
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        CCIIndicator longCci = new CCIIndicator(series, 200);
        CCIIndicator shortCci = new CCIIndicator(series, 5);
        Decimal plus100 = Decimal.HUNDRED;
        Decimal minus100 = Decimal.valueOf(-100);
        
        Rule entryRule = new OverIndicatorRule(longCci, plus100) // Bull trend
                .and(new UnderIndicatorRule(shortCci, minus100)); // Signal
        
        Rule exitRule = new UnderIndicatorRule(longCci, minus100) // Bear trend
                .and(new OverIndicatorRule(shortCci, plus100)); // Signal
        
        Strategy strategy = new Strategy(entryRule, exitRule);
        strategy.setUnstablePeriod(5);
        return strategy;
	}
	
    /**
     * @param series a time series
     * @return a CCI correction strategy
     * @throws Exception 
     */
    public static Strategy SMAStrategy(TimeSeries series) throws Exception {
        
        Rule entryRule = new SMAMultipleUp(series, 5, 10, 30).buildRule();
        
        //Rule exitRule = new WaitForRule(OrderType.BUY, 2);
        
        Rule exitRule = new SMAMultipleDown(series, 5, 10, 30).buildRule();
        
        Strategy strategy = new Strategy(entryRule, exitRule);
        strategy.setUnstablePeriod(5);
        return strategy;  
    } 
    
    
    public static Strategy VOLStrategy(TimeSeries series) throws Exception{
    	Rule entryRule = new VOLMultipleUp(series, 5, 10, 20).buildRule();
    	Rule exitRule = new SMAMultipleDown(series, 5, 10, 30).buildRule();
    	
    	Strategy strategy = new Strategy(entryRule, exitRule);
    	strategy.setUnstablePeriod(5);
    	return strategy;
    }
    

    public static void main(String[] args) throws Exception {
    	
    	String path = "/Users/liwenzhe/Documents/workspace/DataWrapper/data/stocks";
		TimeSeriesRepoBuilder builder = new TimeSeriesRepoBuilder(path);
		TimeSeriesRepo repo = builder.build();
        // Getting the time series
        //TimeSeries series = CsvTradesLoader.loadBitstampSeries();
        //TimeSeries series = repo.get(0);
        // Building the trading strategy
        
		BufferedWriter out = new BufferedWriter(new FileWriter("result.txt"));
		
		for (TimeSeries series : repo.getTimeSeries()){
			out.write(series.getName() + " ");
			System.out.print(series.getName() + " ");
			for (Strategy strategy : buildStrategies(series)){
				// Running the strategy
		        TradingRecord tradingRecord = series.run(strategy);
		        //System.out.println("Number of trades for the strategy: " + tradingRecord.getTradeCount());
		        // Analysis
		        double profit = new TotalProfitCriterion().calculate(series, tradingRecord);
		        //System.out.println("Total profit for the strategy: " + new TotalProfitCriterion().calculate(series, tradingRecord));
		        //out.write(Double.toString(profit * 21/series.getMaximumTickCount()) + "\\t"); // average profit for month
		        System.out.print(Double.toString(profit) + " ");
		        out.write(Double.toString(profit) + " ");     
			}
			System.out.println();
			out.newLine();
		}
		
		
		out.close();
		
		
        
    }
}
