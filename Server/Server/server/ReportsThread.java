package server;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import common.Command;
import common.Message;
import databaselogic.DatabaseController;
import javafx.scene.control.Alert;
import logic.InventoryReports;
import logic.Location;
import logic.Machine;
import logic.Order;
import logic.OrdersReports;
import logic.Subscriber;

public class ReportsThread implements Runnable {
	
	ArrayList<Location> locations = new ArrayList<Location>();
	DatabaseController db;
	String password;
	Message ReportmessageToServer = new Message(null, null);
	private int month,year;
	private ArrayList<String> newItems = new ArrayList<String>(); 
	
	public ReportsThread(DatabaseController db)
	{
		this.db = db;
	}
	
	@Override
	public void run() {
			try {
				//while(true) {
					if(LocalDate.now().getDayOfMonth() == 1) //should be 1
					{
						if(LocalDate.now().getMonthValue() == 1)
						{
							month = 12; //should be 12
							year = LocalDate.now().getYear()-1; //should be -1
						}
						else
						{
							month = LocalDate.now().getMonthValue()-1;
							year = LocalDate.now().getYear();
						}
						CreateOrderReports(month,year);
						CreateInventoryReports(month,year);
						CreateUserReports(month,year);
					}
					Thread.sleep(86400000); 
				//}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			
		
	}
	
	public void CreateOrderReports(int month,int year)
	{
		try {
			String monthStr = String.valueOf(month);
			String yearStr = String.valueOf(year);
			Message msg = new Message(0,Command.ReadLocations);
			ArrayList<Object> objects = null;
			objects = db.ReadFromDB(msg);
			for(Object obj : objects)
			{
				locations.add((Location)obj);
			}
			for(Location location : locations)
			{
			//we need a report for ALL locations
				OrdersReports currentReport;
				createNewOrderReport(location.getLocation(),monthStr,yearStr);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void createNewOrderReport(String location,String month,String year) {
			
			try {
				ArrayList<Order> orders = new ArrayList<Order>();
				ArrayList<Object> objects = new ArrayList<Object>();
				Message orderToServer = new Message(null,null);
				orderToServer.setCommand(Command.ReadOrders);
				orderToServer.setContent(0);
				objects = db.ReadFromDB(orderToServer);
				for(Object obj : objects)
				{
					orders.add((Order)obj);
				}
				String[] dateSplit= null;
				String things = "";
				boolean alertFlag = false;
				for (Order order : orders)
				{
					dateSplit = order.getOrder_created().toString().split("-");
					// if combo box selected values exist in the orders report
					if ( order.getLocation().equals(location)
							&& dateSplit[0].equals(year)
							&& Integer.parseInt(dateSplit[1]) == Integer.parseInt(month))
					{
						things += order.getItems_in_order();
						
						alertFlag = true;
					}
					
				}
				if (alertFlag)
				{
					scanOrder(things);
					ArrayList<String> NewReport = new ArrayList<String>(Arrays.asList("",location,fromArrayToString(newItems),month,year));
					orderToServer.setCommand(Command.InsertOrderReport);
					orderToServer.setContent(NewReport);
					db.SaveToDB(orderToServer);
				}
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
	
	public void scanOrder(String data) {
		String[] temp = data.split("\\.");
		
		for (String s : temp) {
			checkInReport(s);
		}
		
	}
	
	public void checkInReport(String data) {
		String[] nameAmount = data.split(",");
		String[] old = null;
		int newAmount;
		int index = checkContains(newItems,nameAmount[0]);
		if(index == -1) //checkContains
		{
			newItems.add(data);
		}
		else
		{
			old = newItems.get(index).split(",");
			newAmount = Integer.parseInt(nameAmount[1]) + Integer.parseInt(old[1]); //
			newItems.set(index, nameAmount[0] + "," + String.valueOf(newAmount));
		}
	}
	
	public int checkContains(ArrayList<String> arr, String s)
	{
		int size = arr.size();
		String[] split = null;
		for(int i = 0;i<size;i++)
		{
			split = arr.get(i).split(",");
			if(split[0].equals(s))
				return i;
		}
		return -1;
	}
	
	public String fromArrayToString(ArrayList<String> arr)
	{
		int size = arr.size();
		String str = "";
		for(int i = 0;i<size;i++)
		{
			str += arr.get(i);
			str += ".";
		}
		
		return str;
	}
	
	public void CreateInventoryReports(int month,int year)
	{
		try {
			int monthBefore,yearBefore;
			if(month == 1)
			{
				monthBefore = 12; //should be 12
				yearBefore = year-1; //should be -1
			}
			else
			{
				monthBefore = month-1;
				yearBefore = year-1;
			}
			String monthStr = String.valueOf(month);
			String yearStr = String.valueOf(year);
			String monthBeforeStr = String.valueOf(monthBefore);
			String yearBeforeStr = String.valueOf(yearBefore);
			String newData = "";
			String oldData = "";
			Message msg = new Message(null,null);
			Message orderToServer = new Message(null,null);
			ArrayList<Machine> machines = new ArrayList<Machine>();
			ArrayList<Object> objects = null;
			msg = new Message(null,null);
			msg.setCommand(Command.ReadMachines);
			msg.setContent(0);
			objects = db.ReadFromDB(msg);
			for(Object obj : objects)
			{
				machines.add((Machine)obj);
			}
			for(Machine machine : machines)
			{
				oldData = getOldItemsString(machine.getMachine_id(),monthBeforeStr,yearBeforeStr);
				newData = getNewItemsString(machine);
				orderToServer.setCommand(Command.InsertInventoryReport);
				ArrayList<String> newReport = new ArrayList<String>(Arrays.asList(String.valueOf(machine.getMachine_id()),machine.getLocation(),oldData,newData,monthStr,yearStr));
				orderToServer.setContent(newReport);
				db.SaveToDB(orderToServer);			
			}
			
			
			
			
			
		}catch(Exception e) { e.printStackTrace();}
		
	}
	
	public String getNewItemsString(Machine machine)
	{
		ArrayList<String> items = machine.getItems();
		ArrayList<Integer> amount = machine.getAmountItems();
		int size = items.size();
		String newItems = "";
		for(int i=0;i<size;i++)
		{
			newItems += items.get(i);
			newItems += ",";
			newItems += String.valueOf(amount.get(i));
			newItems += ".";
		}
		return newItems;
	}
	
	public String getOldItemsString(int machineId,String month,String year)
	{
		try {
			ArrayList<Object> objects = new ArrayList<Object>();
			Message m = new Message(null,null);
			m.setCommand(Command.ReadInventoryReports);
			m.setContent(0);
			objects = db.ReadFromDB(m);
			for(Object obj : objects)
			{
				if(((InventoryReports)obj).getMonth().equals(month) && ((InventoryReports)obj).getYear().equals(year) && ((InventoryReports)obj).getMachine_id() == machineId)
					return ((InventoryReports)obj).getStock();
			}
			return null;
			
			
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public void CreateUserReports(int month,int year)
	{
		try {
			String monthStr = String.valueOf(month);
			String yearStr = String.valueOf(year);
			int cnt0 = 0;
			int cnt1 = 0;
			int cnt2 = 0;
			int cnt3 = 0;
			int cnt4 = 0;
			int cnt5 = 0;
			ArrayList<Object> objects = new ArrayList<Object>();
			ArrayList<Order> orders = new ArrayList<Order>();
			ArrayList<Subscriber> users = new ArrayList<Subscriber>();
			Message messageToServer = new Message(null,null);
			Message orderToServer = new Message(null,null);
			messageToServer.setCommand(Command.ReadUsers);
			messageToServer.setContent(0);	
			objects = db.ReadFromDB(messageToServer);
			for(Object obj : objects)
			{
				users.add((Subscriber)obj);
			}
			
			messageToServer.setCommand(Command.ReadOrders);
			messageToServer.setContent(0);	
			objects = db.ReadFromDB(messageToServer); 
			for(Object obj : objects)
			{
				if(((Order)obj).getOrder_created().getMonth() == month && ((Order)obj).getOrder_created().getYear() == year)
					orders.add((Order)obj);
				//test if order is in the month thats specified
			}
			
			int ordersCount = 0;
			for (Subscriber user : users) {
				
				if(user.getRole().equals("customer"))
				{
					ordersCount = findUserOrdersCount(user.getId(),orders);
					
					if (ordersCount == 0)
						cnt0++;
					if ((ordersCount > 0) && (ordersCount <= 5))
						cnt1++;
					if ((ordersCount > 5) && (ordersCount <= 10))
						cnt2++;
					if ((ordersCount > 10) && (ordersCount <= 15))
						cnt3++;
					if ((ordersCount > 15) && (ordersCount <= 20))
						cnt4++;
					if ((ordersCount > 20) && (ordersCount <= 25))
						cnt5++;
				}
			}
			String data = cnt0 + "," + cnt1 + "," + cnt2 + "," + cnt3 + "," + cnt4 + "," + cnt5;
			orderToServer.setCommand(Command.InsertUsersReport);
			ArrayList<String> newReport = new ArrayList<String>(Arrays.asList(monthStr,yearStr,data));
			orderToServer.setContent(newReport);
			db.SaveToDB(orderToServer);
		}catch(SQLException e) { e.printStackTrace();}
	}
	
	public int findUserOrdersCount(int id,ArrayList<Order> orders) {
		int cnt = 0;
		
		for (Order i : orders) {
			if (i.getCustomer_id() == id)
				cnt++;
		}
		return cnt;
	}

	
	

}
