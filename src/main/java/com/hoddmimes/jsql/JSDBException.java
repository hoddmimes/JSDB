package com.hoddmimes.jsql;

public class JSDBException extends Exception
{
    public JSDBException(String pMessage) {
        super(pMessage);
    }

    public JSDBException(Exception pException) {
        super(pException);
    }

    public JSDBException(String pMessage, Exception pException) {
        super(pMessage, pException);
    }
}
