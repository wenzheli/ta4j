package eu.verdelhan.tailtest.mocks;

import eu.verdelhan.tailtest.TimeSeries;
import eu.verdelhan.tailtest.tick.DefaultTick;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 * A time series with sample data.
 */
public class MockTimeSeries implements TimeSeries {

	private List<DefaultTick> ticks;

	public MockTimeSeries(double... data) {
		ticks = new ArrayList<DefaultTick>();
		for (int i = 0; i < data.length; i++) {
			ticks.add(new DefaultTick(new DateTime().withMillisOfSecond(i),data[i]));
		}
	}

	public MockTimeSeries(List<DefaultTick> ticks) {
		this.ticks = ticks;
	}

	public MockTimeSeries(double[] data, DateTime[] times) {
		if (data.length != times.length) {
			throw new IllegalArgumentException();
		}
		ticks = new ArrayList<DefaultTick>();
		for (int i = 0; i < data.length; i++) {
			ticks.add(new DefaultTick(data[i], times[i]));
		}
	}

	public MockTimeSeries(DateTime... dates) {
		ticks = new ArrayList<DefaultTick>();
		int i = 1;
		for (DateTime date : dates) {
			ticks.add(new DefaultTick(date, i++));
		}
	}

	public MockTimeSeries() {
		ticks = new ArrayList<DefaultTick>();
		for (double i = 0d; i < 10; i++) {
			DefaultTick tick = new DefaultTick(new DateTime(0), i, i + 1, i + 2, i + 3, i + 4, i + 5, i + 6, (int) (i + 7));
			ticks.add(tick);
		}
	}

	public DefaultTick getTick(int i) {
		return ticks.get(i);
	}

	public int getSize() {
		return ticks.size();
	}

	public int getBegin() {
		return 0;
	}

	public int getEnd() {
		return ticks.size() - 1;
	}

	public String getName() {
		return "SampleTimeSeries";
	}

	public String getPeriodName() {
		return ticks.get(0).getEndTime().toString("hh:mm dd/MM/yyyy - ")
				+ ticks.get(this.getEnd()).getEndTime().toString("hh:mm dd/MM/yyyy");
	}

	public Period getPeriod() {
		return new Period(Math.min(ticks.get(1).getEndTime().getMillis() - ticks.get(0).getEndTime().getMillis(),
				ticks.get(2).getEndTime().getMillis()- ticks.get(1).getEndTime().getMillis()));
	}
}