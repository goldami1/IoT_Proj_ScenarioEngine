package org.IoT_Project.Scenario_Engine.Models;

import java.sql.SQLException;
import java.util.*;

import com.google.gson.annotations.SerializedName;

import DataBase.DBHandler;

public class Product {
	
	@SerializedName("id")
	protected short id;
	@SerializedName("vendor_id")
	protected short vendor_id;
	@SerializedName("name")
	protected String name;
	@SerializedName("picURL")
	protected String picURL;
	@SerializedName("description")
	protected String description;
	@SerializedName("vendorName")
	protected String vendorName;
	@SerializedName("actionAndEventList")
	protected LinkedList<ActionEventProto> actionAndEventList;
	@SerializedName("endPoint")
	protected String endPoint;
	@SerializedName("actionState")
	protected boolean actionState;
	@SerializedName("eventState")
	protected boolean eventState;
	
	public Product()
	{
		this.actionAndEventList = new LinkedList<ActionEventProto>();
		this.id=this.vendor_id=-1;
		this.name = this.picURL = null;
		this.actionState = this.eventState = false;
	}
	
	public Product(short Product_id,
				   short Vendor_id,
				   String Vendor_name,
				   String Product_name,
				   String Product_description,
				   String Product_picURL,
				   String Product_endpoint,
				   LinkedList<ActionEventProto> Product_actionsAndEvents)
	{
		this.id = Product_id;
		this.vendor_id = Vendor_id;
		this.vendorName = Vendor_name;
		this.name = Product_name;
		this.description = Product_description;
		this.picURL = Product_picURL;
		this.endPoint = Product_endpoint;
		this.actionAndEventList = Product_actionsAndEvents;
		this.handleStates();
	}
	
	
	/************   ONLY FOR PRODUCT NEW CREATION IN DB   *************/
	public Product(Product i_product) throws Exception
	{
		this.vendor_id = i_product.getVendor_id();
		this.vendorName = i_product.getVendorName();
		this.name = i_product.getName();
		this.description = i_product.getDescription();
		this.picURL = i_product.getPicURL();
		this.endPoint = i_product.getEndPoint();
		this.actionAndEventList = new LinkedList<ActionEventProto>();
		
		DBHandler db = DBHandler.getInstance();
		boolean isUpdated = i_product.getId() > 0;
		if(!isUpdated)
			this.id = db.getProductsMaxAvailableIdx();
		else
			this.id = i_product.getId();
		
		/*
		 * adding all supported ActionAndEvents from vendor.
		 */
		for(ActionEventProto Current_aep : i_product.getActionAndEventList())
		{
			boolean isAEPUpdated = Current_aep.getId() > 0;
			if(!isAEPUpdated)
			{
				Current_aep.setId(db.getActionsProtoMaxAvailableIdx());
			}
			if(Current_aep.getIsEvent())
				this.eventState = true;
			else
				this.actionState = true;
			Current_aep.setProdId(this.id);
			this.actionAndEventList.add(Current_aep);
		}
		this.actionAndEventList.add(MailAction.mailActionProto);
		
	}
	/*****************************************************************/

	private void handleStates()
	{
		if(actionAndEventList==null)
		{
			eventState = actionState = false;
		}
		else
		{
			for(ActionEventProto Current_aep : this.actionAndEventList)
			{
				if(this.actionState && this.eventState)
					break;
				if(Current_aep.getIsEvent())
					this.eventState = true;
				else
					this.actionState = true;
			}
		}
	}

	public short getId() {
		return id;
	}

	public void setId(short id) {
		this.id = id;
	}

	public short getVendor_id() {
		return vendor_id;
	}

	public void setVendor_id(short vendor_id) {
		this.vendor_id = vendor_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPicURL() {
		return picURL;
	}

	public void setPicURL(String picURL) {
		this.picURL = picURL;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LinkedList<ActionEventProto> getActionAndEventList() {
		return actionAndEventList;
	}

	public void setActionAndEventList(LinkedList<ActionEventProto> actionAndEventList) {
		this.actionAndEventList = actionAndEventList;
	}

	public String getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

	public boolean isActionState() {
		return actionState;
	}

	public void setActionState(boolean actionState) {
		this.actionState = actionState;
	}

	public boolean isEventState() {
		return eventState;
	}

	public void setEventState(boolean ventState) {
		this.eventState = ventState;
	}
	
	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}
	
	public Product setVenName(String i_vendorName)
	{
		setVendorName(i_vendorName);
		return this;
	}
	
}
