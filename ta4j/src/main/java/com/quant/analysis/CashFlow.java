/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Marc de Verdelhan & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.quant.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.quant.Decimal;
import com.quant.Indicator;
import com.quant.Order;
import com.quant.Order.OrderType;
import com.quant.TimeSeries;
import com.quant.TimeSeriesRepo;
import com.quant.Trade;
import com.quant.TradeCol;
import com.quant.TradingRecord;
import com.quant.TradingRecordMul;
import com.quant.select.Portfolio;
import com.quant.select.Selector;

/**
 * The cash flow.
 * <p>
 * This class allows to follow the money cash flow involved by a list of trades over a time series.
 */
public class CashFlow implements Indicator<Decimal> {

    /** The time series */
    private TimeSeries timeSeries;
    
    /** The time series repo */ 
    private TimeSeriesRepo repo;
    
    /** The length of period to consider */
    private int period;
    
    /** The start time that we track from  */
    private DateTime startTime;
    
    /** The end time */
    private DateTime endTime;

    /** The cash flow values, size() = 1 */
    private List<Decimal> values = new ArrayList<Decimal>(Arrays.asList(Decimal.ONE)); 

    /**
     * Constructor.
     * @param timeSeries the time series
     * @param trade a single trade
     */
    public CashFlow(TimeSeries timeSeries, Trade trade) {
        this.timeSeries = timeSeries;
        calculate(trade);
        fillToTheEnd();
    }
    
    
    private Map<DateTime, List<Order>> getDateToOrders(List<Order> orders){
    	Map<DateTime, List<Order>> dateToOrders = new HashMap<DateTime, List<Order>>();
    	for (Order o : orders){
    		DateTime date = o.getDateTime();
    		if (dateToOrders.containsKey(date)){
    			List<Order> tmp = dateToOrders.get(date);
    			tmp.add(o);
    			dateToOrders.put(date, tmp);
    		} else {
    			List<Order> tmp = new ArrayList<Order>();
    			tmp.add(o);
    			dateToOrders.put(date, tmp);
    		}
    	}
    	
    	return dateToOrders;
    }
    
    
    public CashFlow(TimeSeriesRepo repo, List<Order> orders, DateTime startDate, DateTime endDate){
    	Map<DateTime, List<Order>> dateToOrders = getDateToOrders(orders);
    	List<DateTime> dates = Selector.getFullDates(repo.getTimeSeries(), startDate, endDate);
    	Portfolio tmp = new Portfolio();
    	
    	for (DateTime date : dates){
    		List<Order> os = dateToOrders.get(date);
    		if (os != null){
    			for (Order o : os){
        			if (OrderType.BUY.equals(o.getType())){
        				tmp.addAsset(o.getName(), o.getPrice());
        			} else {
        				tmp.removeAsset(o.getName(), o.getPrice());
        			}
        		}
    		}
    		
    		Decimal val = tmp.totalVal(date, repo.getTimeSeries());
    		values.add(val);
    	}
    }
    
    
    
    
    public CashFlow(TimeSeriesRepo repo, TradingRecordMul tradingRecord, 
    									DateTime startTime, DateTime endTime){
    	// get the hs300 index as the reference
    	String code = "hs300"; 
		TimeSeries hs300 = repo.get(code);
		
		// start and end index for the hs300 
		int startIdx = hs300.getIndexFromDate(startTime);
		int endIdx = hs300.getIndexFromDate(endTime);
		
		//this.timeSeries = hs300.subseries(startIdx, endIdx);
		
		// we can compute the total length, which is equivalent to the size of array 
		int len = endIdx - startIdx + 1;
		
		
		for(TradeCol trade : tradingRecord.getTrades()){
			code = trade.getCode();
			int buyIdx = trade.getEntry().getIndex();
			int sellIdx = trade.getExit().getIndex();
			TimeSeries currSeries = repo.get(code);
			DateTime buyDate = currSeries.getTick(buyIdx).getEndTime();
			DateTime sellDate = currSeries.getTick(sellIdx).getEndTime();
			
			int buy = hs300.getIndexFromDate(buyDate) - startIdx;
			int sell = hs300.getIndexFromDate(sellDate) - startIdx;
			
		
	    	if (buy +1 > values.size()) {
	            Decimal lastValue = values.get(values.size() - 1);
	            values.addAll(Collections.nCopies(buy+1 - values.size(), lastValue));
	        }
	    	
	    	
	    	for (int i = Math.max(buy+1, 1); i <= sell; i++) {
	            Decimal ratio;
	            if (trade.getEntry().isBuy()) {
	            	DateTime tmp = hs300.getTick(i + startIdx).getEndTime();
	            	int tmpIdx = currSeries.getIndexFromDate(tmp);
	                ratio = currSeries.getTick(tmpIdx).getClosePrice().dividedBy(currSeries.getTick(trade.getEntry().getIndex()).getClosePrice());
	            } else {
	            	int tmpIdx = currSeries.getIndexFromDate(hs300.getTick(i + startIdx).getEndTime());
	                ratio = currSeries.getTick(trade.getEntry().getIndex()).getClosePrice().dividedBy(currSeries.getTick(tmpIdx).getClosePrice());
	            }
	            values.add(values.get(buy).multipliedBy(ratio));
	        }
	    	
		}	
		
		fillToTheEndMul(len);
    }
    
    public void fillToTheEndMul(int len){
    	if (len > values.size()){
    		Decimal lastValue = values.get(values.size()-1);
    		values.addAll(Collections.nCopies(len - values.size(), lastValue));
    	}
    }
    

    
    
    public CashFlow(TimeSeriesRepo repo, TradingRecordMul tradingRecord, int period){
    	this.repo = repo;
    	this.period = period;
    	calculateMul(tradingRecord);
    	fillToTheEndMul();
    }
    
    public CashFlow(TimeSeriesRepo repo, List<TradingRecordMul> tradingRecordMul){
    	this.repo = repo;
    	calculateMul(tradingRecordMul);
    	fillToTheEndMul();
    }
    
    
    

    /**
     * Constructor.
     * @param timeSeries the time series
     * @param tradingRecord the trading record
     */
    public CashFlow(TimeSeries timeSeries, TradingRecord tradingRecord) {
        this.timeSeries = timeSeries;
        calculate(tradingRecord);
        fillToTheEnd();
    }

    /**
     * @param index the tick index
     * @return the cash flow value at the index-th position
     */
    @Override
    public Decimal getValue(int index) {
        return values.get(index);
    }

    @Override
    public TimeSeries getTimeSeries() {
        return timeSeries;
    }

    /**
     * @return the size of the time series
     */
    public int getSize() {
        return timeSeries.getTickCount();
    }
    
    public List<Decimal> getValues(){
    	return values;
    }

    /**
     * Calculates the cash flow for a single trade.
     * @param trade a single trade
     */
    private void calculate(Trade trade) {
        final int entryIndex = trade.getEntry().getIndex();
        int begin = entryIndex + 1;
        if (begin > values.size()) {
            Decimal lastValue = values.get(values.size() - 1);
            values.addAll(Collections.nCopies(begin - values.size(), lastValue));
        }
        int end = trade.getExit().getIndex();
        for (int i = Math.max(begin, 1); i <= end; i++) {
            Decimal ratio;
            if (trade.getEntry().isBuy()) {
                ratio = timeSeries.getTick(i).getClosePrice().dividedBy(timeSeries.getTick(entryIndex).getClosePrice());
            } else {
                ratio = timeSeries.getTick(entryIndex).getClosePrice().dividedBy(timeSeries.getTick(i).getClosePrice());
            }
            values.add(values.get(entryIndex).multipliedBy(ratio));
        }
    }
    
    private void calculateMul(TradingRecordMul tradingRecordMul){
    	for (TradeCol trade : tradingRecordMul.getTrades()){
    		// the trading record is ordered by time. 
    		calculateMul(trade, repo.getTimeSeries().get(trade.getCode()));
    	}
    }
    
    /**
     * Calculate the cash flow for the single trade. 
     * @param trade  	the single trade, but this trade contains stock code
     * @param series   	the input time series
     */
    private void calculateMul(TradeCol trade, TimeSeries series){
    	final int entryIndex = trade.getEntry().getIndex();
        int begin = entryIndex + 1;
        if (begin > values.size()) {
            Decimal lastValue = values.get(values.size() - 1);
            values.addAll(Collections.nCopies(begin - values.size(), lastValue));
        }
        int end = trade.getExit().getIndex();
        for (int i = Math.max(begin, 1); i <= end; i++) {
            Decimal ratio;
            if (trade.getEntry().isBuy()) {
                ratio = series.getTick(i).getClosePrice().dividedBy(series.getTick(entryIndex).getClosePrice());
            } else {
                ratio = series.getTick(entryIndex).getClosePrice().dividedBy(series.getTick(i).getClosePrice());
            }
            values.add(values.get(entryIndex).multipliedBy(ratio));
        }
    }
    
    
    private void calculateMul(List<TradingRecordMul> tradingRecrodsMul){
    	
    }

    /**
     * Calculates the cash flow for a trading record.
     * @param tradingRecord the trading record
     */
    private void calculate(TradingRecord tradingRecord) {
        for (Trade trade : tradingRecord.getTrades()) {
            // For each trade...
            calculate(trade);
        }
    }

    /**
     * Fills with last value till the end of the series.
     */
    private void fillToTheEnd() {
        if (timeSeries.getEnd() >= values.size()) {
            Decimal lastValue = values.get(values.size() - 1);
            values.addAll(Collections.nCopies(timeSeries.getEnd() - values.size() + 1, lastValue));
        }
    }
    
    private void fillToTheEndMul(){
    	if (period-1 >= values.size()) {
            Decimal lastValue = values.get(values.size() - 1);
            values.addAll(Collections.nCopies(period - values.size(), lastValue));
        }
    }
    
    
    public static void main(String[] args){
    	List<Decimal> a = new ArrayList<Decimal>(Arrays.asList(Decimal.ONE));
    	int aaa = 1;
    }
}