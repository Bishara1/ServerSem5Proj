package databaselogic;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

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
	  
	  @SuppressWarnings("unchecked")
	  public void SaveToDB(Object message) throws SQLException {
		  
		  	Message msg = (Message)message;
		  	
		  	
			PreparedStatement ps = conn.prepareStatement("INSERT INTO users "
					+ "(first_name, last_name, id, phone_number, email_address,"
					+ " credit_card_number, subscriber_number,user_name,password,role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?)");
			ArrayList<String> data = (ArrayList<String>) msg.getContent();
			try 
			{
				for (int i = 1; i < 11; i++)
				{	if ((i==3) || (i==7))
						ps.setInt(i, Integer.parseInt(data.get(i-1)));
				else
					ps.setString(i, data.get(i-1));
				}
				
//				ps.executeQuery();
				ps.executeUpdate();
			} catch (SQLException e) { e.printStackTrace(); }
		}
	 
	  public void UpdateToDB(String[] details) throws SQLException {
		    // data format = { id credit_card subscriber_num}
			PreparedStatement ps = conn.prepareStatement("UPDATE users "
					+ "Set credit_card_number = ?, subscriber_number = ? "
					+ "Where id = ?");
			
			try {
				ps.setString(1, details[1]);  // credit_card
				ps.setString(2, details[2]);  // subscriber_number
				ps.setString(3, details[0]);  // id
				
				ps.executeUpdate();
			} catch (SQLException e) { e.printStackTrace(); }
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
							tempO.setConfirmation_date(rs.getDate(5));
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
			String[] passRoleFname = new String[3];
			try 
			{
				stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT password,role,first_name FROM users Where user_name = \""+ username +"\"");
		 		if(!rs.next())
		 			return new String[] {"","",""};
				passRoleFname[0] = rs.getString(1);
		 		passRoleFname[1] = rs.getString(2);
		 		passRoleFname[2] = rs.getString(3);
				
				rs.close();
			} catch (SQLException e) { e.printStackTrace(); }
			
			return passRoleFname;
	   }
	  
	   public static synchronized DatabaseController GetFunctionsInstance(String databasePassword) {
		   return ( DBFunctionsInstance == null ) ? new DatabaseController(databasePassword) : DBFunctionsInstance;
	   }
}
