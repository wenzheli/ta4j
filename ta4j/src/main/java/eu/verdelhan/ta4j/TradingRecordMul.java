package eu.verdelhan.ta4j;

import java.util.ArrayList;
import java.util.List;

public class TradingRecordMul {
	private List<TradeMul> trades;
	private TradeMul currentTrade;
	
	public TradingRecordMul(){
		trades = new ArrayList<TradeMul>();
		currentTrade = new TradeMul();
	}
	
	public List<TradeMul> getTrades(){
		return trades;
	}
	
	public void clearCurrTrade(){
		currentTrade.clear();
	}
	
	public void addTrade(TradeMul trade){
		trades.add(trade);
	}
	
	public boolean isOpen(){
		return currentTrade.isOpened();
	}
	
	public boolean isClosed(){
		return currentTrade.isClosed();
	}
	
	public TradeMul getCurrentTrade(){
		return currentTrade;
	}
}
