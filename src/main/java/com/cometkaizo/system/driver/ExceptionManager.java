package com.cometkaizo.system.driver;

public interface ExceptionManager {
    Throwable handleException(Exception e);
    Throwable handleError(Error err);
}
