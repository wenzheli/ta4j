package eu.verdelhan.ta4j;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeSeriesRepo {
	
	/** The logger */
	private final Logger log = LoggerFactory.getLogger(getClass());
	/** List of TimeSeries */
	private final List<TimeSeries> timeSeriesCollection; 
	/** The size of the collection */
	private int size; 
	
	
	public TimeSeriesRepo(List<TimeSeries> collection){
		this.timeSeriesCollection = collection;
		this.size = collection.size();
	}
	
	public int size(){
		return size;
	}
	
	public TimeSeries get(int index){
		return timeSeriesCollection.get(index);
	}
	
}
