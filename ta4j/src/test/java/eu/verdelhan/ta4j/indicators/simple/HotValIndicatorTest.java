package eu.verdelhan.ta4j.indicators.simple;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.mocks.MockTimeSeries;

public class HotValIndicatorTest {
	private HotValIndicator hotValIndicator;
	
	TimeSeries series;
	
	@Before
	public void setUp(){
		series = new MockTimeSeries();
		hotValIndicator = new HotValIndicator(series);
	}
	
	@Test
	public void indicatorShouldRetrieveHotVal(){
		for (int i = 0; i < 10; i++){
			assertEquals(hotValIndicator.getValue(i), series.getTick(i).getHotVal());
		}
	}
}
