package eu.verdelhan.ta4j.trading.rules.buying;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.TradingRule;
import eu.verdelhan.ta4j.indicators.helpers.HighestValueIndicator;
import eu.verdelhan.ta4j.indicators.helpers.LowestValueIndicator;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.simple.MaxPriceIndicator;
import eu.verdelhan.ta4j.indicators.simple.MinPriceIndicator;
import eu.verdelhan.ta4j.indicators.simple.MultiplierIndicator;
import eu.verdelhan.ta4j.trading.rules.UnderIndicatorRule;

public class GlobalExtremaBuy extends TradingRule{
	private static final int NB_TICKS_PER_WEEK = 12 * 24 * 7;
	
	public GlobalExtremaBuy(){
		
	}
	
	public Rule buildRule(){
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
        
        return buyingRule;
	}
}
