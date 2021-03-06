package org.IoT_Project.Scenario_Engine.Service;

import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.IoT_Project.Scenario_Engine.Models.*;

import DataBase.DBHandler;
import DataBase.NDBHandler;

public class CustomerService {

	public Customer fetch(User i_user) throws Exception{
		return DBHandler.getInstance().getCustomer(i_user.getId());
	}
	
	public Customer fetch(String i_name, String i_password) throws Exception
	{
		return DBHandler.getInstance().getCustomer(i_name, i_password);
	}
	

	public List<Device> addDevice(short i_CustomerId, Device newDevice) throws Exception{
		Device deviceToAdd = new Device(newDevice);
		deviceToAdd.setCustomer_id(i_CustomerId);
		if(NDBHandler.getInstance().addDevice(deviceToAdd))
		{
			return NDBHandler.getInstance().getDevices(i_CustomerId);
		}
		else
			throw new Exception("no User");
	}

	public List<Device> removeDevice(short cust_id, short device_id) throws Exception{
		List<Device> res = null;
		res =  NDBHandler.getInstance().getDevices(cust_id);
		if(res==null)
			throw new Exception("No Devices Found. Couldnt delete device.");
		return res;
	}

	public List<Scenario> addScenario(short cust_id, Scenario scenarioToAdd) throws Exception{
		Scenario newScenario = new Scenario(scenarioToAdd);
		newScenario.setCust_id(cust_id);
		if(NDBHandler.getInstance().addScenario(newScenario))
		{
			return NDBHandler.getInstance().getScenarios(cust_id);
		}
		else
			throw new Exception("no user");
	}

	public List<Scenario> updateScenario(short cust_id, short origionalScenario_id, Scenario newScenario) throws Exception 
	{
		/*
		if(DBHandler.getInstance().updateScenario(origionalScenario_id, newScenario))
		{
			return DBHandler.getInstance().getScenarios(cust_id);
		}
		else
			throw new Exception("no User, or no Device to update");*/
		throw new Exception("Unimplemented");
		
	}

	public List<Scenario> removeScenario(short cust_id, int scenarioToRemove) throws Exception{
		/*if(DBHandler.getInstance().removeScenario(scenarioToRemove))
		{
			return DBHandler.getInstance().getScenarios(cust_id);
		}
		else
			throw new Exception("no User, or no Scenario to update");
			*/
		throw new Exception("Unimplemnted");
	}

	public List<Device> fetchDevices(short i_user) throws Exception {
			List<Device> res = null;
			res =NDBHandler.getInstance().getDevices(i_user);
			return res;
	}

	public List<Scenario> fetchScenarios(short i_user) throws Exception {
		List<Scenario> res = null;
		res = NDBHandler.getInstance().getScenarios(i_user);
		if(res==null)
			throw new Exception("No Scenarios found for user "+i_user);
		return res;
	}

	public List<Device> updateDevice(short cust_id, short dev_id, Device newDevice) throws Exception {
			//return DBHandler.getInstance().getDevices(cust_id);
		throw new Exception("Unimplemented");
	}
}
