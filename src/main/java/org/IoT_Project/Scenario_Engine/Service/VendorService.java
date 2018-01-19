package org.IoT_Project.Scenario_Engine.Service;

import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.IoT_Project.Scenario_Engine.Models.*;
import DataBase.DBHandler;

public class VendorService {

	public org.IoT_Project.Scenario_Engine.Models.Vendor fetch(String i_name, String i_pswd) throws Exception{
			return DBHandler.getInstance().getVendor(i_name, i_pswd);
	}

	public  List<Product> addProduct(Product i_product) throws Exception{
		Product newProduct = new Product(i_product);
		if(DBHandler.getInstance().addProduct(newProduct))
		{
				return DBHandler.getInstance().getProducts(newProduct.getVendor_id());
		}
		else
			throw new Exception("no User");
	}


	public  List<Product> removeProduct(int vendor_id, Product i_prod2Remove) throws Exception{
		if(DBHandler.getInstance().removeProduct(i_prod2Remove.getId()))
		{
			return DBHandler.getInstance().getProducts(vendor_id);
		}
		else
			throw new Exception("no User, or no Product to remove");
	}

	public Vendor fetch(User i_user) throws Exception{
		return DBHandler.getInstance().getVendor(i_user.getName(), i_user.getPassword());
	}

	public List<Product> updateProduct(short i_id, short i_deviceToUpdateId, Product i_prod) throws Exception{
		if(DBHandler.getInstance().updateProduct(i_deviceToUpdateId, i_prod))
		{
			return DBHandler.getInstance().getProducts(i_id);
		}
		else
			throw new Exception("no User, or no Product to update");
		
	}

	public List<Product> fetchProducts(short i_userId) throws SQLException {
		return DBHandler.getInstance().getProducts(i_userId);
	}
}
