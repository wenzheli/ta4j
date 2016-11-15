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
package com.quant.analysis.criteria;

import java.util.List;

import com.quant.AnalysisCriterion;
import com.quant.Strategy;
import com.quant.TimeSeries;

/**
 * An abstract analysis criterion.
 */
public abstract class AbstractAnalysisCriterion implements AnalysisCriterion {

    @Override
    public Strategy chooseBest(TimeSeries series, List<Strategy> strategies) {
        Strategy bestStrategy = strategies.get(0);
        double bestCriterionValue = calculate(series, series.run(bestStrategy));

        for (int i = 1; i < strategies.size(); i++) {
            Strategy currentStrategy = strategies.get(i);
            double currentCriterionValue = calculate(series, series.run(currentStrategy));

            if (betterThan(currentCriterionValue, bestCriterionValue)) {
                bestStrategy = currentStrategy;
                bestCriterionValue = currentCriterionValue;
            }
        }
        return bestStrategy;
    }

    @Override
    public String toString() {
        String[] tokens = getClass().getSimpleName().split("(?=\\p{Lu})", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length - 1; i++) {
            sb.append(tokens[i]).append(' ');
        }
        return sb.toString().trim();
    }
}