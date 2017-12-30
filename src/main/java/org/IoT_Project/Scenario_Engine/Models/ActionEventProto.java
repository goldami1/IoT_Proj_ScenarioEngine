package org.IoT_Project.Scenario_Engine.Models;

import DataBase.DBHandler;

public class ActionEventProto {
	private short id;
	private String name;
	private String type;
	private short prodId;
	protected String productEP;
	private boolean isEvent;
	
	public ActionEventProto()
	{
		this.id = this.prodId = -1;
		this.isEvent = false;
		this.name = this.type = this.productEP = null;
	}
	
	public ActionEventProto(String name, String type, boolean isEvent, short prodId, String prodEP)
	{
		this.id = DBHandler.getInstance().getIdForActionProto();
		this.name = name;
		this.type = type;
		this.prodId = prodId;
		this.productEP = prodEP;
		this.isEvent = isEvent;
	}

	public short getId() {
		return id;
	}

	public void setId(short id) {
		this.id = id;
	}

	public short getProdId() {
		return prodId;
	}

	public void setProdId(short prodId) {
		this.prodId = prodId;
	}

	public String getProductEP() {
		return productEP;
	}

	public void setProductEP(String productEP) {
		this.productEP = productEP;
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return this.type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public void setIsEvent(boolean b) {
		this.isEvent = b;
	}

	public boolean getIsEvent() {
		return this.isEvent;
	}
}