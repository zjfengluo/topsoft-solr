package com.topsoft.search.solr.schedule;

/**
 * @author wangyg
 */
public class DataImportException extends RuntimeException {
  public DataImportException() {
  }

  public DataImportException(String message) {
    super(message);
  }

  public DataImportException(String message, Throwable cause) {
    super(message, cause);
  }

  public DataImportException(Throwable cause) {
    super(cause);
  }
}
