package com.cc.fileManage.entity;

import androidx.annotation.NonNull;

public class MethodValue<T1, T2> {

    private T1 valueOne;
    private T2 valueTwo;
    private boolean handle;

    public MethodValue(T1 valueOne, T2 valueTwo) {
        this(valueOne, valueTwo, true);
    }

    public MethodValue(T1 valueOne, T2 valueTwo, boolean handle) {
        this.valueOne = valueOne;
        this.valueTwo = valueTwo;
        this.handle = handle;
    }

    public T1 getValueOne() {
        return valueOne;
    }

    public void setValueOne(T1 valueOne) {
        this.valueOne = valueOne;
    }

    public T2 getValueTwo() {
        return valueTwo;
    }

    public void setValueTwo(T2 valueTwo) {
        this.valueTwo = valueTwo;
    }

    public boolean isHandle() {
        return handle;
    }

    public void setHandle(boolean handle) {
        this.handle = handle;
    }

    @NonNull
    @Override
    public String toString() {
        return "MethodValue{" +
                "valueOne=" + valueOne +
                ", valueTwo=" + valueTwo +
                ", handle=" + handle +
                '}';
    }
}
