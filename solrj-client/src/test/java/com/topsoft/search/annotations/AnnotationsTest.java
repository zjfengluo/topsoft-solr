
package com.topsoft.search.annotations;

import org.apache.solr.client.solrj.beans.Field;
import org.junit.Test;

import java.util.Date;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author wangyg
 */
public class AnnotationsTest {
	
  @Highlight
  @DocumentId
  private Long id;

  @Highlight
  private String name;

  @Field("shengri")
  @Highlight
  private Date birthday;

  @Highlight("someValueElse")
  private Object someValue;


  @Test
  public void testGetHighlightFieldNames() {
    int i = 3;
    Set<String> fieldNames = Annotations.getHighlightFieldNames(AnnotationsTest.class);
    assertThat(fieldNames, hasItems("name", "id", "shengri", "someValueElse"));
  }

  @Test
  public void testGetDocumentIdName() {
    assertThat("id", is(Annotations.getDocumentIdName(AnnotationsTest.class)));
  }
}
