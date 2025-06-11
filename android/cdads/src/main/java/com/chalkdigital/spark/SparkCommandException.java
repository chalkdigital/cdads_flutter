package com.chalkdigital.spark;

public class SparkCommandException extends Exception{
    SparkCommandException() {
        super();
    }

    SparkCommandException(String detailMessage) {
        super(detailMessage);
    }

    SparkCommandException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    SparkCommandException(Throwable throwable) {
        super(throwable);
    }
}
