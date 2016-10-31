package eu.verdelhan.ta4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.verdelhan.ta4j.Order.OrderType;
import eu.verdelhan.ta4j.factory.TimeSeriesRepoBuilder;
import eu.verdelhan.ta4j.select.Selector;
import eu.verdelhan.ta4j.trading.rules.buying.VOLMultipleUp;
import eu.verdelhan.ta4j.trading.rules.selling.SMACrossedDown;
import eu.verdelhan.ta4j.trading.rules.selling.SMAMultipleDown;
import eu.verdelhan.ta4j.trading.rules.selling.VOLMultipleDown;

public class TimeSeriesRepo {
	
	/** The logger */
	private final Logger log = LoggerFactory.getLogger(getClass());
	/** List of TimeSeries */
	private Map<String, TimeSeries> timeSeriesCollection;
	/** The size of the collection */
	private int size; 
	
	public TimeSeriesRepo(Map<String, TimeSeries> collection){
		this.timeSeriesCollection = collection;
		this.size = collection.size();
	}
	
	public TimeSeriesRepo(List<TimeSeries> collection){
		timeSeriesCollection = new HashMap<String, TimeSeries>();
		for (TimeSeries series: collection){
			String code = series.getName();
			timeSeriesCollection.put(code, series);
		}
		this.size = collection.size();
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
				TradeMul currTrade = records.get(j).getCurrentTrade();
				if (currTrade.isNew())  // the next step is to buy 
					continue;
				TimeSeries series = timeSeriesCollection.get(currTrade.getCode());
				if (series.getEnd()-i- currTrade.getEntry().getIndex() >= 3) {
					// only keeping 3 days
				
					Order exitOrder = new Order(series.getEnd()-i, OrderType.SELL);
					TradeMul newTrade = new TradeMul(OrderType.BUY, currTrade.getEntry(), exitOrder, 
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
		String path = "/Users/liwenzhe/Documents/workspace/DataWrapper/data/stocks";
		TimeSeriesRepoBuilder builder = new TimeSeriesRepoBuilder(path);
		TimeSeriesRepo repo = builder.build();
		
		TradingRule buying = new VOLMultipleUp(5,10,30,1);
		TradingRule selling = new SMACrossedDown(5,10,30);
		
		List<TradingRecordMul> records = repo.run(buying, selling, 2, 60);
	}
}
