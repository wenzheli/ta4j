package com.quant;

import java.util.ArrayList;
import java.util.List;

public class TradingRecordMul {
	private List<TradeCol> trades;
	private TradeCol currentTrade;
	
	public TradingRecordMul(){
		trades = new ArrayList<TradeCol>();
		currentTrade = new TradeCol();
	}
	
	public List<TradeCol> getTrades(){
		return trades;
	}
	
	public void clearCurrTrade(){
		currentTrade.clear();
	}
	
	public void addTrade(TradeCol trade){
		trades.add(trade);
	}
	
	public boolean isOpen(){
		return currentTrade.isOpened();
	}
	
	public boolean isClosed(){
		return currentTrade.isClosed();
	}
	
	public TradeCol getCurrentTrade(){
		return currentTrade;
	}
}
