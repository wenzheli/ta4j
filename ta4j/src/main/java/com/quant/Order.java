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
package com.quant;

import org.joda.time.DateTime;

/**
 * An order.
 * <p>
 * The order is defined by:
 * <ul>
 * <li>the index (in the {@link TimeSeries time series}) it is executed
 * <li>a {@link OrderType type} (BUY or SELL)
 * <li>a price (optional)
 * <li>an amount to be (or that was) ordered (optional)
 * </ul>
 * A {@link Trade trade} is a pair of complementary orders.
 */
public class Order {

    /**
     * The type of an {@link Order order}.
     * <p>
     * A BUY corresponds to a <i>BID</i> order.<p>
     * A SELL corresponds to an <i>ASK</i> order.
     */
    public enum OrderType {

        BUY {
            @Override
            public OrderType complementType() {
                return SELL;
            }
        },
        SELL {
            @Override
            public OrderType complementType() {
                return BUY;
            }
        };

        /**
         * @return the complementary order type
         */
        public abstract OrderType complementType();
    }

    /** The code of the stock */
    private String name;
    
    /** Type of the order */
    private OrderType type;

    /** The time that order placed */
    private DateTime dateTime;

    /** The index the order was executed */
    private int index;

    /** The price for the order */
    private Decimal price = Decimal.NaN;
    
    /** The amount to be (or that was) ordered */
    private Decimal amount = Decimal.NaN;
    
    /**
     * Constructor.
     * @param index the index the order is executed
     * @param type the type of the order
     */
    protected Order(int index, OrderType type) {
        this.type = type;
        this.index = index;
    }


    protected Order(String name, int index, OrderType type, DateTime dateTime, Decimal price){
        this.name = name;
        this.index = index;
        this.type = type;
        this.dateTime = dateTime;
        this.price = price;
    }

    /**
     * Constructor.
     * @param index the index the order is executed
     * @param type the type of the order
     * @param price the price for the order
     * @param amount the amount to be (or that was) ordered
     */
    protected Order(int index, OrderType type, Decimal price, Decimal amount) {
        this(index, type);
        this.price = price;
        this.amount = amount;
    }

    protected Order(String name, int index, OrderType type, Decimal price, Decimal amount) {
        this(index, type);
        this.name = name;
        this.price = price;
        this.amount = amount;
    }
    
    protected Order(String name, int index, DateTime dateTime, OrderType type, Decimal price, Decimal amount){
        this(name, index, type, price, amount);
        this.dateTime = dateTime;
    }


    /**
     * @return the type of the order (BUY or SELL)
     */
    public OrderType getType() {
        return type;
    }

    /**
     * @return true if this is a BUY order, false otherwise
     */
    public boolean isBuy() {
        return type == OrderType.BUY;
    }

    /**
     * @return true if this is a SELL order, false otherwise
     */
    public boolean isSell() {
        return type == OrderType.SELL;
    }

    /**
     * @return the stock name
     */
    public String getName(){
        return name;
    }

    /**
     * Set the name of the stock 
     */
    public void setName(String name){
        this.name = name;
    }

    /** Set the time that order placed */
    public void setDateTime(DateTime dateTime){
        this.dateTime = dateTime;
    }

    /** Get the time the order placed */
    public DateTime getDateTime(){
        return dateTime;
    }

    /**
     * @return the index the order is executed
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return the price for the order
     */
    public Decimal getPrice() {
        return price;
    }

    /**
     * @return the amount to be (or that was) ordered
     */
    public Decimal getAmount() {
        return amount;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 29 * hash + this.index;
        hash = 29 * hash + (this.price != null ? this.price.hashCode() : 0);
        hash = 29 * hash + (this.amount != null ? this.amount.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Order other = (Order) obj;
        if (this.type != other.type) {
            return false;
        }
        if (this.index != other.index) {
            return false;
        }
        if (this.price != other.price && (this.price == null || !this.price.equals(other.price))) {
            return false;
        }
        if (this.amount != other.amount && (this.amount == null || !this.amount.equals(other.amount))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Order{" + "type=" + type + ", index=" + index + ", price=" + price + ", amount=" + amount + '}';
    }
    
    /**
     * @param index the index the order is executed
     * @return a BUY order
     */
    public static Order buyAt(int index) {
        return new Order(index, OrderType.BUY);
    }

    /**
     * @param index the index the order is executed
     * @param price the price for the order
     * @param amount the amount to be (or that was) bought
     * @return a BUY order
     */
    public static Order buyAt(int index, Decimal price, Decimal amount) {
        return new Order(index, OrderType.BUY, price, amount);
    }

    /**
     * @param index the index the order is executed
     * @return a SELL order
     */
    public static Order sellAt(int index) {
        return new Order(index, OrderType.SELL);
    }

    /**
     * @param index the index the order is executed
     * @param price the price for the order
     * @param amount the amount to be (or that was) sold
     * @return a SELL order
     */
    public static Order sellAt(int index, Decimal price, Decimal amount) {
        return new Order(index, OrderType.SELL, price, amount);
    }
}
