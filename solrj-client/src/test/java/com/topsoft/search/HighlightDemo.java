package com.topsoft.search;

import com.topsoft.search.annotations.DocumentId;
import com.topsoft.search.annotations.Highlight;
import com.topsoft.search.domain.HighlightWrapper;
import com.topsoft.search.domain.Page;
import com.topsoft.search.domain.Pageable;
import com.topsoft.search.support.GenericSolrQueryProfile;
import com.topsoft.search.support.HighlightParameters;
import com.topsoft.search.support.HighlightQueryPreProcessor;
import com.topsoft.search.support.HighlightWrapperResultTransformer;
import com.topsoft.search.support.PagedSolrQueryProfile;
import org.apache.solr.client.solrj.beans.Field;

import java.util.Date;
import java.util.List;

/**
 * @author wangyg
 */
public class HighlightDemo {
  private final AnnotationableSolrMultiCoreQuery query;

  // 设置highlight参数
  private final HighlightParameters enterpriseHighlightParams = HighlightParameters.builder()
      .setFields(Enterprise.class)
      .setFragsize(100)
      .setSnippets(1)
      .setRequireFieldMatch(true)
      .setSimplePre("<span class=\"highlight\">")
      .setSimplePost("</span>")
      .build();

  private final HighlightParameters personHighlightParas = HighlightParameters.builder()
      .setFields("name", "gender", "birthday")
      .build();

  public HighlightDemo(String baseUrl) {
    this.query = new AnnotationableSolrMultiCoreQuery(baseUrl);
  }

  public <T> List<HighlightWrapper<T>> findAllWithHighlightFrom(String coreName, String queryString, HighlightParameters params, Class<T> clazz) {
    // 设置高亮参数
    HighlightQueryPreProcessor preProcessor = new HighlightQueryPreProcessor(params);
    // 根据highlight annotation信息对结果集自动封装为HighlightWrapper<T>对象
    HighlightWrapperResultTransformer<T> transformer = new HighlightWrapperResultTransformer<T>(clazz);

    // 查询所有结果集，不分页
    GenericSolrQueryProfile<HighlightWrapper<T>> profile = new GenericSolrQueryProfile<HighlightWrapper<T>>(preProcessor, transformer);

    // 多核查询
    // query.findAll(queryString, profile, executor);
    return query.findAllFrom(coreName, queryString, profile);
  }

  public <T> Page<HighlightWrapper<T>> findWithHighlightFrom(String coreName, String queryString, Pageable pageRequest, HighlightParameters params, Class<T> clazz) {
    // 设置高亮参数
    HighlightQueryPreProcessor preProcessor = new HighlightQueryPreProcessor(params);
    // 根据highlight annotation信息对结果集自动封装为HighlightWrapper<T>对象
    HighlightWrapperResultTransformer<T> transformer = new HighlightWrapperResultTransformer<T>(clazz);

    // 分页查询
    PagedSolrQueryProfile<HighlightWrapper<T>> profile = new PagedSolrQueryProfile<HighlightWrapper<T>>(preProcessor, transformer, pageRequest);

    // 多核查询
    // query.find(queryString, profile, executor);
    return query.findFrom(coreName, queryString, profile);
  }


  public static class Enterprise {
    @Field
    @DocumentId
    private Long id;
    @Field("enName")
    @Highlight
    private String name;
    @Field("enAddress")
    @Highlight
    private String address;
    @Field("enRegDate")
    @Highlight
    private Date regDate;

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getAddress() {
      return address;
    }

    public void setAddress(String address) {
      this.address = address;
    }

    public Date getRegDate() {
      return regDate;
    }

    public void setRegDate(Date regDate) {
      this.regDate = regDate;
    }
  }

  public static class Person {
    private Date birthday;
    private String name;
    private Gender gender;

    public Date getBirthday() {
      return birthday;
    }

    public void setBirthday(Date birthday) {
      this.birthday = birthday;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Gender getGender() {
      return gender;
    }

    public void setGender(Gender gender) {
      this.gender = gender;
    }
  }

  public static enum Gender {
    MALE, FEMALE;
  }
}
