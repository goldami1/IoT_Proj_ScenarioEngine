package org.IoT_Project.Scenario_Engine.Models;

public interface IUser {
	public User setUsrName(String i_userName) throws Exception;
	public User setPasswrd(String i_password) throws Exception;
	public short getID();
	public String getName();
	public String getEmail();
	public String getUserPicURL();
	public String getUserName();
	public String getPassword();
	public void setIsCustomer(boolean i_isCustomer);
	
	//TODO:
	//public boolean isEnduser();
}
