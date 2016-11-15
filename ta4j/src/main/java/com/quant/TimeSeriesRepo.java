package com.quant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quant.Order.OrderType;
import com.quant.analysis.CashFlow;
import com.quant.factory.TimeSeriesRepoBuilder;
import com.quant.select.Portfolio;
import com.quant.select.Selector;
import com.quant.trading.rules.buying.HotRankBuy;
import com.quant.trading.rules.buying.VOLMultipleUp;
import com.quant.trading.rules.selling.HotRankSell;
import com.quant.trading.rules.selling.SMACrossedDown;
import com.quant.trading.rules.selling.SMAMultipleDown;
import com.quant.trading.rules.selling.VOLMultipleDown;
import com.quant.utils.SecurityCodeConverter;

/**
 * The stock repository.
 * @author wenzheli
 *
 */
public class TimeSeriesRepo {
	
	/** The logger */
	private final Logger log = LoggerFactory.getLogger(getClass());
	/** List of TimeSeries */
	private Map<String, TimeSeries> timeSeriesCollection;
	/** The size of the collection */
	private int size; 
	
	/**
	 * Constructor
	 * @param collection  the collection of time series
	 */
	public TimeSeriesRepo(Map<String, TimeSeries> collection){
		this.timeSeriesCollection = collection;
		this.size = collection.size();
	}
	
	public TimeSeriesRepo(List<TimeSeries> collection) throws IOException{
		timeSeriesCollection = new HashMap<String, TimeSeries>();
		for (TimeSeries series: collection){
			String code = series.getName();
			timeSeriesCollection.put(code, series);
		}
		this.size = collection.size();
		
		// 设置热点值, 目前从文件中读取
		setHotRank();
	}
	
	
	/**
	 * Set the daily hot rank for each stock. 
	 * @throws IOException  
	 */
	private void setHotRank() throws IOException{
		// to convert to 6-digit format. 
		SecurityCodeConverter converter = new SecurityCodeConverter();
		
		String dir = "/Users/wenzheli/Documents/workspace/benew/ta4j/ta4j/resource/redian0909-1014-3";
		File fileDir = new File(dir);
		BufferedReader br = null;
		String line = "";
		for (File f: fileDir.listFiles()){
			if (f.getName().equals(".DS_Store"))
				continue;
			// extract the date and convert it to @DateTime object
			String fileName = f.getName();
			String dateStr = fileName.split("_")[0];
			DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
			DateTime date = formatter.parseDateTime(dateStr);
			
			br = new BufferedReader(new FileReader(f));
			
			int rank  = 1;
			while ((line = br.readLine()) != null) { // read each line
				if (line.equals("")) 
					continue;
				String[] strs = line.split("\t");
				String code = converter.getCodeFromSecode(strs[1]);
				if (code != null && timeSeriesCollection.get(code) != null){
					TimeSeries s = timeSeriesCollection.get(code);
					if (s.getIndexFromDate(date) != -1){ // if the date is exists
						int idx = s.getIndexFromDate(date);
						s.getTick(idx).setHotRank(Decimal.valueOf(rank));
					}
				}
				rank++;
			}
		}
		
	}
	
	
	public TradingRecordMul runHotRankSingle(List<TradingRule> buyingRules, TradingRule sellingRule, 
			DateTime startTime, DateTime endTime) throws Exception{
		TradingRecordMul tradingRecord = new TradingRecordMul();  // to store the list of trades
		// get all the dates that the market opened 
		List<DateTime> dates = Selector.getFullDates(timeSeriesCollection, startTime, endTime); 
		for (DateTime date : dates){
			TradeCol currTrade = tradingRecord.getCurrentTrade();
			if (currTrade.isNew()){  // the next step is to buy entry == null & exit == null
				List<String> codes = Selector.select(timeSeriesCollection, buyingRules, date);
				if (codes.size() > 0){ // buy the first one by default
					//int randomIdx = 0 + (int)(Math.random() * (codes.size()-1));
					int randomIdx = 0;
					// randomly select one of them
					
					int idx = timeSeriesCollection.get(codes.get(randomIdx)).getIndexFromDate(date);
					currTrade.setEntryOrder(new Order(idx, OrderType.BUY));
					currTrade.setCode(codes.get(randomIdx));
				}
			} else{ // the next step is to sell
				// check if the currently bought stock satisfy the selling condition
				TradeCol trade = tradingRecord.getCurrentTrade();
				String code = trade.getCode();
				TimeSeries series = timeSeriesCollection.get(code);

				if (series.getIndexFromDate(date) == -1)  // if not exists
					continue;
				int idx = series.getIndexFromDate(date);
				
				if (Selector.satisfied(series, sellingRule, idx)){
					// then sold it and clear the current trade
					Order entryOrder = trade.getEntry();  // retrieve the current entry order
					TradeCol newTrade = new TradeCol(OrderType.BUY, code); // create a new trade object
					// set the entry and exit order
					newTrade.setEntryOrder(entryOrder);   
					newTrade.setExitOrder(new Order(idx, OrderType.SELL));
					tradingRecord.addTrade(newTrade); // add the trade list
					tradingRecord.clearCurrTrade();   // clear the current trade
				}
			}
				
		}
		return tradingRecord;
	}
	
	
	
	
	public TradingRecordMul runHotRankSingle(TradingRule buyingRule, TradingRule sellingRule, 
												DateTime startTime, DateTime endTime) throws Exception{
		TradingRecordMul tradingRecord = new TradingRecordMul();  // to store the list of trades
		// get all the dates that the market opened 
		List<DateTime> dates = Selector.getFullDates(timeSeriesCollection, startTime, endTime); 
		
		for (DateTime date : dates){
			TradeCol currTrade = tradingRecord.getCurrentTrade();
			if (currTrade.isNew()){  // the next step is to buy entry == null & exit == null
				List<String> codes = Selector.select(timeSeriesCollection, buyingRule, date);
				if (codes.size() > 0){ // buy the first one by default
					int randomIdx = 0 + (int)(Math.random() * (codes.size()-1));
					// randomly select one of them
					
					int idx = timeSeriesCollection.get(codes.get(randomIdx)).getIndexFromDate(date);
					currTrade.setEntryOrder(new Order(idx, OrderType.BUY));
					currTrade.setCode(codes.get(randomIdx));
				}
			} else{ // the next step is to sell
				// check if the currently bought stock satisfy the selling condition
				TradeCol trade = tradingRecord.getCurrentTrade();
				String code = trade.getCode();
				TimeSeries series = timeSeriesCollection.get(code);

				if (series.getIndexFromDate(date) == -1)  // if not exists
					continue;
				int idx = series.getIndexFromDate(date);
				
				if (Selector.satisfied(series, sellingRule, idx)){
					// then sold it and clear the current trade
					Order entryOrder = trade.getEntry();  // retrieve the current entry order
					TradeCol newTrade = new TradeCol(OrderType.BUY, code); // create a new trade object
					// set the entry and exit order
					newTrade.setEntryOrder(entryOrder);   
					newTrade.setExitOrder(new Order(idx, OrderType.SELL));
					tradingRecord.addTrade(newTrade); // add the trade list
					tradingRecord.clearCurrTrade();   // clear the current trade
				}
			}
				
		}
		return tradingRecord;
	}
	
	
	
	
	
	
	
	public List<TradingRecordMul> runHotRankMultiple(TradingRule buyingRule, TradingRule sellingRule, int period){
		return null;
	}
	
	
	
	public TradingRecordMul runHotRankWithOtherisSingle(List<TradingRule> buyingRules,
										TradingRule sellingRules){
		
		return null;
	}
	
	public List<TradingRecordMul> runHotRankWithOthersMultiple(List<TradingRule> buyingRules, 
										List<TradingRule> sellingRules){
		return null;
	}
	
	
	public List<Order> run(List<TradingRule> buyingRules, TradingRule sellingRule, 
									DateTime startDate, DateTime endDate) throws Exception{
		// get the all the trading dates between start date and end date
		List<DateTime> dates = Selector.getFullDates(timeSeriesCollection, startDate, endDate); 
		List<Order> orders = new ArrayList<Order>();
		Portfolio portfolio = new Portfolio();

		for (DateTime date : dates){  // for each trading date
			// check if the selling condition is satisfied
			for (String code : portfolio.getCodes()){
				TimeSeries series = timeSeriesCollection.get(code);
				if (series.getIndexFromDate(date) == -1)  // if not exists
					continue;
				int idx = series.getIndexFromDate(date);  // get the corresponding index
				if (Selector.satisfied(series, sellingRule, idx)){
					// then sell it & remove it from the portfolio
					Decimal currPrice = series.getTick(idx).getClosePrice();
					if (portfolio.removeAsset(code, currPrice)){
						orders.add(new Order(code, idx, OrderType.SELL, date, currPrice));
					}	
				}
			}

			// check if the buying condition is satisfied
			// get all the stocks that satisfy the buying rule
			List<String> codes = Selector.select(timeSeriesCollection, buyingRules, date);
			if (codes.size() <= 0)  
				continue;
			
			int i = 0;
			while (i < codes.size() && portfolio.size() <= Portfolio.MAX_NUM_OF_STOCKS){
				String name = codes.get(i);
				i++;
				TimeSeries series = timeSeriesCollection.get(name);
				if (series.getIndexFromDate(date) == -1)  // if not exists
					continue;
				int idx = series.getIndexFromDate(date);  // get the corresponding index
				Decimal currPrice = series.getTick(idx).getClosePrice();
				// if already exists, then skip the current stock
				if (portfolio.addAsset(name, currPrice)){
					orders.add(new Order(name, idx, OrderType.BUY, date, currPrice));
				}
			}				
		}
		
		return orders;
		
	}
	
	public List<Order> run(TradingRule buyingRule, TradingRule sellingRule, 
									DateTime startDate, DateTime endDate) throws Exception{
		// get the all the trading dates between start date and end date
		List<DateTime> dates = Selector.getFullDates(timeSeriesCollection, startDate, endDate); 
		List<Order> orders = new ArrayList<Order>();
		Portfolio portfolio = new Portfolio();

		for (DateTime date : dates){  // for each trading date
			// check if the selling condition is satisfied
			for (String code : portfolio.getCodes()){
				TimeSeries series = timeSeriesCollection.get(code);
				if (series.getIndexFromDate(date) == -1)  // if not exists
					continue;
				int idx = series.getIndexFromDate(date);  // get the corresponding index
				if (Selector.satisfied(series, sellingRule, idx)){
					// then sell it & remove it from the portfolio
					Decimal currPrice = series.getTick(idx).getClosePrice();
					if (portfolio.removeAsset(code, currPrice)){
						orders.add(new Order(code, idx, OrderType.SELL, date, currPrice));
					}	
				}
			}

			// check if the buying condition is satisfied
			// get all the stocks that satisfy the buying rule
			List<String> codes = Selector.select(timeSeriesCollection, buyingRule, date);
			if (codes.size() <= 0)  
				continue;
			
			int i = 0;
			while (i < codes.size() && portfolio.size() <= Portfolio.MAX_NUM_OF_STOCKS){
				String name = codes.get(i);
				i++;
				TimeSeries series = timeSeriesCollection.get(name);
				if (series.getIndexFromDate(date) == -1)  // if not exists
					continue;
				int idx = series.getIndexFromDate(date);  // get the corresponding index
				Decimal currPrice = series.getTick(idx).getClosePrice();
				// if already exists, then skip the current stock
				if (portfolio.addAsset(name, currPrice)){
					orders.add(new Order(name, idx, OrderType.BUY, date, currPrice));
				}
			}				
		}
		
		return orders;
	}

	
	/**
	 * Run the strategy on the repository 
	 * @param strategy
	 * @param bucket
	 * @return
	 * @throws Exception 
	 */
	public List<TradingRecordMul> run(TradingRule buyingRule, TradingRule sellingRule, int bucket, int period) throws Exception{
		// initialization
		List<TradingRecordMul> records = new ArrayList<TradingRecordMul>(bucket);
		for (int i = 0; i < bucket; i++){
			records.add(new TradingRecordMul());
		}
		
		for (int i = period; i >= 0; i--){
			// check if anything can be sold. 
			for (int j = 0; j < records.size(); j++){
				// for each trading record object
				TradeCol currTrade = records.get(j).getCurrentTrade();
				if (currTrade.isNew())  // the next step is to buy 
					continue;
				TimeSeries series = timeSeriesCollection.get(currTrade.getCode());
				if (series.getEnd()-i- currTrade.getEntry().getIndex() >= 3) {
					// only keeping 3 days
				
					Order exitOrder = new Order(series.getEnd()-i, OrderType.SELL);
					TradeCol newTrade = new TradeCol(OrderType.BUY, currTrade.getEntry(), exitOrder, 
									currTrade.getCode());
					records.get(j).addTrade(newTrade);
					records.get(j).clearCurrTrade();
				} else
					continue;
				
				/*
				// check if it satisfy the selling condition 
				TimeSeries series = timeSeriesCollection.get(currTrade.getCode());
				boolean satisfy = Selector.isSatisfied(series, sellingRule, i);
				
				if (satisfy == true){
					Order exitOrder = new Order(series.getEnd()-i, OrderType.SELL);
					TradeMul newTrade = new TradeMul(OrderType.BUY, currTrade.getEntry(), exitOrder, 
									currTrade.getCode());
					records.get(j).addTrade(newTrade);
					records.get(j).clearCurrTrade();
				}else{	 
					continue;
				}*/
			}
			
			
			// check if we can buy in a new stock
			if (!canBuy(records))	continue;
			
			List<String> codes = Selector.select(this.timeSeriesCollection, buyingRule, i);
			if (codes.isEmpty()) continue;
			
			int p = 0, k=0;
			while (p < records.size() && k < codes.size()){
				if (!records.get(p).getCurrentTrade().isNew()){
					p++; continue;
				}
				
				// otherwise, buy the kth code
				TimeSeries series = timeSeriesCollection.get(codes.get(k));
				records.get(p).getCurrentTrade().setCode(codes.get(k));
				Order entryOrder = new Order(series.getEnd() - i, OrderType.BUY);
				records.get(p).getCurrentTrade().setEntryOrder(entryOrder);
				k++; 
				p++;
			}
			
		}
		
		
		return records;
	}
	
	private boolean canBuy(List<TradingRecordMul> records){
		for (TradingRecordMul record: records){
			if (record.getCurrentTrade().isNew()){
				return true;
			}
		}
		
		return false;
	}
	
	
	public Map<String, TimeSeries> getTimeSeries(){
		return timeSeriesCollection;
	}
	
	public int size(){
		return size;
	}

	
	public TimeSeries get(String code){
		return timeSeriesCollection.get(code);
	}
	
	public static void main(String[] args) throws Exception{
		String path = "/Users/wenzheli/Documents/workspace/quant-data/data/stocks";
		TimeSeriesRepoBuilder builder = new TimeSeriesRepoBuilder(path);
		TimeSeriesRepo repo = builder.build();
		
		TradingRule buying = new HotRankBuy(2);
		TradingRule selling = new HotRankSell(5);
		
		String start = "2016-09-09";
		String end = "2016-10-14";

		DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
		
		TradingRecordMul tradingRecordMul = repo.runHotRankSingle(buying, selling, f.parseDateTime(start), f.parseDateTime(end));
		
		CashFlow cashFlow = new CashFlow(repo, tradingRecordMul, f.parseDateTime(start), f.parseDateTime(end));
		
		
		//TradingRule buying = new VOLMultipleUp(5,10,30,1);
		//TradingRule selling = new SMACrossedDown(5,10,30);
		
		List<TradingRecordMul> records = repo.run(buying, selling, 2, 60);
	}
}
