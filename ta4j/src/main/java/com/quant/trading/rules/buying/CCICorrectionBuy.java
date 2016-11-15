package com.quant.trading.rules.buying;

import com.quant.Decimal;
import com.quant.Rule;
import com.quant.TradingRule;
import com.quant.indicators.oscillators.CCIIndicator;
import com.quant.trading.rules.OverIndicatorRule;
import com.quant.trading.rules.UnderIndicatorRule;

public class CCICorrectionBuy extends TradingRule{
	
	public CCICorrectionBuy(){
	}
	
	@Override
	public Rule buildRule(){
		CCIIndicator longCci = new CCIIndicator(series, 200);
        CCIIndicator shortCci = new CCIIndicator(series, 5);
        Decimal plus100 = Decimal.HUNDRED;
        Decimal minus100 = Decimal.valueOf(-100);
        
        Rule entryRule = new OverIndicatorRule(longCci, plus100) // Bull trend
                .and(new UnderIndicatorRule(shortCci, minus100)); // Signal
        
        return entryRule;
	}
}
