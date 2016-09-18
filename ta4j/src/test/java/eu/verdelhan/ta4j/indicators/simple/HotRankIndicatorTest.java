package eu.verdelhan.ta4j.indicators.simple;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.mocks.MockTimeSeries;

public class HotRankIndicatorTest {
	
	private HotRankIndicator hotRankIndicator;
	
	TimeSeries series;
	
	@Before
	public void setUp(){
		series = new MockTimeSeries();
		hotRankIndicator = new HotRankIndicator(series);
	}
	
	@Test
	public void indicatorShouldRetrieveHotRank(){
		for (int i = 0; i < 10; i++){
			assertEquals(hotRankIndicator.getValue(i), series.getTick(i).getHotRank());
		}
	}
}
