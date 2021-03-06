package DataBase;

import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.IoT_Project.Scenario_Engine.Models.*;
import javafx.util.Pair;

public interface IDBHandler {
	public boolean userConnectionAuth(String i_Username, String i_UserPassword) throws Exception;
	public boolean addCustomer(String i_firstName, String i_lastName, String i_userName, String i_userPassword, String i_email) throws Exception;
	public boolean addVendor(String i_vendorName, String i_userName, String i_userPassword, String i_email, String i_vendorDescription, String i_vendorLogoPic) throws Exception;
	public boolean addDevice(Device i_dev) throws Exception;	
	public Vendor addVendor(Vendor i_vendor) throws Exception;
	public boolean addScenario(Scenario i_Scenario) throws Exception;
	public boolean addVendor(String i_vendorName, String i_userName, String i_userPassword, String i_email, String i_vendorDescription, BufferedImage i_logo) throws Exception;
	public boolean addProduct(Product i_product) throws Exception;
	public LinkedList<Pair<Short,String>> getVendors() throws Exception; //pair(id,name)
	public LinkedList<Product> getProductsByProdID(int i_prod_id) throws Exception;
	public List<Product> getProducts(int vendor_id) throws Exception;
	public void addVndor(Vendor i_vendor) throws Exception;
	public User getUser(String i_username, String i_userPassword) throws Exception;
	public Vendor getVendor(String i_username, String i_password) throws Exception;
	public Vendor getVendor(int vendor_id) throws Exception;
	public LinkedList<Scenario> getScenarios(int cust_id) throws Exception;
	public Customer addCustomer(Customer i_User) throws Exception;
	public void addAction(Action action) throws Exception;
	public Customer getCustomer(String i_username, String i_password) throws Exception;
	public Customer getCustomer(int cust_id) throws Exception;
	public short addEvent(Event event) throws Exception;
	public Event getEvent(int event_id) throws Exception;
	public LinkedList<Device> getDevices(short i_UserID) throws Exception;
	//TODO:
	//2. Make Exception classes for login/signin/
	//public LinkedList<String> selectQ(String i_selectQ) throws SQLException;
	//public boolean addDevice(int product_id,int customer_id,int device_serial) throws Exception ;
	//public boolean removeDevice(int device_id);
	//public boolean removeProduct(int productToRemove_id);
	//public boolean updateProduct(int prod_id, Product new_product);
	//public boolean updateDevice(int origionalDevice_id, Device newDevice);
	//public boolean updateScenario(int origionalScenario_id, Scenario newScenario);
	//public boolean removeScenario(int scenarioToRemove_id);
	//void removeProduct(Product productToRemove);
}