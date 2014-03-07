package com.topsoft.search.icis.ecps;

import java.util.Date;

public class TopEntBaseInfoBean implements TopEntBaseInfo {

	private Long id;
	
	private String entName;

	private String regNo;
	
	private String leRep;
	
	private String opLocOrDom;
	
	private Float regCap;
	
	private String regCapCurName;
	
	private String industryPhyName;
	
	private Date estDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEntName() {
		return entName;
	}

	public void setEntName(String entName) {
		this.entName = entName;
	}

	public String getRegNo() {
		return regNo;
	}

	public void setRegNo(String regNo) {
		this.regNo = regNo;
	}

	public String getLeRep() {
		return leRep;
	}

	public void setLeRep(String leRep) {
		this.leRep = leRep;
	}

	public String getOpLocOrDom() {
		return opLocOrDom;
	}

	public void setOpLocOrDom(String opLocOrDom) {
		this.opLocOrDom = opLocOrDom;
	}

	public Float getRegCap() {
		return regCap;
	}

	public void setRegCap(Float regCap) {
		this.regCap = regCap;
	}

	public String getRegCapCurName() {
		return regCapCurName;
	}

	public void setRegCapCurName(String regCapCurName) {
		this.regCapCurName = regCapCurName;
	}

	public String getIndustryPhyName() {
		return industryPhyName;
	}

	public void setIndustryPhyName(String industryPhyName) {
		this.industryPhyName = industryPhyName;
	}

	public Date getEstDate() {
		return estDate;
	}

	public void setEstDate(Date estDate) {
		this.estDate = estDate;
	}

	public TopEntBaseInfoBean(Long id, String entName, String regNo,
			String leRep, String opLocOrDom, Float regCap,
			String regCapCurName, String industryPhyName, Date estDate) {
		super();
		this.id = id;
		this.entName = entName;
		this.regNo = regNo;
		this.leRep = leRep;
		this.opLocOrDom = opLocOrDom;
		this.regCap = regCap;
		this.regCapCurName = regCapCurName;
		this.industryPhyName = industryPhyName;
		this.estDate = estDate;
	}

}
