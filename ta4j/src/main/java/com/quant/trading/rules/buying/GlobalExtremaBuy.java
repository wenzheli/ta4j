package com.quant.trading.rules.buying;

import com.quant.Decimal;
import com.quant.Rule;
import com.quant.TradingRule;
import com.quant.indicators.helpers.HighestValueIndicator;
import com.quant.indicators.helpers.LowestValueIndicator;
import com.quant.indicators.simple.ClosePriceIndicator;
import com.quant.indicators.simple.MaxPriceIndicator;
import com.quant.indicators.simple.MinPriceIndicator;
import com.quant.indicators.simple.MultiplierIndicator;
import com.quant.trading.rules.UnderIndicatorRule;

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
