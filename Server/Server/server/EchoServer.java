package server;
// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.io.*;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;

import logic.Connected;
import logic.Delivery;
import logic.Item;
import logic.Location;
import logic.Machine;
import logic.Order;
import logic.OrdersReports;
import logic.Subscriber;
import logic.Request;
import databaselogic.DatabaseController;
import ocsf.server.*;
import common.Command;
import common.Message;


/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */
public class EchoServer extends AbstractServer { 
	private DatabaseController dbController;
	public static ArrayList<Connected> users = new ArrayList<Connected>();
	private static String databasePassword = null;
  //Class variables *************************************************
  
  /**
   * The default port to listen on.
   */
  final public static int DEFAULT_PORT = 5555;
  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   */
  public EchoServer(int port) {
    super(port);
  }

  
  //Instance methods ************************************************
  
  /**
   * This method handles any messages received from the client.
   *
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
  
  public void handleMessageFromClient(Object msg, ConnectionToClient client)
  {
	  	Message data = (Message) msg;
	  
		System.out.println("Message received: " + msg + " from " + client);
		try {
		
			ParseClientData(data, client);
		} 
		//catch (SQLException e) {e.printStackTrace();} 
		catch (IOException e) {
			e.printStackTrace();
		}	   
  }
  
  public static String getLocalIp() {
	  String ip = null;
	  try {
		  ip = Inet4Address.getLocalHost().getHostAddress();
	  } catch (UnknownHostException e) { e.printStackTrace(); } 
	  
	  return ip;
  }

    
  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted() {
	  dbController = DatabaseController.GetFunctionsInstance(databasePassword);
	  System.out.println("Server listening for connections on port " + getPort());
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped() {
	  System.out.println("Server has stopped listening for connections.");
  }
  
  @Override
  protected void clientConnected(ConnectionToClient client) {
//	  ArrayList<String> info = new ArrayList<>();
//	  info.add(client.getInetAddress().toString());
	  System.out.println("Connected");
  	
  }
  

  
  //Class methods ***************************************************
  
  /**
   * This method is responsible for the creation of 
   * the server instance (there is no UI in this phase).
   *
   * @param args[0] The port number to listen on.  Defaults to 5555 
   *          if no argument is entered.
   */
  public static void runServer(String[] args) {
    int port = 0; //Port to listen on
    databasePassword = args[1];

    try {
      port = Integer.parseInt(args[0]); 
      
    }
    catch(Throwable t) {
      port = DEFAULT_PORT; //Set port to 5555
    }
	
    EchoServer sv = new EchoServer(port);
    
    try {
      sv.listen(); //Start listening for connections
    } 
    catch (Exception ex)  {
      System.out.println("ERROR - Could not listen for clients!");
    }
  }
  
 
  /**
   * @param data
   * @param client
   * @param ip
   * @throws IOException
   */
  public void ParseClientData(Message data, ConnectionToClient client) throws IOException {
	  Message response = new Message(null, null);
	  ArrayList<Object> GottenDatabase;
	  
	  try {
		  switch(data.getCommand()) 
		  {
		  
			  case DatabaseUpdate:
				  ArrayList<String> change = (ArrayList<String>) data.getContent();
				  String str = "";
					for(int i = 0;i<change.size();i++)
					{
						str += change.get(i);
						str += "|";
					}
					
				  String[] detailsToDB = str.split("\\|"); // get content -> ["tableName","id","data"]
				  Message msg = new Message(detailsToDB,data.getCommand());
				  dbController.UpdateToDB(msg);
				  response.setCommand(Command.DatabaseUpdate);
				  client.sendToClient(response); //tableName,id,whatToUpdate
				  break;
				  
			  case UpdateMachineStock:
				  ArrayList<String> update = ((ArrayList<String>)data.getContent()); // get content
				  String newAmount = "";
				  for(int i = 1;i<update.size();i++)
				  {
					  newAmount += update.get(i);
					  if(i != update.size()-1)
					  	newAmount += ",";
				  }
				  update.add(0, "machinesAmount");
				  String[] param = new String[3];
				  param[0] = "machinesAmount";
				  param[1] = update.get(1);
				  param[2] = newAmount;
				  Message msg1 = new Message(param,Command.UpdateMachineStock);
				  dbController.UpdateToDB(msg1);
				  response.setCommand(Command.UpdateMachineStock);
				  client.sendToClient(response);
				  break;
	
			  case InsertUser:
				  Message m = new Message(null, null);
				  String[] insertToDB = ((String)data.getContent()).split(" ");
				  ArrayList<String> toDB=new ArrayList<>();
				  for(String s: insertToDB)
				  {
					  toDB.add(s);
				  }
				  m.setContent(toDB);
				  dbController.SaveToDB(m);
				  response.setCommand(Command.InsertUser);
				  client.sendToClient(response);
				  break;
				 
				  
			  case Connect:
				  boolean found = false;
				  ArrayList<Subscriber> temp = new ArrayList<Subscriber>();
				  
				  for (Connected Client : users) {
					  if (Client.getIp().equals(client.toString().split(" ")[1])) {
						  users.get(users.indexOf(Client)).setStatus("Connected");
						  found = true;
						  break;
					  }
			  	  }
				  if(found == false)
				  {
					  String[] ip = client.toString().split(" ");
					  users.add(new Connected(ip[0],String.valueOf(this.getPort()),"Connected"));
					  
				  }
				  String username = (String)data.getContent();
				  String[] passRoleFname = dbController.ConnectToServer(username);
				  response.setContent(passRoleFname);
				  response.setCommand(Command.Connect);
				  client.sendToClient(response);
			  	  break;
	
			  case Disconnect:
				  for (Connected Client : users) {
					  if (Client.getIp().equals(client.toString().split(" ")[1])) {
						  users.get(users.indexOf(Client)).setStatus("Disconnected");
						  break;
					  }
				  }
				  
			  	  response.setCommand(Command.Disconnect);
			  	  client.sendToClient(response);
			  	  break;
			  	  
			   case ReadMachines:
				   response.setCommand(Command.ReadMachines);
				   GottenDatabase = dbController.ReadFromDB(data);//alldata - ArrayList<Object>
				   ArrayList<Machine> machines = new ArrayList<>();
				   
				   for (Object obj : GottenDatabase) {
					   machines.add((Machine) obj);
				   }
				   response.setContent(machines);
				   //loop was taking too long -> looked like it was crashing
				   client.sendToClient(response);
				   break;
				   
			    case ReadDeliveries:
				   response.setCommand(Command.ReadDeliveries);
				   
				   GottenDatabase = dbController.ReadFromDB(data);
				   ArrayList<Delivery> deliveries = new ArrayList<>();
				   
				   for (Object obj : GottenDatabase) {
					   deliveries.add((Delivery) obj);
				   }
				   
				   response.setContent(deliveries);
				  
				   client.sendToClient(response);
				   break;
				   
			    case ReadOrders:
					   response.setCommand(Command.ReadOrders);
					   
					   GottenDatabase = dbController.ReadFromDB(data);
					   ArrayList<Order> orders = new ArrayList<>();
					   
					   for (Object obj : GottenDatabase) {
						   orders.add((Order) obj);
					   }
					   
					   response.setContent(orders);
					  
					   client.sendToClient(response);
					   break;
					   
			    case ReadRequests:
					   response.setCommand(Command.ReadRequests);
					   
					   GottenDatabase = dbController.ReadFromDB(data);
					   ArrayList<Request> requests = new ArrayList<>();
					   
					   for (Object obj : GottenDatabase) {
						   requests.add((Request) obj);
					   }
					   
					   response.setContent(requests);
					   client.sendToClient(response);
					   break;
					   
			    case ReadItems:
			    		response.setCommand(Command.ReadItems);
			    		GottenDatabase = dbController.ReadFromDB(data);
						   ArrayList<Item> items = new ArrayList<>();
						   
						   for (Object obj : GottenDatabase) {
							   items.add((Item) obj);
						   }
						   
						   response.setContent(items);
						   client.sendToClient(response);
						   break;
					  
			    case ReadUsers:
			    		response.setCommand(Command.ReadUsers);
			    		GottenDatabase = dbController.ReadFromDB(data);
			    		ArrayList<Subscriber> user = new ArrayList<>();
						   
						   for (Object obj : GottenDatabase) {
							   user.add((Subscriber) obj);
						   }
						   response.setContent(user);
						   client.sendToClient(response);
						   break;
						   
			    case InsertOrder:
		    		response.setCommand(Command.InsertOrder);
		    		response.setContent(0);
		    		dbController.SaveToDB(data);
					client.sendToClient(response);
					break;
					
			    case InsertOrderReport:
			    	response.setCommand(Command.InsertOrderReport);
			    	response.setContent(0);
			    	dbController.SaveToDB(data);
			    	client.sendToClient(response);
			    	break;
			    	
			    case InsertInventoryReport:
					response.setCommand(Command.InsertInventoryReport);
					response.setContent(0);
					dbController.SaveToDB(data);
			    	client.sendToClient(response);
			    	break;
						  
			    case ReadLocations:
			    	response.setCommand(Command.ReadLocations);
			    	GottenDatabase = dbController.ReadFromDB(data);
			    	ArrayList<Location> locations = new ArrayList<>();
			    	
			    	for (Object obj : GottenDatabase)
			    		locations.add((Location) obj);
			    	
			    	response.setContent(locations);
			    	client.sendToClient(response);
			    	break;

			    	
			    case ReadOrdersReports:
			    	response.setCommand(Command.ReadOrdersReports);
			    	GottenDatabase = dbController.ReadFromDB(data);
			    	
			    	ArrayList<OrdersReports> reports = new ArrayList<>();
			    	for(Object obj : GottenDatabase)
			    		reports.add((OrdersReports)obj);
			    	
			    	response.setContent(reports);
			    	client.sendToClient(response);
			    	break;

			    default:
			    		break;  // add functionality
		 }  
	  } catch(SQLException e) {e.printStackTrace();}
  }
}
//End of EchoServer class
