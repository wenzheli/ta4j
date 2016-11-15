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
package com.quant.indicators.trackers.ichimoku;

import com.quant.Decimal;
import com.quant.Indicator;
import com.quant.TimeSeries;
import com.quant.indicators.CachedIndicator;
import com.quant.indicators.helpers.HighestValueIndicator;
import com.quant.indicators.helpers.LowestValueIndicator;
import com.quant.indicators.simple.MaxPriceIndicator;
import com.quant.indicators.simple.MinPriceIndicator;

/**
 * An abstract class for Ichimoku clouds indicators.
 * <p>
 * @see http://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:ichimoku_cloud
 */
public abstract class AbstractIchimokuLineIndicator extends CachedIndicator<Decimal>{

    /** The period high */
    private final Indicator<Decimal> periodHigh;

    /** The period low */
    private final Indicator<Decimal> periodLow;

    /**
     * Contructor.
     * @param series the series
     * @param timeFrame the time frame
     */
    public AbstractIchimokuLineIndicator(TimeSeries series, int timeFrame) {
        super(series);
        periodHigh = new HighestValueIndicator(new MaxPriceIndicator(series), timeFrame);
        periodLow = new LowestValueIndicator(new MinPriceIndicator(series), timeFrame);
    }

    @Override
    protected Decimal calculate(int index) {
        return periodHigh.getValue(index).plus(periodLow.getValue(index)).dividedBy(Decimal.TWO);
    }
}
