package com.topsoft.search.icis.ecps;

import java.util.Date;

public class NationalEntBaseInfoBean implements NationalEntBaseInfo {
	
	public NationalEntBaseInfoBean(Long id, String entName, String regNo,
			String leRep, String regOrgName, Date estDate) {
		super();
		this.id = id;
		this.entName = entName;
		this.regNo = regNo;
		this.leRep = leRep;
		this.regOrgName = regOrgName;
		this.estDate = estDate;
	}

	private Long id;
	
	private String entName;
	
	private String regNo;
	
	private String leRep;
	
	private String regOrgName;
	
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

	public String getRegOrgName() {
		return regOrgName;
	}

	public void setRegOrgName(String regOrgName) {
		this.regOrgName = regOrgName;
	}

	public Date getEstDate() {
		return estDate;
	}

	public void setEstDate(Date estDate) {
		this.estDate = estDate;
	}

}
