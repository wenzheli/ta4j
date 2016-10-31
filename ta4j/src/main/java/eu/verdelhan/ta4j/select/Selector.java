package eu.verdelhan.ta4j.select;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TimeSeriesRepo;
import eu.verdelhan.ta4j.TradingRule;
import eu.verdelhan.ta4j.factory.TimeSeriesRepoBuilder;
import eu.verdelhan.ta4j.trading.rules.buying.SMAMultipleUp;
import eu.verdelhan.ta4j.trading.rules.buying.VOLMultipleUp;

public class Selector {
	
	
	public static List<TimeSeries> select(Map<String, TimeSeries> repo, TradingRule tradingRule) throws Exception{
		List<TimeSeries> res = new ArrayList<TimeSeries>();   // store the result code 
		for (TimeSeries series : repo.values()){
			
			if (Selector.isSatisfied(series, tradingRule)){
				res.add(series); 
			}
		}
		
		return res;
	}
	
	
	/**
	 * Return the stock code which satisfied the rule
	 * @return  the list of stock codes
	 * @throws Exception 
	 */
	public static List<TimeSeries> select(TimeSeriesRepo repo, TradingRule tradingRule) throws Exception{
		List<TimeSeries> res = new ArrayList<TimeSeries>();   // store the result code 
		for (TimeSeries series : repo.getTimeSeries().values()){
			
			if (Selector.isSatisfied(series, tradingRule)){
				res.add(series); 
			}
		}
		
		return res;
	}
	
	/**
	 * Return 				  the list of time series that satisfy the given (list of) rules. 
	 * @param repo   		  the time series repository 
	 * @param tradingRules    list of trading rules
	 * @return                the list of time series meet the condition
	 * @throws Exception
	 */
	public static List<TimeSeries> select(TimeSeriesRepo repo, List<TradingRule> tradingRules) throws Exception{
		List<TimeSeries> res = new ArrayList<TimeSeries>();
		for (TimeSeries series: repo.getTimeSeries().values()){
			for (TradingRule rule : tradingRules){
				if (!Selector.isSatisfied(series, rule)){
					break; 
				}
				res.add(series);
			}
		}
		
		return res;
	}
	
	public static List<TimeSeries> select(Map<String, TimeSeries> repo, List<TradingRule> tradingRules, int beforeIndex) throws Exception{
		List<TimeSeries> res = new ArrayList<TimeSeries>();
		for (TimeSeries series: repo.values()){
			for (TradingRule rule : tradingRules){
				if (!Selector.isSatisfied(series, rule, beforeIndex)){
					break;
				}
				res.add(series);
			}
		}
		
		return res;
	}
	
	
	public static List<String> getCodes(List<TimeSeries> series){
		List<String> codes = new ArrayList<String>();
		for (TimeSeries s : series){
			codes.add(s.getName());
		}
		return codes;
	}
	
	
	public static boolean isSatisfied(TimeSeries series, TradingRule tradingRule) throws Exception{
		tradingRule.setTimeSeries(series);
		Rule rule = tradingRule.buildRule();
		return rule.isSatisfied(series.getEnd());
	}
	
	
	
	public static boolean isSatisfied(TimeSeries series, TradingRule tradingRule, int beforeIndex) throws Exception{
		tradingRule.setTimeSeries(series);
		Rule rule = tradingRule.buildRule();
		return rule.isSatisfied(series.getEnd() - beforeIndex);
	}
	
	public static List<String> select(Map<String, TimeSeries> repo, TradingRule tradingRule, int beforeIndex) throws Exception{
		List<String> codes = new ArrayList<String>();
		for (TimeSeries s: repo.values()){
			if (Selector.isSatisfied(s, tradingRule, beforeIndex)){
				codes.add(s.getName());
			}
		}
		
		return codes;
	}
	
	
	
	public static void main(String[] args) throws Exception{
		String path = "/Users/liwenzhe/Documents/workspace/DataWrapper/data/stocks";
		TimeSeriesRepoBuilder builder = new TimeSeriesRepoBuilder(path);
		TimeSeriesRepo repo = builder.build();
		
		
		TradingRule tradingRule = new VOLMultipleUp(5,10,30,3);
		
		
		List<TimeSeries> candidates = Selector.select(repo, tradingRule);
		List<String> codes = Selector.getCodes(candidates);
		
		System.out.println(codes);
		
		PortfolioCreator creator = new PortfolioCreator(candidates, codes, 30);
		creator.setRiskFactor(0.1);
		double[] weights = creator.create();
		
		for (int i = 0; i < weights.length; i++)
			System.out.println(weights[i]);
		
		System.out.println("next try...");
		creator.setRiskFactor(10);
		weights = creator.create();
		
		System.out.println("the weight vector is");
		for (int i = 0; i < weights.length; i++)
			System.out.println(weights[i]);
	}
}
