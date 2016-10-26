package eu.verdelhan.ta4j.trading.rules.selling;


import java.io.IOException;
import java.util.List;

import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TimeSeriesRepo;
import eu.verdelhan.ta4j.TradingRule;
import eu.verdelhan.ta4j.factory.TimeSeriesRepoBuilder;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.simple.PreviousIndicator;
import eu.verdelhan.ta4j.indicators.trackers.SMAIndicator;
import eu.verdelhan.ta4j.select.Selector;
import eu.verdelhan.ta4j.trading.rules.CrossedDownIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.CrossedUpIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.OverIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.buying.SMACrossedUp;

/**
 * 均线 － 死叉 卖出信号
 * @author liwenzhe
 *
 */
public class SMACrossedDown extends TradingRule{
	
	private int param1 = 5;
	private int param2 = 10;
	private int param3 = 30;
	
	public SMACrossedDown(int param1, int param2, int param3){
		this.setParams(param1, param2, param3);
	}
	
	public SMACrossedDown(int param1, int param2, int parm3, TimeSeries series){
		super(series);
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
		
		Rule rule = new CrossedDownIndicatorRule(smaIndicator1, smaIndicator2)
				.and(new OverIndicatorRule(new PreviousIndicator(smaIndicator1,1), smaIndicator1))
				.and(new OverIndicatorRule(new PreviousIndicator(smaIndicator2,1), smaIndicator2))
				.and(new OverIndicatorRule(new PreviousIndicator(smaIndicator3,1), smaIndicator3));
		
		return rule;
	}
	
	
	public static void main(String[] args) throws Exception{
		String path = "/Users/liwenzhe/Documents/workspace/DataWrapper/data/stocks";
		TimeSeriesRepoBuilder builder = new TimeSeriesRepoBuilder(path);
		TimeSeriesRepo repo = builder.build();
		
		TradingRule tradingRule = new SMACrossedUp(5,10,30);
	
		
		List<TimeSeries> candidates = Selector.select(repo, tradingRule);
		List<String> codes = Selector.getCodes(candidates);
		for(String c : codes)
			System.out.println(c);
	}
	
}
