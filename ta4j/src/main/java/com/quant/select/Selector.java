package com.quant.select;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.quant.Rule;
import com.quant.TimeSeries;
import com.quant.TimeSeriesRepo;
import com.quant.TradingRule;
import com.quant.factory.TimeSeriesRepoBuilder;
import com.quant.trading.rules.buying.SMAMultipleUp;
import com.quant.trading.rules.buying.VOLMultipleUp;

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
	 * Given the list of time series and the trading rule,  returns the list of 
	 * stock codes that satisfy the trading rule. 
	 * 
	 * @param timeSeriesCollection   the list of time series
	 * @param rule    				 trading rule
	 * @param date					 the date
	 * @return             
	 * @throws Exception
	 */
	public static List<String> select(Map<String, TimeSeries> timeSeriesCollection, TradingRule rule, DateTime date) throws Exception{
		List<String> codes = new ArrayList<String>();
		for (String code : timeSeriesCollection.keySet()){
			TimeSeries series = timeSeriesCollection.get(code);
			// convert to the index, if not exists, then skip it. 
			if (series.getDateToIndex().get(date) == null)
				continue;
			// otherwise check if the index of current time series satisfy the trading rule. 
			int index = series.getDateToIndex().get(date); 
			if (Selector.satisfied(series, rule, index)){
				codes.add(code);
			}
		}
		
		return codes;  // list of stock codes
	}
	
	public static List<String> select(Map<String, TimeSeries> timeSeriesCollection, List<TradingRule> rules, DateTime date) throws Exception{
		List<String> codes = new ArrayList<String>();
		for (String code : timeSeriesCollection.keySet()){
			TimeSeries series = timeSeriesCollection.get(code);
			// convert to the index, if not exists, then skip it. 
			if (series.getDateToIndex().get(date) == null)
				continue;
			// otherwise check if the index of current time series satisfy the trading rule. 
			int index = series.getDateToIndex().get(date);
			if (Selector.satisfied(series, rules.get(0), index)){ // first satisfy the hot rank rule
				for (int i = 1; i < rules.size(); i++){
					if (Selector.satisfied(series, rules.get(i), index)){
						codes.add(code);
						break;
					}
				}
			}
		}
		
		return codes;  // list of stock codes
	}
	
	
	public static List<String> selectAllSatisfy(Map<String, TimeSeries> timeSeriesCollection, List<TradingRule> rules, DateTime date) throws Exception{
		List<String> codes = new ArrayList<String>();
		for (String code : timeSeriesCollection.keySet()){
			TimeSeries series = timeSeriesCollection.get(code);
			// convert to the index, if not exists, then skip it. 
			if (series.getDateToIndex().get(date) == null)
				continue;
			// otherwise check if the index of current time series satisfy the trading rule. 
			int index = series.getDateToIndex().get(date);
			if (Selector.satisfied(series, rules.get(0), index)){ // first satisfy the hot rank rule
				boolean flag = true;
				for (int i = 1; i < rules.size(); i++){
					
					if (!Selector.satisfied(series, rules.get(i), index)){
						flag = false;
						
						break;
					}
				}
				
				if (flag == true){
					codes.add(code);
				}
			}
		}
		
		return codes;  // list of stock codes
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
	
	
	public static boolean satisfied(TimeSeries series, TradingRule tradingRule, int index) throws Exception{
		tradingRule.setTimeSeries(series);
		Rule rule = tradingRule.buildRule();
		return rule.isSatisfied(index);
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
	
	
	public static List<DateTime> getFullDates(Map<String, TimeSeries> repo, int period){
		String code = "000300";
		TimeSeries series = repo.get(code);
		
		List<DateTime> dates = new ArrayList<DateTime>();
		int size = series.getEnd();
		for (int i = 0; i < period; i++){
			dates.add(series.getTick(size - period + i).getEndTime());
		}
		
		return dates;
	}
	
	
	public static List<DateTime> getFullDates(Map<String, TimeSeries> repo, 
							DateTime startTime, DateTime endTime){
		String code = "hs300";
		TimeSeries series = repo.get(code);
		
		List<DateTime> dates = new ArrayList<DateTime>();
		int startIdx = series.getIndexFromDate(startTime);
		int endIdx = series.getIndexFromDate(endTime);
		
		for (int i = startIdx; i <= endIdx; i++){
			dates.add(series.getTick(i).getEndTime());
		}
		
		return dates;
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
