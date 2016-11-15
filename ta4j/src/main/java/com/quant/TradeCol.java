package com.quant;

import com.quant.Order.OrderType;

public class TradeCol extends Trade{
	
	private String code;
	
	public TradeCol(OrderType startingType, String code){
		super(startingType);
		this.code = code;
	}
	
	public TradeCol(){
		
	}
	
	public void setEntryOrder(Order order){
		this.entry = order;
	}
	
	public void setExitOrder(Order order){
		this.exit = order;
	}
	
	public void setCode(String code){
		this.code = code;
	}
	
	public String getCode(){
		return code;
	}
	
	public TradeCol(OrderType startingType, Order enter, Order exit, String code){
		super(enter, exit);
		this.code = code;
		setStartingType(startingType);
	}

}
