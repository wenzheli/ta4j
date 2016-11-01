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
package eu.verdelhan.ta4j.analysis;

import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TimeSeriesRepo;
import eu.verdelhan.ta4j.Trade;
import eu.verdelhan.ta4j.TradeMul;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.TradingRecordMul;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

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

    /** The cash flow values */
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
    
    
    public CashFlow(TimeSeriesRepo repo, TradingRecordMul tradingRecord, 
    									DateTime startTime, DateTime endTime){
    	this.repo = repo;
    	this.startTime = startTime;
    	this.endTime = endTime;
    	
    	String code = "hs300";
		TimeSeries hs300 = repo.get(code);
		
		int startIdx = hs300.getIndexFromDate(startTime);
		int endIdx = hs300.getIndexFromDate(endTime);
		
		int len = endIdx - startIdx + 1;
		
		for(TradeMul trade : tradingRecord.getTrades()){
			code = trade.getCode();
	    	TimeSeries s = repo.getTimeSeries().get(code);
	    	DateTime buyDate = s.getTick(trade.getEntry().getIndex()).getEndTime();
	    	int buyIdx = hs300.getIndexFromDate(buyDate) - startIdx;
	    	
	    	if (buyIdx > values.size()) {
	            Decimal lastValue = values.get(values.size() - 1);
	            values.addAll(Collections.nCopies(buyIdx - values.size(), lastValue));
	        }
	    	
	    	DateTime sellDate = s.getTick(trade.getExit().getIndex()).getEndTime();
	    	int sellIdx = hs300.getIndexFromDate(sellDate) - startIdx;
	    	
	    	for (int i = Math.max(buyIdx, 1); i <= sellIdx; i++) {
	            Decimal ratio;
	            if (trade.getEntry().isBuy()) {
	            	int tmpIdx = s.getIndexFromDate(hs300.getTick(i + startIdx).getEndTime());
	                ratio = s.getTick(tmpIdx).getClosePrice().dividedBy(s.getTick(trade.getEntry().getIndex()).getClosePrice());
	            } else {
	                ratio = s.getTick(trade.getEntry().getIndex()).getClosePrice().dividedBy(s.getTick(i).getClosePrice());
	            }
	            values.add(values.get(buyIdx).multipliedBy(ratio));
	        }
	    	
		}	
    }
    
    private void calculateMulBeta(TradeMul trade, TimeSeries hs300){
    	String code = trade.getCode();
    	TimeSeries s = repo.getTimeSeries().get(code);
    	DateTime buyDate = s.getTick(trade.getEntry().getIndex()).getEndTime();
    	int buyIdx = hs300.getIndexFromDate(buyDate);
    	
    	if (buyIdx > values.size()) {
            Decimal lastValue = values.get(values.size() - 1);
            values.addAll(Collections.nCopies(buyIdx - values.size(), lastValue));
        }
    	
    	DateTime sellDate = s.getTick(trade.getExit().getIndex()).getEndTime();
    	int sellIdx = hs300.getIndexFromDate(sellDate);
    	
    	for (int i = Math.max(buyIdx, 1); i <= sellIdx; i++) {
            Decimal ratio;
            if (trade.getEntry().isBuy()) {
            	
                ratio = timeSeries.getTick(i).getClosePrice().dividedBy(timeSeries.getTick(entryIndex).getClosePrice());
            } else {
                ratio = timeSeries.getTick(entryIndex).getClosePrice().dividedBy(timeSeries.getTick(i).getClosePrice());
            }
            values.add(values.get(entryIndex).multipliedBy(ratio));
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
    	for (TradeMul trade : tradingRecordMul.getTrades()){
    		// the trading record is ordered by time. 
    		calculateMul(trade, repo.getTimeSeries().get(trade.getCode()));
    	}
    }
    
    /**
     * Calculate the cash flow for the single trade. 
     * @param trade  	the single trade, but this trade contains stock code
     * @param series   	the input time series
     */
    private void calculateMul(TradeMul trade, TimeSeries series){
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
}