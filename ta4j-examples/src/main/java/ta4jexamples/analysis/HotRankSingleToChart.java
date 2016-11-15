package ta4jexamples.analysis;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.quant.TimeSeries;
import com.quant.TimeSeriesRepo;
import com.quant.TradingRecordMul;
import com.quant.TradingRule;
import com.quant.analysis.CashFlow;
import com.quant.factory.TimeSeriesRepoBuilder;
import com.quant.trading.rules.buying.HotRankBuy;
import com.quant.trading.rules.selling.HotRankSell;

public class HotRankSingleToChart extends ApplicationFrame{
	public HotRankSingleToChart(final String title, CashFlow cashFlow, TimeSeries series) {

        super(title);

        final XYDataset dataset = createDataset(cashFlow, series);
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
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
	
	public static void main(String[] args) throws Exception{
		String path = "/Users/wenzheli/Documents/workspace/quant-data/data/stocks";
		TimeSeriesRepoBuilder builder = new TimeSeriesRepoBuilder(path);
		TimeSeriesRepo repo = builder.build();
		
		TradingRule buying = new HotRankBuy(2);
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
		
		
		// draw the chart
		final HotRankSingleToChart demo = new HotRankSingleToChart("Line Chart Demo 6", cashFlow, series);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
	}
}
