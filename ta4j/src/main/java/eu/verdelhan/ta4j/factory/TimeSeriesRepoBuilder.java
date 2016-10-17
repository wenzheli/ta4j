package eu.verdelhan.ta4j.factory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TimeSeriesRepo;

public class TimeSeriesRepoBuilder {
	private String dirPath;
	
	public TimeSeriesRepoBuilder(String path){
		this.dirPath = path;
	}
	
	public TimeSeriesRepo build() throws IOException{
 		BufferedReader br = null;
		String line = "";
		File dir = new File(dirPath);
		
		List<TimeSeries> series = new ArrayList<TimeSeries>();
		for (File f : dir.listFiles()){
			System.out.println("processing " + f.getName());
			br = new BufferedReader(new FileReader(f));
			br.readLine();    // skip the header line
			String code = getStockCode(f.getName());
			
			List<String> lines = new ArrayList<String>();
            while ((line = br.readLine()) != null) { // loop each stock info 
            	lines.add(line);
            }
            List<Tick> ticks = buildTicks(lines);
            series.add(new TimeSeries(code, ticks));
		}
		
		return new TimeSeriesRepo(series);
	}
	
	private List<Tick> buildTicks(List<String> lines){
		List<Tick> ticks = new ArrayList<Tick>();
		int n = lines.size();
		for (int i = n-1; i >= 0; i--){ // from reverse order
			String[] strs = lines.get(i).split(",");
			
			// format date
			DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
			DateTime date = f.parseDateTime(strs[0]);
			
			double open = Double.parseDouble(strs[1]);
			double high = Double.parseDouble(strs[2]);
			double close = Double.parseDouble(strs[3]);
			double low = Double.parseDouble(strs[4]);
			double vol = Double.parseDouble(strs[5]);
			
			ticks.add(new Tick(date, open, high, low, close, vol));
		}
		
		return ticks;
	}
	
	/**
	 * Parse the file name and get the stock code
	 * The format of file name as follows:  [code].csv
	 * @param name
	 * @return
	 */
	private String getStockCode(String name){
		String[] strs = name.split("\\.");
		return strs[0];
	}
	
	public static void main(String[] args) throws IOException{
		String path = "/Users/liwenzhe/Documents/workspace/DataWrapper/data/stocks";
		TimeSeriesRepoBuilder builder = new TimeSeriesRepoBuilder(path);
		builder.build();
	}
	
	
}
