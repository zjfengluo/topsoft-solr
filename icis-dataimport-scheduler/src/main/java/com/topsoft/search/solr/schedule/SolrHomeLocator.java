package com.topsoft.search.solr.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import java.io.File;

/**
 * @author wangyg
 */
public class SolrHomeLocator {
  private static final Logger logger = LoggerFactory.getLogger(SolrHomeLocator.class);
  private final String instanceDir;

  public SolrHomeLocator() {
    this(null);
  }

  public SolrHomeLocator(String instanceDir) {
    if (instanceDir == null) {
      this.instanceDir = locateHome();
      logger.info("new SolrHomeLocator for deduced Solr Home: '{}'", this.instanceDir);
    } else {
      this.instanceDir = normalizeDir(instanceDir);
      logger.info("new SolrHomeLocator for directory: '{}'", this.instanceDir);
    }
  }

  public String getInstanceDir() {
    return instanceDir;
  }

  public String getConfigDir() {
    return instanceDir + "conf/";
  }

  private String locateHome() {
    String home = null;
    // Try JNDI
    try {
      Context c = new InitialContext();
      home = (String) c.lookup("java:comp/env/solr/home");
      logger.info("Using JNDI solr.home: " + home);
    } catch (NoInitialContextException e) {
      logger.info("JNDI not configured for solr (NoInitialContextEx)");
    } catch (NamingException e) {
      logger.info("No solr home in JNDI");
    } catch (RuntimeException ex) {
      logger.warn("Odd RuntimeException while testing for JNDI: " + ex.getMessage());
    }

    // Now try system property
    if (home == null) {
      String prop = "solr.solr.home";
      home = System.getProperty(prop);
      if (home != null) {
        logger.info("using system property " + prop + ": " + home);
      }
    }

    // if all else fails, try
    if (home == null) {
      home = "solr/";
      logger.info("solr home defaulted to '" + home + "' (could not find system property or JNDI)");
    }
    return normalizeDir(home);

  }

  /**
   * Ensures a directory name always ends with a '/'.
   */
  private String normalizeDir(String path) {
    return (path != null && (!(path.endsWith("/") || path.endsWith("\\")))) ? path + File.separator : path;

  }
}
