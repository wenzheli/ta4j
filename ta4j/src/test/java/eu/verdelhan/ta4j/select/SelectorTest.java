package eu.verdelhan.ta4j.select;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import eu.verdelhan.ta4j.TimeSeriesRepo;
import eu.verdelhan.ta4j.factory.TimeSeriesRepoBuilder;

public class SelectorTest {
	
	protected TimeSeriesRepo repo;
	
	@Before
    public void setUp() throws IOException {
		String path = "/Users/wenzheli/Documents/workspace/quant-data/data/stocks";
		TimeSeriesRepoBuilder builder = new TimeSeriesRepoBuilder(path);
		repo = builder.build();
	}
	
	
	@Test
	public void selectFullDatesTest(){
		int period = 30;
		List<DateTime> dates = Selector.getFullDates(repo, period);
		for (DateTime date : dates){
			System.out.println(date);
		}
		
		assertEquals(dates.size(), 30);
	}
}
