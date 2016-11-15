package com.quant.trading.rules.buying;

import com.quant.Decimal;
import com.quant.Rule;
import com.quant.TradingRule;
import com.quant.indicators.simple.ClosePriceIndicator;
import com.quant.indicators.trackers.RSIIndicator;
import com.quant.indicators.trackers.SMAIndicator;
import com.quant.trading.rules.CrossedDownIndicatorRule;
import com.quant.trading.rules.OverIndicatorRule;

public class RSI2Buy extends TradingRule{
	
	public RSI2Buy(){
		
	}
	
	public Rule buildRule(){
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
        
        return entryRule;
	}
}
