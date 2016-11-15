package com.quant.select;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quant.Decimal;
import com.quant.TimeSeries;

/**
 * Portfolio class, encodes the assets and corresponding value. 
 */
public class Portfolio {
	
	/** The begining of the cash */
	private Decimal beginCash;
	/** The current cash  */
	private Decimal balance;
	/** Each assets and its total value */
	private Map<String, Decimal> assets; 
	/** The time that portfolio is created */
	private DateTime dataTime;
	/** The price for each stock when they bought */
	private Map<String, Decimal> assetBuyPrice; 
	/** Portfolio version */
	String version;    
	/** Maximum number of stocks can hold */
	public static final int MAX_NUM_OF_STOCKS = 20;
	
	/** The logger */
    protected final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 *	Constructor. 
	 */
	public Portfolio(){
		this.beginCash = Decimal.ONE;   // by default
		this.balance = Decimal.ONE;    // by default
		assets = new HashMap<String, Decimal>();
		assetBuyPrice = new HashMap<String, Decimal>();
	}


	/**
	 * Add a new asset into the portfolio. 
	 * @param the stock name
	 */ 
	public boolean addAsset(String name, Decimal price){
		if (!isExists(name) && this.size() < MAX_NUM_OF_STOCKS){
			int remainCnt = MAX_NUM_OF_STOCKS - this.size();
			Decimal val = balance.dividedBy(Decimal.valueOf(remainCnt));
			assets.put(name, val);
			assetBuyPrice.put(name, price);
			balance = balance.minus(val);
			return true;		
		}

		return false;
	}

	/**
	 * Add new (or existing) asset into the portfolio. 
	 * @param name    the stock name
	 * @param value   the amount of the asset to be added. 
	 */
	public void addAsset(String name, Decimal value, Decimal price){
		if (value.isLessThan(Decimal.ZERO)){
			throw new IllegalArgumentException("The value shouldn't be larger than the current holding cash");
		}
		if (assets.containsKey(name)){  // if the stock already exists, the do summation 
			Decimal newVal = assets.get(name) .plus(value);
			assets.put(name, newVal);
		} else{   // otherwise directly insert
			if (assets.size() >= MAX_NUM_OF_STOCKS){
				log.info("We already reach the maximum number of stocks, cannot insert a new one any more!");
				return;
			}
			// otherwise, insert a new one
			assets.put(name, value);
			assets.put(name, price);
		}
	}

	/**
	 * Remove a single asset from the portfolio (complete removal)
	 * @param name  the stock name
	 */
	public boolean removeAsset(String name, Decimal sellPrice){
		if (!assets.containsKey(name)){
			return false;
		}
	
		Decimal buyPrice = assetBuyPrice.get(name);
		Decimal val = assets.get(name);
		assets.remove(name);
		assetBuyPrice.remove(name);
		balance = balance.plus(val.multipliedBy(sellPrice.dividedBy(buyPrice)));
		
		return true;
	}

	/**
	 * Remove a single asset from the protfolio,  the amount to be removed 
	 * are determined by the @param percent
	 * @param name     the name (code) of the stock
	 * @param percent  the percentage of the amount to be removed 
	 */
	public void removeAssetByPercent(String name, Decimal percent){
		if (!assets.containsKey(name)){
			throw new IllegalArgumentException("[Deletion] Stock :" + name + "does not exists in the portfolio");
		}

		Decimal newVal = assets.get(name).multipliedBy(Decimal.ONE.minus(percent));
		balance = balance.plus( assets.get(name).multipliedBy(percent));
	}

	
	/** 
	 * @return true if the stock has been already bought before
	 */ 
	public boolean isExists(String code){
		return assets.containsKey(code);
	}

	/**
	 * Invest more cash 
	 */
	public void addCash(Decimal val){
		beginCash.plus(val);
	}

	public List<String> getCodes(){  
		List<String> res = new ArrayList<String>();
		for (String key : assets.keySet()){
			res.add(key);
		}
		return res;
	}
	
	/**
	 * Compute the total value for given date 
	 * @param dateTime             the input date time
	 * @param timeSeriesCollection  time series repository
	 * @return   the total value we hold right now
	 */
    public Decimal totalVal(DateTime dateTime, Map<String, TimeSeries> timeSeriesCollection){
    	Decimal sum = Decimal.ZERO; 
    	sum = sum.plus(balance);
    	for (String code : assets.keySet()){
    		Decimal val = assets.get(code);
    		TimeSeries series = timeSeriesCollection.get(code);
    		Decimal price = series.getClosePrice(dateTime);
    		if (price == null){
    			sum = sum.plus(val);
    		} else{
    			sum = sum.plus(val.multipliedBy(price.dividedBy(assetBuyPrice.get(code))));
    		}
    	}
    	
    	return sum;
    }
	
	/** 
	  * @return the number of assets. 
	  */
	public int size(){
		return assets.size();
	}
	
	
	/**
	 * @return the currently holding value
	 */ 
	public Decimal getHoldingVal(){
		Decimal sum = Decimal.ZERO;
		for (String name : assets.keySet()){
			sum = sum.plus(assets.get(name));
		}
	
		return sum;
	}
	

	/**
	 * @return the current holding value
	 */
	public Decimal getbalance(){
		return balance;
	}

	/**
	 * @return the original holding value
	 */
	public Decimal getBeginCash(){
		return beginCash;
	}

	public void setDateTime(DateTime dateTime){
		this.dataTime = dateTime;
	}
	
	public String toString(){
		String output = "";
		for (String code : assets.keySet()){
			output += code + ":  " + assets.get(code).toString() + "\n";
		}
		
		return output;
	}
	
	public Decimal getBalance(){
		return balance;
	}
}
