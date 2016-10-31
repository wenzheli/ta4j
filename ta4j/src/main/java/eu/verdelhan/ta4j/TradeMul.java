package eu.verdelhan.ta4j;

import eu.verdelhan.ta4j.Order.OrderType;

public class TradeMul extends Trade{
	
	private String code;
	
	public TradeMul(OrderType startingType, String code){
		super(startingType);
		this.code = code;
	}
	
	public TradeMul(){
		
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
	
	public TradeMul(OrderType startingType, Order enter, Order exit, String code){
		super(enter, exit);
		this.code = code;
		setStartingType(startingType);
	}

}
