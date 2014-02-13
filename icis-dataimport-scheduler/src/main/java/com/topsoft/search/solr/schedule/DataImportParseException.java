package com.topsoft.search.solr.schedule;

/**
 * @author wangyg
 */
public class DataImportParseException extends RuntimeException {
  public DataImportParseException() {
    super();
  }

  public DataImportParseException(String message) {
    super(message);
  }

  public DataImportParseException(String message, Throwable cause) {
    super(message, cause);
  }

  public DataImportParseException(Throwable cause) {
    super(cause);
  }
}
