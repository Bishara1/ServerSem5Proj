package databaselogic;

import java.sql.SQLException;
import java.util.ArrayList;

import common.Message;

public interface DBInterface {
	
	Object SaveToDB(Object message) throws SQLException;
	void UpdateToDB(Message details) throws SQLException;
	ArrayList<Object> ReadFromDB(Message m) throws SQLException;
	String[] ConnectToServer(String str , int flag) throws SQLException;
}
