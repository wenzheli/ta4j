package eu.verdelhan.ta4j.select;

import java.util.Map;

import org.joda.time.DateTime;

/**
 * Portfolio class, encodes the assets and corresponding weights. 
 */
public class Portfolio {
	
	/** Asset and its weight */
	private Map<String, Double> assets; 
	/** The expected run for this portfolio */
	private double expectedReturn; 
	/** The time that portfolio is created */ 
	private DateTime dataTime;
	/** Portfolio version */
	String version;    
	
	public Portfolio(Map<String, Double> assets, double expectedReturn){
		this.assets = assets;
		this.expectedReturn = expectedReturn;
	}
	
	public Portfolio(Map<String, Double> assets, double expectedReturn, DateTime dateTime){
		this.assets = assets;
		this.expectedReturn = expectedReturn;
		this.dataTime = dateTime;
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
	
	
}
