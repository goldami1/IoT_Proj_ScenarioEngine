package org.IoT_Project.Scenario_Engine.Models;

import java.sql.SQLException;

import DataBase.DBHandler;

public class User implements IUser 
{

	protected short id;
	protected String name, userPicURL, email;
	protected String userName, password;
	protected boolean isCustomer;		//true - Customer, False - vendor
	
	public User(String userName, String password, String i_name, String i_email, String i_userPicURL) 
	{
		this.userName = userName;
		this.password = password;
		this.name = i_name;
		this.email = i_email;
		this.userPicURL = i_userPicURL;
		this.id = -1;
	}
	
	//Constructors
	public User()
	{
		this.id = -1;
		this.name = this.userPicURL = this.email = this.userName = this.password = null;
		this.isCustomer = false;
	}
	
	public User(String i_uname, String i_password) throws SQLException
	{
		 User usr = (User)DBHandler.getInstance().getUser(i_uname, i_password);
		 this.userName = usr.getUsername();
		 this.name = usr.getName();
		 this.password = usr.getPassword();
		 this.id = usr.getID();
		 this.email = usr.getEmail();
		 this.userPicURL = usr.getUserPicURL();
		 
	}
	
	public User(User i_user) {
		this.id = i_user.getID();
		this.name = i_user.getName();
		this.userPicURL = i_user.getUserPicURL();
		this.userName = i_user.getUsername();
		this.password = i_user.getPassword();
	}

	//Getters&Setters
	public short getID()
	{
		return id;
	}
	public String getName()
	{
		return name;
	}
	public String getUsername()
	{
		return userName;
	}
	public String getPassword()
	{
		return password;
	}
	public String getEmail()
	{
		return email;
	}
	public String getUserPicURL()
	{
		return userPicURL;
	}
	public void setIsCustomer(boolean isCustomer)
	{
		this.isCustomer = isCustomer;
	}
	public boolean getIsCustomer()
	{
		return this.isCustomer;
	}
	//Methods
	public User setUserName(String i_userName) throws Exception 
	{
		boolean isTaken = true;
		try 
		{
			isTaken = DBHandler.getInstance().isUsernameAvailable(i_userName);
		} catch (Exception e)
		{
			throw new Exception("There's a problem with setUserName in User", e);
		}

		if (!isTaken) 
		{
			userName = i_userName;
		}

		return this;
	}
	public User setPassword(String i_password) throws Exception
	{
		boolean isExists = false;
		if (userName != null)
		{
			try
			{
			isExists = DBHandler.getInstance().isUsernameAvailable(userName);
			}
			catch(Exception e)
			{
				throw new Exception("There's a problem with setPassword in User", e);
			}
			
			if (isExists) 
			{
				password = i_password;
			}
		}
		return this;
	}

}
