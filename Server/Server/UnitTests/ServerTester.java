package UnitTests;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import common.Command;
import common.Message;
import databaselogic.DatabaseController;
import logic.Connected;
import logic.InventoryReports;
import logic.Machine;
import server.EchoServer;
import server.ReportsThread;

class ServerTester {
	
	DatabaseController db;
	EchoServer server;
	ReportsThread thread;

	@BeforeEach
	void setUp() throws Exception {
		db = DatabaseController.GetFunctionsInstance("sfnhli147258!");
		server.users = new ArrayList<>();
		server = new EchoServer(5555);
		server.setDbController(db);
		server.setConnectedToDatabase(true);
		thread = new ReportsThread(db);
	}

	
	//****************************************************************************************************************************
	//----------------------------------------------------------------------------------------------------------------------------
	// LOGIN UNIT TESTS
	//----------------------------------------------------------------------------------------------------------------------------
	//****************************************************************************************************************************
	
	@Test
	void NullIPUsernameLogin()
	{
		String[] usernamePassword = {"mc","123456"};
		Message msg = new Message(usernamePassword,Command.Connect);
		try {
			String[] details = server.getConnectDetails(msg, null);
		}catch( NullPointerException e) {
			assertTrue(true);
			return;
		}
		assertFalse(true);
		return;
		
	}
	
	@Test
	void NullDetailsUsernameLogin()
	{
		String[] usernamePassword = null;
		Message msg = new Message(usernamePassword,Command.Connect);
		try {
			String[] details = server.getConnectDetails(msg, "17.0.0.24");
		}catch( NullPointerException e) {
			assertTrue(true);
			return;
		}
		assertFalse(true);
		return;
		
	}
	
	@Test
	void NullIPEKTLogin()
	{
		String ID = "123456789";
		Message msg = new Message(ID,Command.Connect);
		try {
			String[] details = server.getEKTConnectDetails(msg, null);
		}catch( NullPointerException e) {
			assertTrue(true);
			return;
		}
		assertFalse(true);
		return;
		
	}
	
	@Test
	void NullDetailsEKTLogin()
	{
		String ID = null;
		Message msg = new Message(ID,Command.Connect);
		String[] details = server.getEKTConnectDetails(msg, "17.0.0.24");
		
		assertEquals(details[0],"-1");
		assertEquals(details[1],"-1");
		assertEquals(details[2],"-1");
		assertEquals(details[3],"-1");
		assertEquals(details[4],"-1");
		assertEquals(details[5],"-1");
		
	}
	
	@Test
	void SuccessfulUsernameLogin() {
		
		boolean flag = false;
		String[] usernamePassword = {"mc","123456"};
		Message msg = new Message(usernamePassword,Command.Connect);
		String[] details = server.getConnectDetails(msg, "17.0.0.24");
		
		assertEquals(details[0],"123456");
		assertEquals(details[1],"customer");
		assertEquals(details[2],"mark");
		assertEquals(details[3],"-1");
		assertEquals(details[4],"0");
		assertEquals(details[5],"12365464");
		
		for (Connected Client : server.users) {
			  if (Client.getConnectedUserID() == Integer.parseInt(details[5]) && Client.getStatus().equals("Connected") && Client.getIp().equals("17.0.0.24")) {
				  flag = true;
			  }
	  	  }
		if(flag)
			assertTrue(true);
		else
			assertFalse(true);
		
	}
	
	@Test
	void SuccessfulEKTLogin() {
		
		boolean flag = false;
		String ID = "123456789";
		Message msg = new Message(ID,Command.Connect);
		String[] details = server.getEKTConnectDetails(msg, "17.0.0.24");
		
		assertEquals(details[0],"123456");
		assertEquals(details[1],"ceo");
		assertEquals(details[2],"Leen");
		assertEquals(details[3],"1254");
		assertEquals(details[4],"0");
		assertEquals(details[5],"123456789");
		
		for (Connected Client : server.users) {
			  if (Client.getConnectedUserID() == Integer.parseInt(details[5]) && Client.getStatus().equals("Connected") && Client.getIp().equals("17.0.0.24")) {
				  flag = true;
			  }
	  	  }
		if(flag)
			assertTrue(true);
		else
			assertFalse(true);
		
	}
	
	@Test
	void UserAlreadyConnectedUsernameLogin()
	{
		
		String[] usernamePassword = {"mc","123456"};
		Message msg = new Message(usernamePassword,Command.Connect);
		String[] details = server.getConnectDetails(msg, "17.0.0.24");
		
		details = server.getConnectDetails(msg, "17.0.0.24");
		
		assertEquals(details[0],"User already connected");
		assertEquals(details[1],"customer");
		assertEquals(details[2],"mark");
		assertEquals(details[3],"-1");
		assertEquals(details[4],"0");
		assertEquals(details[5],"12365464");
	}
	
	@Test
	void UserAlreadyConnectedEKTLogin()
	{
		String ID = "123456789";
		Message msg = new Message(ID,Command.Connect);
		String[] details = server.getEKTConnectDetails(msg, "17.0.0.24");
		
		details = server.getEKTConnectDetails(msg, "17.0.0.24");
		
		assertEquals(details[0],"User already connected");
		assertEquals(details[1],"ceo");
		assertEquals(details[2],"Leen");
		assertEquals(details[3],"1254");
		assertEquals(details[4],"0");
		assertEquals(details[5],"123456789");
	}
	
	@Test
	void IncorrectPasswordUsernameLogin()
	{
		String[] usernamePassword = {"mc","1"};
		Message msg = new Message(usernamePassword,Command.Connect);
		String[] details = server.getConnectDetails(msg, "17.0.0.24");
		
		assertEquals(details[0],"-1");
		assertEquals(details[1],"-1");
		assertEquals(details[2],"-1");
		assertEquals(details[3],"-1");
		assertEquals(details[4],"-1");
		assertEquals(details[5],"-1");
	}
	
	@Test
	void SuccessfulUsernameLoginAfterDisconnect()
	{
		boolean changed = true;
		boolean found = false;
		server.users.add(new Connected("17.0.0.24","5555","Disconnected",12365464));
		String[] usernamePassword = {"mc","123456"};
		Message msg = new Message(usernamePassword,Command.Connect);
		String[] details = server.getConnectDetails(msg, "17.0.0.24");
		
		assertEquals(details[0],"123456");
		assertEquals(details[1],"customer");
		assertEquals(details[2],"mark");
		assertEquals(details[3],"-1");
		assertEquals(details[4],"0");
		assertEquals(details[5],"12365464");
		
		for (Connected Client : server.users) {
			  if (Client.getConnectedUserID() == Integer.parseInt(details[5]) && Client.getStatus().equals("Connected") && Client.getIp().equals("17.0.0.24")) {
				  found = true;
			  }
			  if (Client.getConnectedUserID() == Integer.parseInt(details[5]) && Client.getStatus().equals("Disconnected") && Client.getIp().equals("17.0.0.24")) {
				  changed = false;
			  }
			  
	  	  }
		if(found && changed)
			assertTrue(true);
		else
			assertFalse(true);
	}
	
	@Test
	void SuccessfulEKTLoginAfterDisconnect()
	{
		boolean changed = true;
		boolean found = false;
		server.users.add(new Connected("17.0.0.24","5555","Disconnected",12365464));
		String ID = "123456789";
		Message msg = new Message(ID,Command.Connect);
		String[] details = server.getEKTConnectDetails(msg, "17.0.0.24");
		
		assertEquals(details[0],"123456");
		assertEquals(details[1],"ceo");
		assertEquals(details[2],"Leen");
		assertEquals(details[3],"1254");
		assertEquals(details[4],"0");
		assertEquals(details[5],"123456789");
		
		for (Connected Client : server.users) {
			  if (Client.getConnectedUserID() == Integer.parseInt(details[5]) && Client.getStatus().equals("Connected") && Client.getIp().equals("17.0.0.24")) {
				  found = true;
			  }
			  if (Client.getConnectedUserID() == Integer.parseInt(details[5]) && Client.getStatus().equals("Disconnected") && Client.getIp().equals("17.0.0.24")) {
				  changed = false;
			  }
			  
	  	  }
		if(found && changed)
			assertTrue(true);
		else
			assertFalse(true);
	}
	
	@Test
	void UsernameIsntInDB()
	{
		String[] usernamePassword = {"Godzilla","123456"};
		Message msg = new Message(usernamePassword,Command.Connect);
		String[] details = server.getConnectDetails(msg, "17.0.0.24");
		
		assertEquals(details[0],"-1");
		assertEquals(details[1],"-1");
		assertEquals(details[2],"-1");
		assertEquals(details[3],"-1");
		assertEquals(details[4],"-1");
		assertEquals(details[5],"-1");
	}
	
	@Test
	void IDIsntInDB()
	{
		String ID = "12";
		Message msg = new Message(ID,Command.Connect);
		String[] details = server.getEKTConnectDetails(msg, "17.0.0.24");
		
		assertEquals(details[0],"-1");
		assertEquals(details[1],"-1");
		assertEquals(details[2],"-1");
		assertEquals(details[3],"-1");
		assertEquals(details[4],"-1");
		assertEquals(details[5],"-1");
	}
	
	//****************************************************************************************************************************
	//----------------------------------------------------------------------------------------------------------------------------
	// INVENTORY REPORT CREATION AND VIEW UNIT TESTS
	//----------------------------------------------------------------------------------------------------------------------------
	//****************************************************************************************************************************
	
	@Test
	void ReadNullReportDetails()
	{
		//ReadFromDB the November 2022 inventory report
		ArrayList<Object> check = new ArrayList<>();
		Message msg = new Message(null,Command.ReadInventoryReports);
		try {
			check = db.ReadFromDB(msg);
		} catch (SQLException | NullPointerException e) {
			assertTrue(true);
			return;
		}
		assertFalse(true);
		
	}
	
	@Test
	void ReadNonExistentReport()
	{
		//ReadFromDB the November 2022 inventory report
		ArrayList<Object> check = new ArrayList<>();
		Message msg = new Message(16,Command.ReadInventoryReports);
		try {
			check = db.ReadFromDB(msg);
		} catch (SQLException e) {
			e.printStackTrace();
			assertFalse(true);
			return;
		}
		assertTrue(check.isEmpty());
		return;
		
	}
	
	@Test
	void ReadSpecificReport()
	{
		//ReadFromDB the November 2022 inventory report
		InventoryReports expectedReport = new InventoryReports("North","bamba,100.bisli,300.cola,100.bueno,300.pringles,50.","bamba,90.bisli,100.cola,70.bueno,250.pringles,30.","11","2022",2,1);
		ArrayList<Object> check = new ArrayList<>();
		Message msg = new Message(2,Command.ReadInventoryReports);
		try {
			check = db.ReadFromDB(msg);
		} catch (SQLException e) {
			e.printStackTrace();
			assertFalse(true);
			return;
		}
		InventoryReports temp = (InventoryReports)check.get(0);
		assertEquals(expectedReport,temp);
		return;
		
	}
	
	@Test
	void ReadAllReports()
	{
		ArrayList<Object> check = new ArrayList<>();
		Message msg = new Message(0,Command.ReadInventoryReports);
		try {
			check = db.ReadFromDB(msg);
		} catch (SQLException e) {
			e.printStackTrace();
			assertFalse(true);
			return;
		}
		assertEquals(2,check.size());
		return;
	}
	
	@Test
	void InsertNullParamaterInventoryReport()
	{
		ArrayList<Object> check = new ArrayList<>();
		Message msg = new Message(0,Command.ReadInventoryReports);
		thread.insertNewInventoryReport(null, null, null, null, null);
		try {
			check = db.ReadFromDB(msg);
		} catch (SQLException e) {
			e.printStackTrace();
			assertFalse(true);
			return;
		}
		assertEquals(check.size(),3);
		return;
	}
	
	@Test
	void InsertInvalidDetailsInventoryReports()
	{
		Machine machine = new Machine(1,4,68,"North","mango,bamba,bisli,cola,bueno,pringles","8,0,15,9,14,22");
		thread.insertNewInventoryReport(machine,"-4","1399","-3","1400");
		ArrayList<Object> check = new ArrayList<>();
		Message msg = new Message(0,Command.ReadInventoryReports);
		try {
			check = db.ReadFromDB(msg);
		} catch (SQLException e) {
			e.printStackTrace();
			assertFalse(true);
			return;
		}
		assertEquals(check.size(),3);
		return;
	}
	
	@Test
	void InsertValidInventoryReport()
	{
		Machine machine = new Machine(1,4,68,"North","mango,bamba,bisli,cola,bueno,pringles","8,0,15,9,14,22");
		Object result = thread.insertNewInventoryReport(machine,"12","2022","1","2023");
		ArrayList<Object> check = new ArrayList<>();
		Message msg = new Message(4,Command.ReadInventoryReports);
		try {
			check = db.ReadFromDB(msg);
		} catch (SQLException e) {
			e.printStackTrace();
			assertFalse(true);
			return;
		}
		InventoryReports expected = new InventoryReports("North","bamba,25.bisli,251.cola,50.bueno,230.pringles,1.","mango,8.bamba,0.bisli,15.cola,9.bueno,14.pringles,22.","1","2023",4,1);
		assertEquals((InventoryReports)check.get(0),expected);
	}
	
	
	
	
	

}
