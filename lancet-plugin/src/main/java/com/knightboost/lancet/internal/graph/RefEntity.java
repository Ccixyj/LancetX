package com.knightboost.lancet.internal.graph;

interface RefEntity {

    void inc();

    void dec();

    boolean isFree();

    int getCount();

    void setCount(int count);
}
