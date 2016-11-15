package com.quant.select;

import java.util.List;
import java.util.Map;

import com.joptimizer.functions.ConvexMultivariateRealFunction;
import com.joptimizer.functions.LinearMultivariateRealFunction;
import com.joptimizer.functions.PDQuadraticMultivariateRealFunction;
import com.joptimizer.optimizers.JOptimizer;
import com.joptimizer.optimizers.OptimizationRequest;
import com.quant.Decimal;
import com.quant.TimeSeries;
import com.quant.indicators.simple.ClosePriceIndicator;
import com.quant.indicators.statistics.CovarianceIndicator;
import com.quant.indicators.trackers.SMAIndicator;

public class PortfolioCreator {
	
	/** candidate list of time series, which has been selected */ 
	private List<TimeSeries> candidates;
	/** list of stock codes for candidate list */
	private List<String> codes;
	/** the length of time which used to compute the summary of statistics */ 
	private int timeFrame;
	/** (optional) if we set this value, the end index would be : endIndex - beforeEndIndex, 
	 *  used for evaluating the portfolio algorithm
	 */
	/** risk factor  */
	private double risk = 0.1;  // default 
	
	private int beforeEndIndex = 0; 
	
	public PortfolioCreator(List<TimeSeries> candidates, List<String> codes, int timeFrame){
		this.candidates = candidates;
		this.codes = codes;
		this.timeFrame = timeFrame;
	}
	
	public void beforeEndIndex(int timeframe){
		this.beforeEndIndex = timeframe;
	}
	
	public void setRiskFactor(double risk){
		this.risk = risk;
	}
	
	/**
	 * The function used to compute the optimal portfolio
	 * It uses mean-variance optimization scheme.  
	 * @return 
	 * @throws Exception 
	 */
	public double[] create() throws Exception{
		int n = candidates.size();
		// covariance matrix for the list of stock returns
		double[][] covMatrix = calCovMatrix();
		
		System.out.println("the covariance matrix is");
		java.text.DecimalFormat   df   =new   java.text.DecimalFormat("#.00");  
		for (int i= 0; i < covMatrix.length; i++){
			for (int j = 0; j < covMatrix[0].length; j++){
				System.out.print(df.format(covMatrix[i][j]) + "\t");
			}
			System.out.println();
		}
		// mean vector for expected return for each stock 
		double[] meanVec = calMeans();
		System.out.println("the mean vector is");
		for (int i = 0; i < meanVec.length; i++){
			System.out.println(meanVec[i]);
		}
		
		
		// construct the mathematical programming  -  Quadratic programming
		double[][] P = covMatrix; 
		for (int i = 0; i < P.length; i++){
			for (int j = 0; j < P[0].length; j++){
				P[i][j] *= 2;   // scaling by factor of 2
			}
		}
		
		double[] q = meanVec;
		for (int i = 0; i < q.length; i++){
			q[i] *= -risk;	
		}
		
		double[][] A = new double[1][n];
		for (int i = 0; i < n; i++){
				A[0][i] = 1.0;
		}
		
		double[] b = new double[]{1};
		
		double[][] G = new double[n][n];
		for (int i = 0; i < n; i++){
			for (int j = 0; j < n; j++){
				G[i][j] = 0;
			}
			
			G[i][i] = -1;
		}
		
		
		
		PDQuadraticMultivariateRealFunction objectiveFunction = new PDQuadraticMultivariateRealFunction(P, q, 0);
		//inequalities
		ConvexMultivariateRealFunction[] inequalities = new ConvexMultivariateRealFunction[n];
		for (int i = 0; i < n; i++){
			inequalities[i] = new LinearMultivariateRealFunction(G[i], 0);
		}
		
		//optimization problem
		OptimizationRequest or = new OptimizationRequest();
		or.setF0(objectiveFunction);
		
		//or.setInitialPoint(new double[] { 0.1, 0.9});
		or.setFi(inequalities); //if you want x>0 and y>0
		or.setA(A);
		or.setB(b);
		or.setToleranceFeas(1.E-12);
		or.setTolerance(1.E-12);
		
		//optimization
		JOptimizer opt = new JOptimizer();
		opt.setOptimizationRequest(or);
		int returnCode = opt.optimize();
		double[] sol = opt.getOptimizationResponse().getSolution();
		
		return sol;
	}
	
	private double[] calMeans(){
		int n = candidates.size();
		double[] meanReturns = new double[n];
		
		// for each stock, compute the mean (expected) return 
		for (int i = 0; i < n; i++){ // for each candidate stock i 
			double s = 0.0;
			TimeSeries series = candidates.get(i); 
			// compute the expected return for the period specified by timeFrame
			ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
			double after = closePrice.getValue(series.getEnd() - beforeEndIndex).toDouble();
			double before = closePrice.getValue(series.getEnd() - beforeEndIndex - timeFrame).toDouble();
			meanReturns[i] = (after - before)/before;
		}
		
		return meanReturns;
	}
	
	/**
	 * Compute the covariance matrix for list of stocks
	 * @return
	 */
	private double[][] calCovMatrix(){
		int n = candidates.size();
		double[][] covMatrix = new double[n][n];
		for (int i = 0; i < n; i++){
			for (int j = 0; j < n; j++){
				// compute the covariance between two stocks 
				TimeSeries series1 = candidates.get(i);
				TimeSeries series2 = candidates.get(j);
				
				ClosePriceIndicator closePrice1 = new ClosePriceIndicator(series1);
				ClosePriceIndicator closePrice2 = new ClosePriceIndicator(series2);
				SMAIndicator smaIndicator1 = new SMAIndicator(closePrice1, timeFrame);
				SMAIndicator smaIndicator2 = new SMAIndicator(closePrice2, timeFrame);
				
				Decimal mean1 = smaIndicator1.getValue(series1.getEnd() - beforeEndIndex);
				Decimal mean2 = smaIndicator2.getValue(series2.getEnd() - beforeEndIndex);
				
				Decimal covariance = Decimal.ZERO;
				for (int k = 0; k < timeFrame; k++){
					Decimal mul = closePrice1.getValue(series1.getEnd() - beforeEndIndex - k).minus(mean1)
							.multipliedBy(closePrice2.getValue(series2.getEnd() - beforeEndIndex - k).minus(mean2));
					covariance = covariance.plus(mul);
				}
				
				covariance = covariance.dividedBy(Decimal.valueOf(timeFrame));
				covMatrix[i][j] = covariance.toDouble();
			}
		}
		
		return covMatrix;
	}
	
	
	
	public static void main(String[] args) throws Exception{
		// Objective function
				double[][] P = new double[][] {{ 1., 0.4 }, { 0.4, 1. }};
				PDQuadraticMultivariateRealFunction objectiveFunction = new PDQuadraticMultivariateRealFunction(P, null, 0);

				//equalities
				double[][] A = new double[][]{{1,1}};
				double[] b = new double[]{1};

				//inequalities
				ConvexMultivariateRealFunction[] inequalities = new ConvexMultivariateRealFunction[2];
				inequalities[0] = new LinearMultivariateRealFunction(new double[]{-1, 0}, 0);
				inequalities[1] = new LinearMultivariateRealFunction(new double[]{0, -1}, 0);
				
				//optimization problem
				OptimizationRequest or = new OptimizationRequest();
				or.setF0(objectiveFunction);
				or.setInitialPoint(new double[] { 0.1, 0.9});
				//or.setFi(inequalities); //if you want x>0 and y>0
				or.setA(A);
				or.setB(b);
				or.setToleranceFeas(1.E-12);
				or.setTolerance(1.E-12);
				
				//optimization
				JOptimizer opt = new JOptimizer();
				opt.setOptimizationRequest(or);
				int returnCode = opt.optimize();
	}
	
	
	
	
	
}
