package com.microsoft.gctoolkit.online.statistics;

public class NotEnoughSampleException extends ArithmeticException {
    public NotEnoughSampleException() {
        super();
    }

    public NotEnoughSampleException(String s) {
        super(s);
    }
}
