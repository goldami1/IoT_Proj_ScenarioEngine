package DataBase;

import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.IoT_Project.Scenario_Engine.Models.Action;
import org.IoT_Project.Scenario_Engine.Models.ActionEventProto;
import org.IoT_Project.Scenario_Engine.Models.Customer;
import org.IoT_Project.Scenario_Engine.Models.Device;
import org.IoT_Project.Scenario_Engine.Models.Event;
import org.IoT_Project.Scenario_Engine.Models.Product;
import org.IoT_Project.Scenario_Engine.Models.Scenario;
import org.IoT_Project.Scenario_Engine.Models.User;
import org.IoT_Project.Scenario_Engine.Models.Vendor;
import org.IoT_Project.Scenario_Engine.Models.Event.ElogicOperand;

import javafx.util.Pair;
import utils.Constants;
import utils.serverImgManager;

public class DBHandler implements IDBHandler 
{	
	private static DBHandler instance;
	private static Connection currSession;
	private final static String k_action = "ACTIONS";
	private final static String k_event = "EVENTS";
	private final static String k_driver = "com.mysql.jdbc.Driver";
	private final static String k_selectEvent="select * from EVENTS where event_id=?;";
	private final static String k_selectEventProto="select * from EVENTS_PROTO where eventproto_id=?;";
	private final static String k_userConnectionAuth = "SELECT user_name, user_password FROM CUSTOMERS WHERE user_name=? and user_password=? UNION SELECT user_name, user_password FROM VENDORS WHERE user_name=? and user_password=?;";
	private final static String k_doesCustomerExistByName = "SELECT user_name FROM CUSTOMERS WHERE user_name=?;";
	private final static String k_doesCustomerExistByID = "SELECT customer_id FROM CUSTOMERS WHERE customer_id=?;";
	private final static String k_doesVendorExistByName = "SELECT user_name FROM VENDORS WHERE user_name=?;";
	private final static String k_doesVendorExistByID= "SELECT vendor_id FROM VENDORS WHERE vendor_id=?;";
	private final static String k_selectMaxID = "SELECT MAX(%s) FROM %s;";
	private final static String k_selectEventProtoParams =	"SELECT event_name, event_description, productEP, param_id, EVENTS_PROTO.eventproto_id, param_name, param_desc, param_type, param_min, param_max " + 
														"FROM EVENTS_PROTO INNER JOIN AE_PROTO_PARAMS ON EVENTS_PROTO.eventproto_id=AE_PROTO_PARAMS.eventproto_id " + 
														"WHERE EVENTS_PROTO.eventproto_id IN (SELECT DISTINCT eventproto_id FROM EVENTS_PROTO where product_id=?) " + 
														"ORDER BY AE_PROTO_PARAMS.eventproto_id, param_id;";
	private final static String k_selectActionProtoParams =	"SELECT action_name, action_description, productEP, param_id, ACTIONS_PROTO.actionproto_id, param_name, param_desc, param_type, param_min, param_max " + 
															"FROM ACTIONS_PROTO INNER JOIN AE_PROTO_PARAMS ON ACTIONS_PROTO.actionproto_id=AE_PROTO_PARAMS.actionproto_id " + 
															"WHERE ACTIONS_PROTO.actionproto_id IN (SELECT DISTINCT actionproto_id FROM ACTION_PROTO where product_id=?) " + 
															"ORDER BY AE_PROTO_PARAMS.eventproto_id, param_id;";
	
	
	private short[] TABLE_maxID;
	private enum EntityAndIdxValue
	{		
			CUSTOMERS_TABLE(1, "CUSTOMERS", "CUSTOMERS.customer_id"),
			VENDORS_TABLE(2, "VENDORS", "VENDORS.vendor_id"),
			ACTIONS_PROTO_TABLE(3, "ACTIONS_PROTO", "ACTIONS_PROTO.actionproto_id"),
			ACTIONS_TABLE(4, "ACTIONS", "ACTIONS.action_id"),
			EVENTS_PROTO_TABLE(5, "EVENTS_PROTO", "EVENTS_PROTO.eventproto_id"),
			EVENTS_TABLE(6, "EVENTS", "EVENTS.event_id"),
			PRODUCTS_TABLE(7, "PRODUCTS", "PRODUCTS.product_id"),
			DEVICES_TABLE(8, "DEVICES", "DEVICES.device_id"),
			SCENARIOS_TABLE(9, "SCENARIOS", "SCENARIOS.scenario_id");
			
			private final int idx;
			private final String Entity;
			private final String idxValColumn;
			
			private EntityAndIdxValue(int i_idx, String i_entity, String i_idxValColumn)
			{
				idx=i_idx;
				Entity = i_entity;
				idxValColumn= i_idxValColumn;
			}
	}

	
	protected DBHandler() throws SQLException
	{
		instance = null;
		currSession=null;
		TABLE_maxID = new short[EntityAndIdxValue.values().length+1];
		initializeAllMaxIds();
		serverImgManager.getInstance();
		
	}
	
	public static DBHandler getInstance() throws SQLException
	{
		if(instance == null)
		{
			instance = new DBHandler();
		}
		return instance;
	}
	
	//===================================
	//<begin> DB connection METHODS <begin>
	@SuppressWarnings("finally")
	private static Connection openConnection() throws SQLException
	{
		Connection conn = null;
		try
		{
			String url = Constants.getDatabaseConnectionURL();
			String username = Constants.getDatabaseUserName();
			String password = Constants.getDatabasePassword();	
			Class.forName(k_driver);
				
			conn = DriverManager.getConnection(url, username, password);
			
			currSession = conn;
		} catch (ClassNotFoundException e) {
			System.out.println("Class Loader Exception!");
		}
		finally
		{
			return conn;
		}
	}
	
	private static void closeConnection() throws SQLException
	{
		if(currSession!=null)
		{
			currSession.close();
		}
	}
	
	@SuppressWarnings("finally")
	public boolean userConnectionAuth(String i_userName, String i_password) throws SQLException
	{
		String sqlQuery = k_userConnectionAuth;
		boolean flag = true;
		
		try 
		{
			Connection connection = openConnection();
			java.sql.PreparedStatement connectionStat  =connection.prepareStatement(sqlQuery);
			connectionStat.setString(1, i_userName);
			connectionStat.setString(2, i_password);
			connectionStat.setString(3, i_userName);
			connectionStat.setString(4, i_password);
			ResultSet connectionRes = connectionStat.executeQuery();
			
			if (!connectionRes.next()) 
			{
				throw new SQLException("User info not found in DB exception!");
			}
			if (!connectionRes.getString(2).equals(i_password)) 
			{
				flag= false;
				throw new SQLException("The password incorrect exception!");
			}
		}
		finally
		{
			closeConnection();
			return flag;
		}
	}
	//<end> DB connection METHODS <end>
	//===================================

	//===================================
	//<begin> ADD C.R.U.D METHODS <begin>
 
	/*
	public boolean addCustomer(String i_firstName, String i_lastName, String i_userName, String i_userPassword, String i_email, BufferedImage i_pic) throws SQLException 
	{
		String path = serverImgManager.getInstance().createNewVendor(i_email, i_pic);
		boolean res = addCustomer(i_firstName, i_lastName, i_userName, i_userPassword, i_email, path);
		
		return res;
	}*/
	
	@SuppressWarnings("finally")
	public boolean addCustomer(String i_firstName, String i_lastName, String i_userName, String i_userPassword, String i_email) throws SQLException 
	{
		final String sqlQuery =	"insert into CUSTOMERS (first_name, last_name, user_name, user_password, email)"
								+ "values ("
								+"'"+i_firstName+"',"
								+"'"+i_lastName+"',"
								+"'"+i_userName+"',"
								+"'"+i_userPassword+"',"
								+"'"+i_email+"'"
								+")";
		
		boolean flag = false;
		
		try 
		{
			Connection connection = openConnection();
			java.sql.PreparedStatement insertStat  = connection.prepareStatement(sqlQuery);
			insertStat.executeUpdate();
			flag = true;
		}
		finally
		{
			closeConnection();
			incrementMaxIdxValue(EntityAndIdxValue.CUSTOMERS_TABLE);
			System.out.println("Customer Added Successfully");
			return flag;
		}
	}
	
	public boolean addVendor(String i_vendorName, String i_userName, String i_userPassword, String i_email, String i_vendorDescription, BufferedImage i_logo) throws SQLException 
	{
		String path = serverImgManager.getInstance().createNewVendor(i_vendorName, i_logo);
		boolean res = addVendor(i_vendorName, i_userName, i_userPassword, i_email, i_vendorDescription, path);
		
		return res;
	}
	
	@SuppressWarnings("finally")
	public boolean addVendor(String i_vendorName, String i_userName, String i_userPassword, String i_email, String i_vendorDescription, String i_vendorLogoPic) throws SQLException 
	{
		final String sqlQuery =	"insert into VENDORS (vendor_name, user_name, user_password, email, vendor_description, vendor_logo_pic)"
				+ "values ("
				+"'"+i_vendorName+"',"
				+"'"+i_userName+"',"
				+"'"+i_userPassword+"',"
				+"'"+i_email+"',"
				+"'"+i_vendorDescription+"',"
				+"'"+i_vendorLogoPic+"'"
				+")";

		boolean flag = false;

		try 
		{
				Connection connection = openConnection();
				java.sql.PreparedStatement insertStat  = connection.prepareStatement(sqlQuery);
				insertStat.executeUpdate();
				flag = true;
		}
		catch (SQLException e) {System.out.println(e);}
		finally
		{
			closeConnection();
			incrementMaxIdxValue(EntityAndIdxValue.VENDORS_TABLE);
			return flag;
		}
	}
	//<end> ADD C.R.U.D METHODS <end>
	//===================================
	
  
	//===================================
	//<begin> get C.R.U.D METHODS <begin>
	
	public LinkedList<String> selectQ(String i_selectQ) throws SQLException
	{
		Connection connection = openConnection();
		List<Row> table = new ArrayList<Row>();
		LinkedList<String> res = new LinkedList<String>();
		StringBuilder rowBuilder = new StringBuilder();
		PreparedStatement st = connection.prepareStatement(i_selectQ);
		ResultSet rs = st.executeQuery();

		Row.formTable(rs, table);

		for (Row row : table)
		{
		    for (Entry<Object, Class> col: row.row)
		    {
		        rowBuilder.append((" > " + ((col.getValue()).cast(col.getKey()))));
		    }
		    res.add(rowBuilder.toString());
		    rowBuilder = new StringBuilder();
		}
		
		return res;
	}
	//<end> get C.R.U.D METHODS <end>
	//===================================
	
  
	//===================================
	//<begin> static find C.R.U.D METHODS <begin>
	
	public boolean isUsernameAvailable(String i_userName) throws Exception
	{
		return !(doesCustVendExist((short) -1, i_userName, k_doesCustomerExistByName)) || !(doesCustVendExist((short) -1, i_userName, k_doesVendorExistByName));
	}
	//<end> static find C.R.U.D METHODS <end>
	//===================================

	public boolean addDevice(Device i_dev) throws SQLException {
		return addDevice(i_dev.getProtoDevice().getId(), i_dev.getCustomer_id(), i_dev.getSerial_number());
	}

	@SuppressWarnings("finally")
	public boolean addScenario(Scenario i_Scenario) throws Exception 
	{
		if(!doesCustVendExist(i_Scenario.getCust_id(), null, k_doesCustomerExistByID))
		{
			System.out.println("No such customer!");
			return false;
		}
	
		addActions(i_Scenario);
		addEvents(i_Scenario);
		addlogExpr(i_Scenario);
		
		final String sqlQuery =	"insert into SCENARIOS (scenario_id, customer_id, scenario_name, scenario_description)"
				+ "values ("
				+"'"+i_Scenario.getId()+"',"
				+"'"+i_Scenario.getCust_id()+"',"
				+"'"+i_Scenario.getName()+"',"
				+"'"+i_Scenario.getDescription()+"',"
				+")";

		boolean flag = false;

		try 
		{
				Connection connection = openConnection();
				java.sql.PreparedStatement insertStat  = connection.prepareStatement(sqlQuery);
				insertStat.executeUpdate();
				flag = true;
				incrementMaxIdxValue(EntityAndIdxValue.SCENARIOS_TABLE);
		}
		finally
		{
			closeConnection();
			return flag;
		}
	}

	
	private void addlogExpr(Scenario i_Scenario) throws SQLException
	{
		Iterator<Entry<Short,Event>> itr = i_Scenario.getEventsToHappen().entrySet().iterator();
		boolean isNeedToCloseConn=false;
		
		try
		{
			Connection connection = currSession;
			if(currSession==null)
			{
				connection = openConnection();
				isNeedToCloseConn = true;
			}
			short i=1;
			while(itr.hasNext())
			{
				final String sqlQuery =	"insert into LOGEXPRS (scenario_id, loc_id, event_id)"
						+ "values ("
						+"'"+i_Scenario.getId()+"',"
						+"'"+Short.toString(i++)+"',"
						+"'"+itr.next().getValue().getId()+"',"
						+")";
				
						java.sql.PreparedStatement insertStat  = connection.prepareStatement(sqlQuery);
						insertStat.executeUpdate();
			}
		}
		finally
		{
			if(isNeedToCloseConn)
				closeConnection();
		}
	}
	
	private void insertAEParamsValues(short i_ActionEvent_id ,List<String> i_ParamValues, String i_KActionEvent) throws SQLException
	{
		short param_idx=0;
		String ae = null;
		if(i_KActionEvent.compareTo(k_action)==0)
			ae="action_id";
		else
			ae="event_id";

		Connection connection = openConnection();
		for(String paramVal : i_ParamValues)
		{
			
			final String sqlQuery = "insert into " + i_KActionEvent +"_PARAM_VALUES ("+ae+", param_id, param_val)"
					+ "values ("
					+"'"+i_ActionEvent_id+"',"
					+"'"+(param_idx++)+"',"
					+"'"+paramVal+"'"
					+");";

			java.sql.PreparedStatement insertStat  = connection.prepareStatement(sqlQuery);
			insertStat.executeUpdate();
		}
	}
	
	private void addActions(Scenario i_Scenario) throws Exception 
	{
		Iterator<Action> itr = i_Scenario.getActions().iterator();
		try
		{
			Connection connection = openConnection();
			while(itr.hasNext())
			{
				Action currentAction = itr.next();
				if(currentAction.getDevice_serialNum()<0 || currentAction.getActionDescription().getProdId()<0)
					throw new Exception("Incorrect Action object - no matching Product, or no Action instance available!");
			
			
				insertAEParamsValues(currentAction.getId() ,currentAction.getParameters(), k_action);
				
				final String sqlQuery = "insert into ACTIONS (device_id, actionproto_id) "
						+ "values ("
						+"'"+currentAction.getDevice_serialNum()+"',"
						+"'"+currentAction.getActionDescription().getId()+"'"
						+");";

				java.sql.PreparedStatement insertStat  = connection.prepareStatement(sqlQuery);
				insertStat.executeUpdate();
				incrementMaxIdxValue(EntityAndIdxValue.ACTIONS_TABLE);
			
				itr.next();
			}
		}
		finally
		{
			closeConnection();
		}
 
	}
	
	
	private void addEvents(Scenario i_Scenario) throws Exception 
	{
		Iterator<Entry<Short, Event>> itr = i_Scenario.getEventsToHappen().entrySet().iterator();
		boolean isNeedToCloseConn=false;
		
		try
		{
			Connection connection = currSession;
			if(currSession==null)
			{
				connection = openConnection();
				isNeedToCloseConn = true;
			}
			while(itr.hasNext())
			{
				Event currentEvent = itr.next().getValue();
				if(currentEvent.getDevice_serialNum()<0 || currentEvent.getActionDescription().getProdId()<0)
					throw new Exception("Incorrect Event object - no matching Product, or no Event instance available!");
			
				insertAEParamsValues(currentEvent.getId(), currentEvent.getParameters(), k_event);
				
				final String sqlQuery = "insert into EVENTS (device_id, eventproto_id, logicOper) "
						+ "values ("
						+"'"+currentEvent.getDevice_serialNum()+"',"
						+"'"+currentEvent.getActionDescription().getId()+"',"
						+"'"+currentEvent.getLogicOperator()+"'"
						+");";

				java.sql.PreparedStatement insertStat  = connection.prepareStatement(sqlQuery);
				insertStat.executeUpdate();
				incrementMaxIdxValue(EntityAndIdxValue.EVENTS_TABLE);
			
				itr.next();
			}
		}
		finally
		{
			if(isNeedToCloseConn)
				closeConnection();
		}
 
	}
	

	@SuppressWarnings("finally")
	private boolean doesCustVendExist(short i_custVendID, String i_userName, String i_CustVendQuery) throws Exception
	{
		boolean isNeedToCloseConn=false;
		if(i_userName!=null && i_custVendID>0)
			throw new Exception("wrong use of method doesCustVendExist - choose either username, either userID");
		
		Connection connection=null;
		try
		{
			connection = currSession;
			if(currSession==null)
			{
				connection = openConnection();
				isNeedToCloseConn = true;
			}
			PreparedStatement queryingStatement = connection.prepareStatement(i_CustVendQuery);
			if(i_userName!=null)
				queryingStatement.setString(1,  i_userName);
			else
				queryingStatement.setString(1, Short.toString(i_custVendID));
			ResultSet queryResult = queryingStatement.executeQuery();
			if (!queryResult.next())
			{
				return false;
			}
			else
				return true;
		} 
		finally
		{
			if(connection!=null || isNeedToCloseConn)
				closeConnection();
			return false;
		}
	}

	@SuppressWarnings("finally")
	public LinkedList<Pair<Short, String>> getVendors() throws SQLException //final&complete IMPL
	{
		LinkedList<Pair<Short,String>> res = null;
		String vendor_name=null;
		short vendor_id=-1;
		
		try
		{
			Connection connection = openConnection();
			PreparedStatement queryStatement;
			ResultSet queryResult;
			
			queryStatement  =connection.prepareStatement("select * from VENDORS;");
			queryResult = queryStatement.executeQuery();
				
			while (queryResult.next())
			{
				if(res==null)
					res = new LinkedList<Pair<Short,String>>();
				
				vendor_id = Short.parseShort(queryResult.getString(1));
				vendor_name = queryResult.getString(2);
				
				res.add(new Pair(vendor_id, vendor_name));
			}
		}	
		finally
		{
			closeConnection();
			if(res==null)
				throw new SQLException("No registered vendors found!");
			return res;
		}
	}
	
	private LinkedList<LinkedList<String>> getAESupportedValues(int i_ActionEventId, String i_ActionEvent) throws SQLException
	{
		LinkedList<LinkedList<String>> res = null;
		LinkedList<String> event_opts=null;
		String sqlQuery = null;
		short tempParamId=-1;
		if(i_ActionEvent.compareTo(k_action)==0)
			sqlQuery = "select * from PARAM_OPTIONS where actionproto_id=? order by param_id;";
		else
			sqlQuery = "select * from PARAM_OPTIONS where eventproto_id=? order by param_id;";
		
		Connection connection = openConnection();
		PreparedStatement qStatement;
		ResultSet qResult;
		
		qStatement = connection.prepareStatement(sqlQuery);
		qStatement.setString(1, Integer.toString(i_ActionEventId));
		qResult = qStatement.executeQuery();
		
		event_opts = new LinkedList<String>();
		while(qResult.next())
		{
			if(res==null)
				res = new LinkedList<LinkedList<String>>();
			
			if(tempParamId != -1 && tempParamId != Short.parseShort(qResult.getString(3)))
			{					
				res.add(event_opts);
				event_opts = new LinkedList<String>();
			}
			
			tempParamId = Short.parseShort(qResult.getString(3));
			event_opts.add(qResult.getString(4));
		}
		
		return res;
	}
	
	private LinkedList<ActionEventProto> getListOfAEProtosByProdID(short i_prod_id, String i_ActionEvent) throws NumberFormatException, SQLException
	{
		LinkedList<ActionEventProto> res=null;
		Connection connection = openConnection();
		PreparedStatement qStatement;
		ResultSet qsResult;
		String sqlQuery=null;
		
		if(i_ActionEvent.compareTo(k_action)==0)
			sqlQuery = k_selectActionProtoParams;
		else
			sqlQuery = k_selectEventProtoParams;
		
		qStatement = connection.prepareStatement(sqlQuery);
		qStatement.setString(1, Integer.toString(i_prod_id));
		qsResult = qStatement.executeQuery();
		short tempAENum=-1;
		LinkedList<String> buf_Types = new LinkedList<String>();
		LinkedList<String> buf_SupportedParamsName= new LinkedList<String>();
		LinkedList<String> buf_min = new LinkedList<String>();
		LinkedList<String> buf_max = new LinkedList<String>();
		
		while(qsResult.next())
		{
			if(res==null)
				res = new LinkedList<ActionEventProto>();
			if(tempAENum != -1 && tempAENum != Integer.parseInt(qsResult.getString(5)))
			{					
				res.add(new ActionEventProto(tempAENum,
						qsResult.getString(1),
						qsResult.getString(2),
						buf_Types,
						getAESupportedValues(tempAENum, i_ActionEvent),
						buf_SupportedParamsName,
						buf_min,
						buf_max,
						i_prod_id,
						qsResult.getString(3),
						true));
				
				buf_Types = new LinkedList<String>();
				buf_SupportedParamsName= new LinkedList<String>();
				buf_min = new LinkedList<String>();
				buf_max = new LinkedList<String>();
			}
			tempAENum = Short.parseShort(qsResult.getString(5));
			buf_SupportedParamsName.add(qsResult.getString(6));
			buf_Types.add(qsResult.getString(8));
			buf_min.add(qsResult.getString(9));
			buf_max.add(qsResult.getString(10));
		}
		
		return res;
	}
	/*
	private LinkedList<ActionEventProto> getListOfEventProtosByProdID(short i_prod_id) throws NumberFormatException, SQLException
	{
		LinkedList<ActionEventProto> res=null;
		Connection connection = openConnection();
		PreparedStatement qStatement;
		ResultSet qsResult;
		qStatement = connection.prepareStatement(k_selectEventProtoParams);
		qStatement.setString(1, Integer.toString(i_prod_id));
		qsResult = qStatement.executeQuery();
		short tempEveNum=-1;
		LinkedList<String> buf_Types = new LinkedList<String>();
		LinkedList<String> buf_SupportedParamsName= new LinkedList<String>();
		LinkedList<String> buf_min = new LinkedList<String>();
		LinkedList<String> buf_max = new LinkedList<String>();
		
		while(qsResult.next())
		{
			if(res==null)
				res = new LinkedList<ActionEventProto>();
			if(tempEveNum != -1 && tempEveNum != Integer.parseInt(qsResult.getString(5)))
			{					
				res.add(new ActionEventProto(tempEveNum,
						qsResult.getString(1),
						qsResult.getString(2),
						buf_Types,
						getAESupportedValues(tempEveNum, k_event),
						buf_SupportedParamsName,
						buf_min,
						buf_max,
						i_prod_id,
						qsResult.getString(3),
						true));
				
				buf_Types = new LinkedList<String>();
				buf_SupportedParamsName= new LinkedList<String>();
				buf_min = new LinkedList<String>();
				buf_max = new LinkedList<String>();
			}
			tempEveNum = Short.parseShort(qsResult.getString(5));
			buf_SupportedParamsName.add(qsResult.getString(6));
			buf_Types.add(qsResult.getString(8));
			buf_min.add(qsResult.getString(9));
			buf_max.add(qsResult.getString(10));
		}
		
		return res;
	}*/

	@SuppressWarnings("finally")
	public LinkedList<Product> getProducts(int vendor_id) throws SQLException//final&complete IMPL
	{
		String prod_name=null, prod_pic=null, prod_desc=null, prodEP=null, ven_name=null;
		short prod_id=-1;
		Product currProd=null;
		LinkedList<Product> res = null;
		LinkedList<ActionEventProto> currProd_aep_list=null;
		
		try
		{
			Connection connection = openConnection();
			PreparedStatement queryStatement, venNameQStatement;
			ResultSet queryResult, venNameQResult;
			
			venNameQStatement  =connection.prepareStatement("select vendor_name from VENDORS where vendor_id=?;");
			venNameQStatement.setString(1, Integer.toString(vendor_id));
			venNameQResult = venNameQStatement.executeQuery();
			if(venNameQResult.next())
				ven_name = venNameQResult.getString(1);
			else
				throw new SQLException("No such vendor found for getProducts");
			
			queryStatement  =connection.prepareStatement("select * from PRODUCTS where vendor_id=?;");
			queryStatement.setString(1, Integer.toString(vendor_id));
			queryResult = queryStatement.executeQuery();
				
			while (queryResult.next())
			{
				if(res==null)
					res = new LinkedList<Product>();
				
				prod_id = Short.parseShort(queryResult.getString(1));
				prod_name = queryResult.getString(3);
				prod_pic = queryResult.getString(4);
				prod_desc = queryResult.getString(7);
				prodEP = queryResult.getString(8);
				
				currProd_aep_list = getListOfAEProtosByProdID(prod_id, k_action);
				currProd_aep_list.addAll(getListOfAEProtosByProdID(prod_id, k_event));
				
				currProd = new Product(prod_id, (short)vendor_id, ven_name, prod_name, prod_desc, prod_pic, prodEP, currProd_aep_list);
					
				res.add(currProd);
			}
		}	
		finally
		{
			closeConnection();
			if(res==null)
				throw new SQLException("No products added to provided vendor id");
			return res;
		}
	}
	
	
	@SuppressWarnings("finally")
	public LinkedList<Product> getProductsByProdID(int i_prod_id) throws SQLException//final&complete IMPL
	{
		String prod_name=null, prod_pic=null, prod_desc=null, prodEP=null, ven_name=null;
		short prod_id=-1, vendor_id=-1;
		Product currProd=null;
		LinkedList<Product> res = null;
		LinkedList<ActionEventProto> currProd_aep_list=null;
		
		try
		{
			Connection connection = openConnection();
			PreparedStatement queryStatement, venNameQStatement;
			ResultSet queryResult, venNameQResult;
			
			venNameQStatement  =connection.prepareStatement("select vendor_name from VENDORS inner join PRODUCTS on VENDORS.vendor_id=PRODUCTS.vendor_id where product_id=?;");
			venNameQStatement.setString(1, Integer.toString(i_prod_id));
			venNameQResult = venNameQStatement.executeQuery();
			if(venNameQResult.next())
				ven_name = venNameQResult.getString(1);
			else
				throw new SQLException("No such vendor found for getProducts");
			
			queryStatement  =connection.prepareStatement("select * from PRODUCTS where product_id=?;");
			queryStatement.setString(1, Integer.toString(i_prod_id));
			queryResult = queryStatement.executeQuery();
				
			while (queryResult.next())
			{
				if(res==null)
					res = new LinkedList<Product>();
				
				prod_id = Short.parseShort(queryResult.getString(1));
				vendor_id = Short.parseShort(queryResult.getString(2));
				prod_name = queryResult.getString(3);
				prod_pic = queryResult.getString(4);
				prod_desc = queryResult.getString(7);
				prodEP = queryResult.getString(8);
				
				currProd_aep_list = getListOfAEProtosByProdID(prod_id, k_action);
				currProd_aep_list.addAll(getListOfAEProtosByProdID(prod_id, k_event));
				
				currProd = new Product(prod_id, vendor_id, ven_name, prod_name, prod_desc, prod_pic, prodEP, currProd_aep_list);
					
				res.add(currProd);
			}
		}	
		finally
		{
			closeConnection();
			if(res==null)
				throw new SQLException("No products added to provided vendor id");
			return res;
		}
	}
	
	
	@SuppressWarnings("finally")
	private String getVenNameByID(int i_ven_id) throws SQLException
	{
		String res = null;
		boolean isNeedToCloseConn=false;
		
		try
		{
			Connection connection = currSession;
			if(currSession==null)
			{
				connection = openConnection();
				isNeedToCloseConn = true;
			}
			PreparedStatement queryStatement;
			ResultSet queryResult;
			
			queryStatement  =connection.prepareStatement("select * from VENDORS where vendor_id=?;");
			queryStatement.setString(1, Integer.toString(i_ven_id));
			queryResult = queryStatement.executeQuery();
				
			if(queryResult.next())
			{
				res = queryResult.getString(2);
			}
		}	
		finally
		{
			if(isNeedToCloseConn)
				closeConnection();
			if(res==null)
				throw new SQLException("DB isn't normalized, vendor name couldn't be found by vendor id!");
			return res;
		}
		
	}

	//****************	CHANGED UNCERTAIN	*********************\\
	@SuppressWarnings("finally")
	public LinkedList<Device> getDevices(short user_id) throws SQLException	//final&complete IMPL
	{
		short serial_num=-1, dev_id=-1, prod_id=-1, cust_id=-1, ven_id=-1;
		Product prod= null;
		
		
		LinkedList<Device> res = null;
		
		try
		{
			Connection connection = openConnection();
			PreparedStatement devQStatement, prodQStatement;
			ResultSet devsQResult, prodsQResult;
			
			devQStatement  =connection.prepareStatement("select * from DEVICES where customer_id=?;");
			devQStatement.setString(1, Integer.toString(user_id));
			devsQResult = devQStatement.executeQuery();
				
			while (devsQResult.next())
			{
				if(connection==null)
					connection = openConnection();
				if(res==null)
					res = new LinkedList<Device>();
				
				dev_id = Short.parseShort(devsQResult.getString(1));
				prod_id = Short.parseShort(devsQResult.getString(2));
				cust_id = Short.parseShort(devsQResult.getString(3));
				serial_num = Short.parseShort(devsQResult.getString(4));
				
				
				prod = getProductsByProdID((int)prod_id).getFirst();
				ven_id = prod.getVendor_id();

				//prodQStatement  =connection.prepareStatement("select * from PRODUCTS where product_id=?;");
				//prodQStatement.setString(1, Integer.toString(prod_id));
				//prodsQResult = prodQStatement.executeQuery();
					
					
				//if (prodsQResult.next())
				//{
					//short vendor_id = Short.parseShort(prodsQResult.getString(2));
					//String product_name = prodsQResult.getString(3);
					//String product_pic = prodsQResult.getString(4);
					//prod = new Product(product_name, product_pic, null, null);
					//prod = new Product(prod, vendor_id, prod_id);
					//String prod_description = null;
					/*
					 * need to add product_description for each product
					 */
					//prod = getProductsByProdID((int)prod_id).getFirst();
					//TODO
					/*
					 * NOTES:
					 * 1. added description for product.
					 * 2. the nulls in the c'tor(line 489) are the endpoint(for the device) and the supported Action/Event list.
					 */
				//}
				//else
				//	throw new SQLException("relevant product for device couldn't be found! DB isn't normalized!");
					
				Device devToAdd =new Device(dev_id, serial_num, cust_id, prod.setVenName(getVenNameByID(ven_id)));
				res.add(devToAdd);
			}
		}	
		finally
		{
			closeConnection();
			return res;
		}
	}

	@SuppressWarnings("finally")
	public boolean addDevice(int product_id, int customer_id, int device_serial) throws SQLException //final&complete IMPL
	{ 
		final String sqlQuery =	"insert into DEVICES (product_id, customer_id , serial_num) "
				+ "values ("
				+"'"+product_id+"',"
				+"'"+customer_id+"',"
				+"'"+device_serial+"'"
				+");";

		boolean flag = false;

		try 
		{
				Connection connection = openConnection();
				java.sql.PreparedStatement insertStat  = connection.prepareStatement(sqlQuery);
				insertStat.executeUpdate();
				flag = true;
				incrementMaxIdxValue(EntityAndIdxValue.DEVICES_TABLE);
		}
		finally
		{
			closeConnection();
			return flag;
		}
	}

	//****************** CHANGED UNCERTAIN (added notes where needed) *********************\\
	
	@SuppressWarnings("finally")
	public User getUser(String i_username, String i_userPassword) throws SQLException //final&complete IMPL
	{
		User res=null;
		boolean isCustomer = true;
		
		try
		{
			res = getCustomer(i_username, i_userPassword);
			
			if(res==null)
			{
				isCustomer = false;
				res = getVendor(i_username, i_userPassword);
			}
		}
		finally
		{
			if(res==null)
				throw new SQLException("User not found!");
			res.setIsCustomer(isCustomer);
			return res;
		}
	}

	public void addVndor(Vendor i_vendor) throws SQLException {
		final String sqlQuery =	"insert into CUSTOMERS (vendor_id, vendor_name , user_name, user_password, email, vendor_description, vendor_logo_pic)"
				+ "values ("
				+"'"+i_vendor.getId()+"',"
				+"'"+i_vendor.getName()+"',"
				+"'"+i_vendor.getUserName()+"'"
				+"'"+i_vendor.getPassword()+"'"
				+"'"+i_vendor.getEmail()+"'"
				+"'"+i_vendor.getDescription()+"'"
				+"'"+i_vendor.getLogoPicURL()+"'"
				+")";

		boolean flag = false;

		try 
		{
				Connection connection = openConnection();
				java.sql.PreparedStatement insertStat  = connection.prepareStatement(sqlQuery);
				insertStat.executeUpdate();
				flag = true;
				incrementMaxIdxValue(EntityAndIdxValue.VENDORS_TABLE);
		}
		finally
		{
			closeConnection();
			if(!flag)
				throw new SQLException("New Customer couldn't be added!");
		}
	}
	
	@SuppressWarnings("finally")
	public Customer addCustomer(Customer i_User) throws SQLException //final&complete IMPL
	{
		String fName=null, lName=null;
		if(i_User.getName().contains(" "))
		{
			fName = i_User.getName().split(" ")[0];
			lName = i_User.getName().split(" ")[1];
		}
		else
			fName = lName = i_User.getName();
		
		final String sqlQuery =	"insert into CUSTOMERS (customer_id, first_name , last_name, user_name, user_password, email) "
				+ "values ("
				+"'"+i_User.getId()+"',"
				+"'"+fName+"',"
				+"'"+lName+"',"
				+"'"+i_User.getUserName()+"',"
				+"'"+i_User.getPassword()+"',"
				+"'"+i_User.getEmail()+"'"
				+");";

		boolean flag = false;

		try 
		{
				Connection connection = openConnection();
				java.sql.PreparedStatement insertStat  = connection.prepareStatement(sqlQuery);
				insertStat.executeUpdate();
				flag = true;
				incrementMaxIdxValue(EntityAndIdxValue.CUSTOMERS_TABLE);
		}
		finally
		{
			closeConnection();
			if(!flag)
				throw new SQLException("New Customer couldn't be added!");
			return i_User;
		}		
	}
	
	//Returns just a customer, without additionals
	@SuppressWarnings("finally")
	public Customer getCustomer(String i_username, String i_password) throws SQLException //final&complete IMPL
	{
		Customer res=null;
		
		try
		{
			Connection connection = openConnection();
			PreparedStatement queryStatement;
			ResultSet queryResult;
			
			queryStatement  =connection.prepareStatement("select * from CUSTOMERS where user_name=? and user_password=?;");
			queryStatement.setString(1, i_username);
			queryStatement.setString(2, i_password);
			queryResult = queryStatement.executeQuery();
			
			if(queryResult.next())
			{
				String user_email=null, user_PicURL=null;
				String cust_fname=null, cust_lname=null;
				short cust_id=-1;
				boolean isCustomer = true;
				
				cust_id = Short.parseShort(queryResult.getString(1));
				cust_fname = queryResult.getString(2);
				cust_lname = queryResult.getString(3);
				user_email = queryResult.getString(6);
				
				res = new Customer(cust_id, i_username, i_password, cust_fname + " " + cust_lname, user_email, user_PicURL, isCustomer, null, null);
			}
		}	
		finally
		{
			closeConnection();
			if(res==null)
				throw new SQLException("No customer user found for provided credantials!");
			else
				return res;
		}
	}

	//Returns just a customer, without additionals
	@SuppressWarnings("finally")
	public Customer getCustomer(int cust_id) throws SQLException //final&complete IMPL
	{
		Customer res=null;
		
		try
		{
			Connection connection = openConnection();
			PreparedStatement queryStatement;
			ResultSet queryResult;
			
			queryStatement  =connection.prepareStatement("select * from CUSTOMERS where customer_id=?;");
			queryStatement.setString(1, Integer.toString(cust_id));
			queryResult = queryStatement.executeQuery();
			
			if(queryResult.next())
			{
				String cust_username=null, cust_password=null, user_email=null, user_PicURL=null;
				String cust_fname=null, cust_lname=null;
				boolean isCustomer = true;
				
				cust_fname = queryResult.getString(2);
				cust_lname = queryResult.getString(3);
				cust_username = queryResult.getString(4);
				cust_password = queryResult.getString(5);
				user_email = queryResult.getString(6);
				
				res = new Customer((short)cust_id, cust_username, cust_password, cust_fname + cust_lname, user_email, user_PicURL, isCustomer, null, null);
			}
		}	
		finally
		{
			closeConnection();
			if(res==null)
				throw new SQLException("No customer user found for provided credantials!");
			else
				return res;
		}
	}
	
	//Returns just a vendor, without additionals
	@SuppressWarnings("finally")
	public Vendor getVendor(String i_username, String i_password) throws SQLException //final&complete IMPL
	{
		Vendor res=null;
		
		try
		{
			Connection connection = openConnection();
			PreparedStatement queryStatement;
			ResultSet queryResult;
			
			queryStatement  =connection.prepareStatement("select * from VENDORS where user_name=? and user_password=?;");
			queryStatement.setString(1, i_username);
			queryStatement.setString(2, i_password);
			queryResult = queryStatement.executeQuery();
			
			if(queryResult.next())
			{
				String user_email=null;
				String vend_name=null, vend_desc=null, vend_logo_pic=null;
				short vend_id=-1;
				
				vend_id = Short.parseShort(queryResult.getString(1));
				vend_name = queryResult.getString(2);
				user_email = queryResult.getString(5);
				vend_desc = queryResult.getString(6);
				vend_logo_pic = queryResult.getString(7);
				
				res = new Vendor(vend_id, i_username, i_password, vend_name, vend_logo_pic, user_email, false, vend_desc, vend_logo_pic, getProducts(vend_id));
			}
		}	
		finally
		{
			closeConnection();
			if(res==null)
				throw new SQLException("No vendor user found for provided credantials!");
			else
				return res;
		}
	}

	//Returns just a vendor, without additionals
	@SuppressWarnings("finally")
	public Vendor getVendor(int vendor_id) throws SQLException //final&complete IMPL
	{
		Vendor res=null;
		
		try
		{
			Connection connection = openConnection();
			PreparedStatement queryStatement;
			ResultSet queryResult;
			
			queryStatement  =connection.prepareStatement("select * from VENDORS where vendor_id=?;");
			queryStatement.setString(1, Integer.toString(vendor_id));
			queryResult = queryStatement.executeQuery();
			
			if(queryResult.next())
			{
				String user_email=null, vend_username=null, vend_password=null;
				String vend_name=null, vend_desc=null, vend_logo_pic=null;
				
				vend_name = queryResult.getString(2);
				vend_username = queryResult.getString(3);
				vend_password = queryResult.getString(4);
				user_email = queryResult.getString(5);
				vend_desc = queryResult.getString(6);
				vend_logo_pic = queryResult.getString(7);
				
				res = new Vendor((short)vendor_id, vend_username, vend_password, vend_name, vend_logo_pic, user_email, false, vend_desc, vend_logo_pic, getProducts(vendor_id));
			}
		}	
		finally
		{
			closeConnection();
			if(res==null)
				throw new SQLException("No vendor user found for provided credantials!");
			else
				return res;
		}
	}

	@SuppressWarnings("finally")
	public LinkedList<Scenario> getScenarios(int i_cust_id) throws SQLException 
	{
		LinkedList<Scenario> res = null;
		Pair<Short, Scenario> currentScen = getScenario((short)i_cust_id, -1);
		LinkedList<Short> scenario_ids= null;
		
		try
		{
			Connection connection = openConnection();
			PreparedStatement queryStatement;
			ResultSet queryResult;
			
			queryStatement  =connection.prepareStatement("select DISTINCT(SCENARIOS.scenario_id), SCENARIOS.customer_id, SCENARIOS.scenario_name, SCENARIOS.scenario_description, SCENARIOS_EVENTS.event_id from SCENARIOS INNER JOIN SCENARIOS_EVENTS ON SCENARIOS.scenario_id=SCENARIOS_EVENTS.scenario_id where SCENARIOS.customer_id=? ORDER BY SCENARIOS.scenario_id;");
			queryStatement.setString(1, Integer.toString(i_cust_id));
			queryResult = queryStatement.executeQuery();			
			while(queryResult.next())
			{
				if(scenario_ids == null)
					scenario_ids = new LinkedList<Short>();
				scenario_ids.add(Short.parseShort(queryResult.getString(1)));
			}
			
			
			if(currentScen.getKey()!=-1 && currentScen.getValue()!=null)
			{
				res = new LinkedList<Scenario>();
				res.add(currentScen.getValue());
				scenario_ids.removeFirst();
				
				while(!scenario_ids.isEmpty())
				{
					currentScen = getScenario((short)i_cust_id, scenario_ids.removeFirst());
					if(currentScen.getKey()!=-1 && currentScen.getValue()!=null)
						res.add(currentScen.getValue());
				}
			}
		}
		finally
		{
			closeConnection();
			if(scenario_ids==null)
				throw new SQLException("No Scenarios Found in DB!");
			return res;
		}
		
	}
	
	@SuppressWarnings("finally")
	private Pair<Short, Scenario> getScenario(short i_cust_id, int i_scenario_id) throws SQLException
	{
		short scenario_id=-1, event_id=-1, action_id=-1;
		boolean isStillOk=true;
		boolean isNeedToCloseConn=false;
		char logic_oper = '\0';
		String scenario_name=null, scenario_description=null; 
		
		Scenario res = null;
		LinkedList<Action> acts = null;
		LinkedList<Event> eves = null;
		
		try
		{
			Connection connection = currSession;
			if(currSession==null)
			{
				connection = openConnection();
				isNeedToCloseConn = true;
			}
			PreparedStatement scen_actQStatement, scen_eveQStatement, logexprQStatement;
			ResultSet scen_actsQResult, scen_evesQResult, logexprsQResult;
			boolean isRelevantIter= true;
			
			scen_eveQStatement  =connection.prepareStatement("select SCENARIOS.scenario_id, SCENARIOS.customer_id, SCENARIOS.scenario_name, SCENARIOS.scenario_description, SCENARIOS_EVENTS.event_id from SCENARIOS INNER JOIN SCENARIOS_EVENTS ON SCENARIOS.scenario_id=SCENARIOS_EVENTS.scenario_id where SCENARIOS.customer_id=? ORDER BY SCENARIOS.scenario_id;");
			scen_eveQStatement.setString(1, Integer.toString(i_cust_id));
			scen_evesQResult = scen_eveQStatement.executeQuery();
			while (scen_evesQResult.next())
			{
				if(i_scenario_id == -1)
				{
					if(scenario_id==-1)
						scenario_id = Short.parseShort(scen_evesQResult.getString(1));
					
					if(scenario_id!=-1 && scenario_id<Short.parseShort(scen_evesQResult.getString(1)))
						break;
					else if(scenario_id!=-1 && scenario_id>Short.parseShort(scen_evesQResult.getString(1)))
						isRelevantIter = false;
					else
						isRelevantIter = true;
				}
				else
				{
					if(i_scenario_id<Short.parseShort(scen_evesQResult.getString(1)))
					{
						break;
					}
					else if(i_scenario_id>Short.parseShort(scen_evesQResult.getString(1)))
					{
						isRelevantIter = false;
					}
					else if(i_scenario_id==Short.parseShort(scen_evesQResult.getString(1)))
					{
						isRelevantIter = true;
						scenario_id = Short.parseShort(scen_evesQResult.getString(1));
					}
				}
				
				if(isRelevantIter)
				{
					if(eves==null)
					{
						eves = new LinkedList<Event>();
					}
					
					scenario_id = Short.parseShort(scen_evesQResult.getString(1));
					scenario_name= scen_evesQResult.getString(3);
					scenario_description= scen_evesQResult.getString(4);
					event_id=Short.parseShort(scen_evesQResult.getString(5));
					
					Event current_eve = getEvent(event_id);
					connection = openConnection();
					logexprQStatement = connection.prepareStatement("select * from LOGEXPRS where scenario_id=? and event_id=?;");
					logexprQStatement.setString(1, Short.toString(scenario_id));
					logexprQStatement.setString(2, Short.toString(event_id));
					logexprsQResult = logexprQStatement.executeQuery();
					
					if(!logexprsQResult.next())
						isStillOk = false;
					else
					{
						logic_oper = logexprsQResult.getString(4).charAt(0);
					}
					
					
					
					current_eve.setLogicOper(ElogicOperand.setLogicOperFromChar(logic_oper));
					eves.add(current_eve);
				}
			}
			
			
			isRelevantIter = true;
			scen_actQStatement  =connection.prepareStatement("select SCENARIOS.scenario_id, SCENARIOS.customer_id, SCENARIOS.scenario_name, SCENARIOS.scenario_description, SCENARIOS_ACTIONS.action_id from SCENARIOS INNER JOIN SCENARIOS_ACTIONS ON SCENARIOS.scenario_id=SCENARIOS_ACTIONS.scenario_id where SCENARIOS.customer_id=? ORDER BY SCENARIOS.scenario_id;");
			scen_actQStatement.setString(1, Integer.toString(i_cust_id));
			scen_actsQResult = scen_actQStatement.executeQuery();
			while (scen_actsQResult.next() && isStillOk)
			{	
				if(i_scenario_id==-1)
				{
					if(scenario_id!=-1 && scenario_id<Short.parseShort(scen_actsQResult.getString(1)))
						break;
					else if(scenario_id!=-1 && scenario_id>Short.parseShort(scen_actsQResult.getString(1)))
						isRelevantIter = false;
					else
						isRelevantIter = true;
				}
				else
				{
					if(i_scenario_id<Short.parseShort(scen_actsQResult.getString(1)))
					{
						break;
					}
					else if(i_scenario_id>Short.parseShort(scen_actsQResult.getString(1)))
					{
						isRelevantIter = false;
					}
					else if(i_scenario_id==Short.parseShort(scen_actsQResult.getString(1)))
					{
						isRelevantIter = true;
						scenario_id = Short.parseShort(scen_actsQResult.getString(1));
					}
				}
				
				
				if(isRelevantIter)
				{
					if(acts==null)
					{
						acts = new LinkedList<Action>();
					}

					action_id=Short.parseShort(scen_actsQResult.getString(5));
					
					Action current_act = getAction(action_id);
					connection = openConnection();
					
					acts.add(current_act);
				}
			}
			
			res = new Scenario(scenario_id, (short)i_cust_id, scenario_name, scenario_description, eves, acts);
			
		}	
		finally
		{
			if(isNeedToCloseConn)
				closeConnection();
			if(!isStillOk)
				throw new SQLException("Wrong representation in DB, scenario, event, and logic expression tables aren't normalized!");
			return new Pair<Short, Scenario>(scenario_id, res);
		}
	}
	
	private Action getAction(short i_action_id)
	{
		//TODO
		return null;
	}
	
	public Scenario getScenario(short i_event_id)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean removeDevice(int device_id)
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean removeProduct(int productToRemove_id) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean updateProduct(int prod_id, Product new_product) 
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean updateDevice(int origionalDevice_id, Device newDevice) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean updateScenario(int origionalScenario_id, Scenario newScenario) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean removeScenario(int scenarioToRemove_id)
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	//**************** CHANGE UNCERTAIN(included notes) ******************\\
	@SuppressWarnings("finally")
	public Event getEvent(int event_id) throws Exception
	{
		Event res = null;

		String eve_name=null, eve_type=null, eve_param=null, eve_deviceEP=null;
		short eve_deviceId=-1, eve_id=-1, eve_proto=-1, eve_prod_id=-1;
		char eve_logicOperator=' ';
		
		try
		{
			Connection connection = openConnection();
			PreparedStatement queryingStatement;
			ResultSet queryResult;
			
			queryingStatement  =connection.prepareStatement(k_selectEvent);
			queryingStatement.setString(1, Integer.toString(event_id));
			queryResult = queryingStatement.executeQuery();
					
			if (!queryResult.next())
			{
				throw new SQLException("Wrong tables format - maximum ids fields weren't found!");
			}
			else
			{
				eve_id = Short.parseShort(queryResult.getString(1));
				eve_deviceId = Short.parseShort(queryResult.getString(2));
				eve_proto = Short.parseShort(queryResult.getString(3));
				eve_param = queryResult.getString(4);
				eve_logicOperator = queryResult.getString(5).charAt(0);
			} 
			
			queryingStatement  =connection.prepareStatement(k_selectEventProto);
			queryingStatement.setString(1, Short.toString(eve_proto));
			queryResult = queryingStatement.executeQuery();
					
			if (!queryResult.next())
			{
				throw new SQLException("Wrong tables format - maximum ids fields weren't found!");
			}
			else
			{
				eve_prod_id = Short.parseShort(queryResult.getString(2));
				eve_name = queryResult.getString(3);
				eve_type = queryResult.getString(5);
			} 
			
			boolean isTriggered = false;
			boolean isEvent = true;
			
			ActionEventProto eve_prototype = new ActionEventProto(eve_proto, eve_name, description, eve_type, supportedValues, min, max,  eve_prod_id, eve_deviceEP, isEvent);
			//Action eveWithParamAndDevID = new Action(eve_id, eve_deviceId, eve_param, eve_prototype);
			Event completeEvent = new Event(eve_id, eve_deviceId, eve_param, eve_prototype, eve_logicOperator, isTriggered);
			res = completeEvent;
			//res = new Event(eve_name, eve_type, eve_param, eve_logicOperator, eve_deviceEP, eve_deviceId);
			/*
			 * NOTES:
			 * 1. no need to create action and then deliver it to event.
			 * 2. need to fetch isTriggered from DB in case that the event has already happened.
			 * 3. need to fetch isEvent from DB to place in isEvent.
			 * watch booleans isTriggered, and boolean isEvent in c'tors lines 747, 749.
			 */
		}
		finally
		{
			closeConnection();
			return res;
		}
	}

	public void removeProduct(Product productToRemove)
	{
		
	}

	public short getIdForScenario() 
	{
		return getScenariosMaxAvailableIdx();
	}

	@SuppressWarnings("finally")
	public boolean addProduct(Product i_product) throws SQLException
	{
		final String sqlQuery =	"insert into PRODUCTS (vendor_id, product_name, product_pic, events_state, actions_state)"
				+ "values ("
				+"'"+i_product.getVendor_id()+"',"
				+"'"+i_product.getName()+"',"
				+"'"+i_product.getPicURL()+"'"
				+"'"+i_product.isEventState()+"'"
				+"'"+i_product.isActionState()+"'"
				+")";
		
		boolean flag = false;

		try 
		{
				Connection connection = openConnection();
				java.sql.PreparedStatement insertStat  = connection.prepareStatement(sqlQuery);
				insertStat.executeUpdate();
				flag = true;
		}
		finally
		{
			closeConnection();
			return flag;
		}
	}
	
	private void initializeAllMaxIds() throws SQLException
	{
		Connection connection = openConnection();
		PreparedStatement queryingStatement=null;
		ResultSet queryResult=null;
		
		for(EntityAndIdxValue ev:EntityAndIdxValue.values())
		{
			queryingStatement = connection.prepareStatement(String.format(k_selectMaxID, ev.idxValColumn, ev.Entity));
			
			queryResult = queryingStatement.executeQuery();
				
			if (!queryResult.next())
			{
				throw new SQLException("Wrong tables format - maximum ids fields weren't found!");
			}
			else
			{
				String currentVal = queryResult.getString(1);
					
				if(currentVal!=null)
				{
					TABLE_maxID[ev.idx]= Short.parseShort(currentVal);
				}
				else
				{
					TABLE_maxID[ev.idx]=0;
				}
			} 
		}
		closeConnection();
	}		
	
	private void incrementMaxIdxValue(EntityAndIdxValue tableName)
	{
		TABLE_maxID[tableName.idx]++;
	}
	
	public short getCustomersMaxAvailableIdx()
	{
		return (short) (TABLE_maxID[EntityAndIdxValue.CUSTOMERS_TABLE.idx]+1); 
	}
	
	public short getVendorsMaxAvailableIdx()
	{
		return (short) (TABLE_maxID[EntityAndIdxValue.VENDORS_TABLE.idx]+1);
	}
	
	public short getEventsMaxAvailableIdx()
	{
		return (short) (TABLE_maxID[EntityAndIdxValue.EVENTS_TABLE.idx]+1);
	}
	
	public short getEventsProtoMaxAvailableIdx()
	{
		return (short) (TABLE_maxID[EntityAndIdxValue.EVENTS_PROTO_TABLE.idx]+1);
	}
	
	public short getActionsMaxAvailableIdx()
	{
		return (short) (TABLE_maxID[EntityAndIdxValue.ACTIONS_TABLE.idx]+1);
	}
	
	public short getActionsProtoMaxAvailableIdx()
	{
		return (short) (TABLE_maxID[EntityAndIdxValue.ACTIONS_PROTO_TABLE.idx]+1);
	}
	
	public short getProductsMaxAvailableIdx()
	{
		return (short) (TABLE_maxID[EntityAndIdxValue.PRODUCTS_TABLE.idx]+1);
	}
	
	public short getScenariosMaxAvailableIdx()
	{
		return (short) (TABLE_maxID[EntityAndIdxValue.SCENARIOS_TABLE.idx]+1);
	}
	
	public short getDevicesMaxAvailableIdx()
	{
		return (short) (TABLE_maxID[EntityAndIdxValue.DEVICES_TABLE.idx]+1);
	}

	public boolean isEventUpdated(Event event) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void addEvent(Event event) {
		// TODO Auto-generated method stub
		
	}

	public void addAction(Action action) {
		// TODO Auto-generated method stub
		
	}
}