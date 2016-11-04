/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Marc de Verdelhan & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ta4jexamples.analysis;

import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TimeSeriesRepo;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.TradingRecordMul;
import eu.verdelhan.ta4j.TradingRule;
import eu.verdelhan.ta4j.analysis.CashFlow;
import eu.verdelhan.ta4j.factory.TimeSeriesRepoBuilder;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.trading.rules.buying.HotRankBuy;
import eu.verdelhan.ta4j.trading.rules.buying.SMACrossedUp;
import eu.verdelhan.ta4j.trading.rules.buying.SMAMultipleUp;
import eu.verdelhan.ta4j.trading.rules.buying.VOLMultipleUp;
import eu.verdelhan.ta4j.trading.rules.selling.HotRankSell;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ta4jexamples.loaders.CsvTradesLoader;
import ta4jexamples.strategies.MovingMomentumStrategy;

/**
 * This class builds a graphical chart showing the cash flow of a strategy.
 */
public class CashFlowToChart {

    /**
     * Builds a JFreeChart time series from a Ta4j time series and an indicator.
     * @param tickSeries the ta4j time series
     * @param indicator the indicator
     * @param name the name of the chart time series
     * @return the JFreeChart time series
     */
    private static org.jfree.data.time.TimeSeries buildChartTimeSeries(TimeSeries tickSeries, Indicator<Decimal> indicator, String name) {
        org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(name);
        for (int i = 0; i < tickSeries.getTickCount(); i++) {
            Tick tick = tickSeries.getTick(i);
            chartTimeSeries.add(new Minute(tick.getEndTime().toDate()), indicator.getValue(i).toDouble());
        }
        return chartTimeSeries;
    }

    /**
     * Adds the cash flow axis to the plot.
     * @param plot the plot
     * @param dataset the cash flow dataset
     */
    private static void addCashFlowAxis(XYPlot plot, TimeSeriesCollection dataset) {
        final NumberAxis cashAxis = new NumberAxis("Cash Flow Ratio");
        cashAxis.setAutoRangeIncludesZero(false);
        plot.setRangeAxis(1, cashAxis);
        plot.setDataset(1, dataset);
        plot.mapDatasetToRangeAxis(1, 1);
        final StandardXYItemRenderer cashFlowRenderer = new StandardXYItemRenderer();
        cashFlowRenderer.setSeriesPaint(0, Color.blue);
        plot.setRenderer(1, cashFlowRenderer);
    }

    /**
     * Displays a chart in a frame.
     * @param chart the chart to be displayed
     */
    private static void displayChart(JFreeChart chart) {
        // Chart panel
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        panel.setPreferredSize(new Dimension(1024, 400));
        // Application frame
        ApplicationFrame frame = new ApplicationFrame("Ta4j example - Cash flow to chart");
        frame.setContentPane(panel);
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }
    
    
    private XYDataset createDataset(CashFlow cashFlow, TimeSeries benchMark) {
        
    	
        final XYSeries series1 = new XYSeries("cash flow");
        for (int i = 0; i < cashFlow.getValues().size(); i++){
        	series1.add(i,cashFlow.getValue(i).toDouble());
        }
       
        final XYSeries series2 = new XYSeries("hs300");
        for (int i = 0; i < benchMark.getTickCount(); i++){
        	series2.add(i, benchMark.getTick(i).getClosePrice().toDouble());
        }
       
        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        dataset.addSeries(series2);
                
        return dataset;
        
    }
    
private JFreeChart createChart(final XYDataset dataset) {
        
        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
            "Line Chart Demo 6",      // chart title
            "X",                      // x axis label
            "Y",                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

//        final StandardLegend legend = (StandardLegend) chart.getLegend();
  //      legend.setDisplaySeriesShapes(true);
        
        // get a reference to the plot for further customisation...
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
    //    plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        
        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(1, false);
        plot.setRenderer(renderer);

        // change the auto tick unit selection to integer units only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        // OPTIONAL CUSTOMISATION COMPLETED.
                
        return chart;
        
    }


	public static void drawHotRankMultiple() throws Exception{
		String path = "/Users/wenzheli/Documents/workspace/quant-data/data/stocks";
		TimeSeriesRepoBuilder builder = new TimeSeriesRepoBuilder(path);
		TimeSeriesRepo repo = builder.build();
		
		List<TradingRule> buyingRules = new ArrayList<TradingRule>();
		buyingRules.add(new HotRankBuy(20));
		TradingRule smaCrossedUp = new SMACrossedUp(5,10,30); 
		TradingRule smaMultipleUp = new SMAMultipleUp(5,10,30);
		TradingRule volMultipleUp = new VOLMultipleUp(5,10,20);
		
		buyingRules.add(smaCrossedUp);
		buyingRules.add(smaMultipleUp);
		buyingRules.add(volMultipleUp);
		
		
		TradingRule selling = new HotRankSell(8);
		
		String start = "2016-09-09";
		String end = "2016-10-14";

		DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
		
		TradingRecordMul tradingRecordMul = repo.runHotRankSingle(buyingRules, selling, f.parseDateTime(start), f.parseDateTime(end));
		
		CashFlow cashFlow = new CashFlow(repo, tradingRecordMul, f.parseDateTime(start), f.parseDateTime(end));
		
		String code = "hs300"; 
		TimeSeries hs300 = repo.get(code);
		int startIdx = hs300.getIndexFromDate(f.parseDateTime(start));
		int endIdx = hs300.getIndexFromDate(f.parseDateTime(end));
		TimeSeries series = hs300.subseriesDeep(startIdx, endIdx);
		
		System.out.println("the strategy is: ");
		for(int i = 0; i <  cashFlow.getValues().size(); i++){
			System.out.println(cashFlow.getValue(i));
		}
			
		
		System.out.println("the hs300 is: ");
		for(int i = 0; i <= 18; i++){
			System.out.println(series.getTick(i).getClosePrice());
		}
		
	}
    
    public static void drawHotRankSingle() throws Exception{
    	String path = "/Users/wenzheli/Documents/workspace/quant-data/data/stocks";
		TimeSeriesRepoBuilder builder = new TimeSeriesRepoBuilder(path);
		TimeSeriesRepo repo = builder.build();
		
		TradingRule buying = new HotRankBuy(5);
		TradingRule selling = new HotRankSell(5);
		
		String start = "2016-09-09";
		String end = "2016-10-14";

		DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd");
		
		TradingRecordMul tradingRecordMul = repo.runHotRankSingle(buying, selling, f.parseDateTime(start), f.parseDateTime(end));
		
		CashFlow cashFlow = new CashFlow(repo, tradingRecordMul, f.parseDateTime(start), f.parseDateTime(end));
		
		String code = "hs300"; 
		TimeSeries hs300 = repo.get(code);
		int startIdx = hs300.getIndexFromDate(f.parseDateTime(start));
		int endIdx = hs300.getIndexFromDate(f.parseDateTime(end));
		TimeSeries series = hs300.subseriesDeep(startIdx, endIdx);
		
		System.out.println("the strategy is: ");
		for(int i = 0; i <  cashFlow.getValues().size(); i++){
			System.out.println(cashFlow.getValue(i));
		}
			
		
		System.out.println("the hs300 is: ");
		for(int i = 0; i <= 18; i++){
			System.out.println(series.getTick(i).getClosePrice());
		}
		
		/**
         * Building chart datasets
         */
        TimeSeriesCollection datasetAxis1 = new TimeSeriesCollection();
        datasetAxis1.addSeries(buildChartTimeSeries(series, new ClosePriceIndicator(series), "hs300"));
        TimeSeriesCollection datasetAxis2 = new TimeSeriesCollection();
        datasetAxis2.addSeries(buildChartTimeSeries(series, cashFlow, "Strategy: use the rank of hot discussions"));
        
        
        /**
         * Creating the chart
         */
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "hs300 vs Strategy", // title
                "Date", // x-axis label
                "Price", // y-axis label
                datasetAxis1, // data
                true, // create legend?
                true, // generate tooltips?
                false // generate URLs?
                );
        XYPlot plot = (XYPlot) chart.getPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MM-dd HH:mm"));

        /**
         * Adding the cash flow axis (on the right)
         */
        addCashFlowAxis(plot, datasetAxis2);

        /**
         * Displaying the chart
         */
        displayChart(chart);
		
    }

    public static void main(String[] args) throws Exception {
    	//drawHotRankSingle();
    	drawHotRankMultiple();
    	
        
        
    }
}
