package com.quant.trading.rules.buying;

import java.util.List;

import com.quant.Rule;
import com.quant.TimeSeries;
import com.quant.TimeSeriesRepo;
import com.quant.TradingRule;
import com.quant.factory.TimeSeriesRepoBuilder;
import com.quant.indicators.simple.ClosePriceIndicator;
import com.quant.indicators.simple.PreviousIndicator;
import com.quant.indicators.trackers.SMAIndicator;
import com.quant.select.Selector;
import com.quant.trading.rules.CrossedUpIndicatorRule;
import com.quant.trading.rules.UnderIndicatorRule;


/**
 * 均线 － 金叉 买入信号
 * @author liwenzhe
 *
 */
public class SMACrossedUp extends TradingRule{
	
	private int param1 = 5;
	private int param2 = 10;
	private int param3 = 30;
	
	public SMACrossedUp(int param1, int param2, int param3){
		this.setParams(param1, param2, param3);
	}
	
	public SMACrossedUp(int param1, int param2, int parm3, TimeSeries series){
		super (series);
		this.setParams(param1, param2, param2);
	}
	
	private void setParams(int param1, int param2, int param3){
		this.param1 = param1;
		this.param2 = param2;
		this.param3 = param3;
	}
	
	
	@Override
	public Rule buildRule() throws Exception {
		// TODO Auto-generated method stub
		if (param1 == 0 || param2 == 0 || param3 == 0 || series == null){
			throw new Exception("There are one or more params are not initialzied!");
		}
		
		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		SMAIndicator smaIndicator1 = new SMAIndicator(closePrice, param1);
		SMAIndicator smaIndicator2 = new SMAIndicator(closePrice, param2);
		SMAIndicator smaIndicator3 = new SMAIndicator(closePrice,param3);
		
		Rule rule = new CrossedUpIndicatorRule(smaIndicator1, smaIndicator2)
				.and(new UnderIndicatorRule(new PreviousIndicator(smaIndicator1,1), smaIndicator1))
				.and(new UnderIndicatorRule(new PreviousIndicator(smaIndicator2,1), smaIndicator2))
				.and(new UnderIndicatorRule(new PreviousIndicator(smaIndicator3,1), smaIndicator3));
		
		return rule;
	}
	
	public static void main(String[] args) throws Exception{
		String path = "/Users/liwenzhe/Documents/workspace/DataWrapper/data/stocks";
		TimeSeriesRepoBuilder builder = new TimeSeriesRepoBuilder(path);
		TimeSeriesRepo repo = builder.build();
		
		TradingRule tradingRule = new SMACrossedUp(5,10,30);
	
		
		List<TimeSeries> candidates = Selector.select(repo, tradingRule);
	}
	
}
