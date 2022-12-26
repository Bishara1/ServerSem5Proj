package databaselogic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import common.Message;
import logic.Machine;
import logic.Subscriber;

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
			PreparedStatement ps = conn.prepareStatement("INSERT INTO users "
					+ "(first_name, last_name, id, phone_number, email_address,"
					+ " credit_card_number, subscriber_number,user_name,password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			ArrayList<String> data = (ArrayList<String>) message;
			
			try 
			{
				for (int i = 1; i < 10; i++)
					ps.setString(i, data.get(i-1));
				
				ps.executeQuery();
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
				
				switch (m.getCommand()) {
					case ReadMachines:
						Machine tempM = new Machine();
						while(rs.next()) { 
							tempM.setMachine_id(Integer.parseInt(rs.getString(1)));
							tempM.setLocation(rs.getString(2));
							tempM.setThreshold(Integer.parseInt(rs.getString(3)));
							tempM.setTotal_inventory(Integer.parseInt(rs.getString(4)));
							tempM.setItems(rs.getString(5));
							tempM.setAmount_per_item(rs.getString(6));
							alldata.add(tempM);
							tempM = new Machine();
						}
						break;
				}
				rs.close();
			}
			catch (SQLException e) {e.printStackTrace(); }
			
			return alldata;
	   }
	  
	  public String ConnectToServer(String username) throws SQLException {
			Statement stmt;
			String password = null;
			try 
			{
				stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT password FROM users Where user_name = \""+ username +"\"");
		 		if(!rs.next())
		 			return "";
				password = rs.getString(1); //check if username exists lol I forgor :skull_emoji:
		 		
				rs.close();
			} catch (SQLException e) { e.printStackTrace(); }
			
			return password;
	   }
	  
	   public static synchronized DatabaseController GetFunctionsInstance(String databasePassword) {
		   return ( DBFunctionsInstance == null ) ? new DatabaseController(databasePassword) : DBFunctionsInstance;
	   }
}
