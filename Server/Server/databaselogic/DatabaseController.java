package databaselogic;

import java.sql.Connection;


import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

//import Client.ChatClient;
import common.Command;
import common.Message;
import gui_server.ServerInfoController;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import logic.*;

//This Class is built using Singleton design pattern
public class DatabaseController {
	  private Connection conn;
	  private static DatabaseController DBFunctionsInstance = null;  // only one instance (singleton)
	
	  private DatabaseController(String dbpassword) {
		  ConnectToDB(dbpassword);
	  }
	
	
	public void ConnectToDB(String databasePassword) {
		  try {
		      Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		      System.out.println("Driver definition succeed");
		  } catch (Exception ex) {
			  /* handle the error*/
//			  sic.screen.setText("Driver definition failed");
			  System.out.println("Driver definition failed");
	      }
	      
	      try 
	      {
	          conn = DriverManager.getConnection("jdbc:mysql://localhost/ekrut?serverTimezone=IST", "root", databasePassword);
	          System.out.println("SQL connection succeed");
	   	  } catch (SQLException ex)  { /* handle any errors*/
				System.out.println("SQLException: " + ex.getMessage());
				System.out.println("SQLState: " + ex.getSQLState());
				System.out.println("VendorError: " + ex.getErrorCode());
	   	  }
	  }
	  
	  
	  public void SaveToDB(Object message) throws SQLException {
		  
		  	PreparedStatement ps;
		  
		  	Message msg = (Message)message;
		  	ArrayList<String> data = (ArrayList<String>) msg.getContent();
		  	
		  	switch(msg.getCommand()) {
		  	case InsertUser:
		  	ps = conn.prepareStatement("INSERT INTO users "
						+ "(first_name, last_name, id, phone_number, email_address,"
						+ " credit_card_number, subscriber_number,user_name,password,role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?)");
				try 
				{
					for (int i = 1; i < 11; i++)
					{	if ((i==3) || (i==7))
							ps.setInt(i, Integer.parseInt(data.get(i-1)));
					else
						ps.setString(i, data.get(i-1));
					}
					
//					ps.executeQuery();
					ps.executeUpdate();
				} catch (SQLException e) { e.printStackTrace(); }
		  		
		  	case InsertOrder:
		  		ps = conn.prepareStatement("INSERT INTO orders "
						+ "(order_number, customer_id, order_status, order_created,"
						+ " location, items_in_order,price,supply_method,machine_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
		  		try 
				{
					ps.setInt(1, getLastId("orders", Command.ReadOrders));
					ps.setInt(2, Integer.parseInt(data.get(0))); 
					ps.setString(3, "Pending");
					ps.setDate(4, Date.valueOf(LocalDate.now()));
					ps.setString(5, data.get(1));
					ps.setString(6, data.get(2));
					ps.setInt(7, Integer.parseInt(data.get(3)));
					ps.setString(8, data.get(4));
					ps.setInt(9, Integer.parseInt(data.get(5)));
					ps.executeUpdate();
					
				} catch (SQLException e) { e.printStackTrace(); }
		  		
				ps = conn.prepareStatement("UPDATE machines " 
						+ "SET amount_per_item = ?"
						+ "WHERE machine_id = ?"); 
				try {
					ps.setString(1, data.get(6));
					ps.setInt(2, Integer.parseInt(data.get(5)));
					ps.executeUpdate();
				} catch (SQLException e) { e.printStackTrace(); }
				//add new Total Inventory
				break;
		  		
		  	case InsertOrderReport:
		  		ps = conn.prepareStatement("INSERT INTO ordersreport "
						+ "(report_id, machine_id, location, data, month, year)"
						+ " VALUES (?, ?, ?, ?, ?, ?)");
		  		try 
		  		{
		  			ps.setString(1, String.valueOf(getLastId("ordersreport", Command.InsertOrderReport)));
		  			ps.setString(2, data.get(1));
		  			ps.setString(3, data.get(2));
		  			ps.setString(4, data.get(3));
		  			ps.setString(5, data.get(4));
		  			ps.setString(6, data.get(5));
		  			ps.executeUpdate();
		  			
		  		} catch (Exception e) {	e.printStackTrace();}
		  		break;
		  		
		  	case InsertInventoryReport:
		  		ps=conn.prepareStatement("INSERT INTO inventoryreport "
						+ "(machine_id, items, total_inventory, location)"
						+ " VALUES ( ?, ?, ?, ?)");
		  		try 
		  		{
		  			ps.setString(1, data.get(0));
		  			ps.setString(2, data.get(1));
		  			ps.setString(3, data.get(2));
		  			ps.setString(4, data.get(3));
		  			
		  			ps.executeUpdate();
		  			
		  		} catch (Exception e) {	e.printStackTrace();}
		  		break;
		  		
		  	default:
		  		break;
		  	}
		  	
			

		}
	 
	  public void UpdateToDB(Message details) throws SQLException {
		    // data format = { id credit_card subscriber_num}
		 String[] s = (String[]) details.getContent();
		 String TableName = s[0];
		 PreparedStatement ps;
		  switch (TableName)
		  {
		case "ordersreport":
			ps = conn.prepareStatement("UPDATE ordersreport "
					+ "Set data = ?"
					+ "Where report_id = ?");
			try {
				ps.setString(1, s[2]);
				ps.setString(2, s[1]);
			
				ps.executeUpdate();
			} catch (SQLException e) { e.printStackTrace(); }
			break;
			
		case "machinesAmount":
			ps = conn.prepareStatement("UPDATE machines " 
					+ "SET amount_per_item = ?"
					+ "WHERE machine_id = ?"); 
			try {
				ps.setString(1, s[2]);
				ps.setString(2, s[1]);
				ps.executeUpdate();
			} catch (SQLException e) { e.printStackTrace(); }
			//add new Total Inventory
			break;


		default:
			System.out.println("default entered");
			break;
		}
//			PreparedStatement ps = conn.prepareStatement("UPDATE users "
//					+ "Set credit_card_number = ?, subscriber_number = ? "
//					+ "Where id = ?");
			
			
		}
	  


	  public ArrayList<Object> ReadFromDB(Message m) throws SQLException {
		    Statement stmt;
			ArrayList<Object> alldata = new ArrayList<>();
			String query = m.getCommand().GetQuery();
		
			if ((int)m.getContent() != 0) 
				query += " WHERE " + m.getCommand().GetID() + " = " + (int)m.getContent();
			
			try {
				stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query);
				if(!rs.next()) {
					alldata.add(null);
					return alldata;
		 			}
				rs = stmt.executeQuery(query);
				switch (m.getCommand()) {
					case ReadMachines:
						Machine tempM;
						while(rs.next()) { 
							tempM = new Machine();
							tempM.setMachine_id(rs.getInt(1));
							tempM.setLocation(rs.getString(2));
							tempM.setThreshold(rs.getInt(3));
							tempM.setTotal_inventory(rs.getInt(4));
							tempM.setAllItems(rs.getString(5));
							tempM.setAmount_per_item(rs.getString(6));
							alldata.add(tempM);
						}
						break;
						
					case ReadDeliveries:
						Delivery tempD;
						while(rs.next()) {
							tempD = new Delivery();
							tempD.setDelivery_id(rs.getInt(1));
							tempD.setOrder_id(rs.getInt(2));
							tempD.setShipping_date(rs.getDate(3));
							tempD.setEstimated_dleivery(rs.getDate(4));
							tempD.setStatus(rs.getString(5));
							alldata.add(tempD);
						}
						break;
						
					case ReadRequests:
						Request tempR;
						while(rs.next()) {
							tempR = new Request();
							tempR.setRequest_id(rs.getInt(1));
							tempR.setCustomer_id(rs.getInt(2));
							tempR.setType(rs.getString(3));
							alldata.add(tempR);
						}
						break;
						
					case ReadUsers:
						Subscriber tempSub;
						while(rs.next()) {
							tempSub = new Subscriber();
							tempSub.setFname(rs.getString(1));
							tempSub.setLName(rs.getString(2));
							tempSub.setId(rs.getInt(3));
							tempSub.setPhoneNum(rs.getString(4));
							tempSub.setEmail(rs.getString(5));
							tempSub.setVisa(rs.getString(6));
							tempSub.setSubNum(rs.getInt(7));
							tempSub.setUserName(rs.getString(8));
							tempSub.setPassword(rs.getString(9));
							tempSub.setRole(rs.getString(10));
							tempSub.setIs_new_subscriber(rs.getInt(11));
							alldata.add(tempSub);
						}
						break;
						
					case ReadOrders:
						Order tempO;
						while(rs.next()) {
							tempO = new Order();
							tempO.setOrder_num(rs.getInt(1));
							tempO.setCustomer_id(rs.getInt(2));
							tempO.setOrder_status(rs.getString(3));
							tempO.setOrder_created(rs.getDate(4));
							tempO.setLocation(rs.getString(6));
							tempO.setItems_in_order(rs.getString(7));
							tempO.setPrice(rs.getInt(8));
							tempO.setSupply_method(rs.getString(9));
							tempO.setMachine_id(rs.getInt(10));
							alldata.add(tempO);
						}
						break;
						
					case ReadItems:
						Item tempI;
						while(rs.next()) {
							tempI = new Item();
							tempI.setProductID(rs.getString(1));
							tempI.setPrice(rs.getInt(2));
							alldata.add(tempI);
						}
						break;
						
					case ReadLocations:
						Location tempL;
						while(rs.next()) {
							tempL = new Location();
							tempL.setLocation(rs.getString(1));
							tempL.setSale_value(rs.getInt(2));
							alldata.add(tempL);
						}
						break;
					
					case ReadOrdersReports:
						OrdersReports tempRorders;
						while(rs.next())
						{
							tempRorders = new OrdersReports();
							tempRorders.setReport_id(rs.getString(1));
							tempRorders.setMachine_id(rs.getString(2));
							tempRorders.setLocation(rs.getString(3));
							tempRorders.setData(rs.getString(4));
							tempRorders.setMonth(rs.getString(5));
							tempRorders.setYear(rs.getString(6));
							alldata.add(tempRorders);
						}
						break;
						
					default:
						System.out.println("Dude what");
						return null;
							
				}
				rs.close();
			}
			catch (SQLException e) {e.printStackTrace(); }
			
			return alldata;
	   }
	  
	  public String[] ConnectToServer(String username) throws SQLException {
			Statement stmt;
			String password = null;
			String[] passRoleFnameSubNumFirstCart = new String[5];
			try 
			{
				stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT password,role,first_name,subscriber_number,is_new_subscriber FROM users Where user_name = \""+ username +"\"");
		 		if(!rs.next())
		 			return new String[] {"","",""};
				passRoleFnameSubNumFirstCart[0] = rs.getString(1);
		 		passRoleFnameSubNumFirstCart[1] = rs.getString(2);
		 		passRoleFnameSubNumFirstCart[2] = rs.getString(3);
		 		passRoleFnameSubNumFirstCart[3] = rs.getString(4);
		 		passRoleFnameSubNumFirstCart[4] = rs.getString(5);
				
				rs.close();
			} catch (SQLException e) { e.printStackTrace(); }
			
			return passRoleFnameSubNumFirstCart;
	   }
	  
	   public static synchronized DatabaseController GetFunctionsInstance(String databasePassword) {
		   return ( DBFunctionsInstance == null ) ? new DatabaseController(databasePassword) : DBFunctionsInstance;
	   }
	   
	   public int getLastId(String tableName, Command cmd) throws SQLException
	   {
		   String query = "SELECT " + cmd.GetID() + " FROM " + tableName; 
		   Statement stmt;
		   stmt = conn.createStatement();
		   ResultSet rs = stmt.executeQuery(query);
		   ArrayList<Integer> ID = new ArrayList<Integer>();
		   while(rs.next())
			   ID.add(rs.getInt(1));
		   int lastID = 0;
		   for(Integer id : ID)
		   {
			   if(id > lastID)
				   lastID = id;
		   }
		   lastID++;
		   return lastID;
		   
	   }
	   
	   
}
