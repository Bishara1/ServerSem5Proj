package gui_server;

import java.net.URL;
import java.util.ResourceBundle;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import logic.Connected;
import server.EchoServer;

public class ServerInfoController implements Initializable {
	
	@FXML
	private TextField serverIptxt;
	@FXML
	private TextField serverPortxt;
	@FXML
	private PasswordField databasePasswordtxt;
	
	@FXML
	private TableView<Connected> table;
	@FXML
	public TableColumn<Connected, String> colIp;
	@FXML
	public TableColumn<Connected, String> colHost;
	@FXML
	public TableColumn<Connected, String> colStatus;
	
	private ObservableList<Connected> data;
	
	
	public void start(Stage primaryStage) throws Exception {
		// get port and initialize port text field
		String port = Integer.toString(EchoServer.DEFAULT_PORT);
		
		//FXMLLoader loader = new FXMLLoader();
		Parent root = FXMLLoader.load(getClass().getResource("/gui_server/ServerInfo.fxml"));
		
		Scene scene = new Scene(root);
		primaryStage.setTitle("Server Info");
		primaryStage.setScene(scene);
		
		primaryStage.show();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		serverIptxt.setText(EchoServer.getLocalIp());  // Set current ip
		serverPortxt.setText("5555");
		databasePasswordtxt.setText("Bv654gF11!");
		LoadTable();

		/*
		 * data.addListener(new ListChangeListener<Connected>() {
		 * 
		 * @Override public void onChanged(Change<? extends Connected> c) { // TODO
		 * Auto-generated method stub initialize(null, null); }
		 * 
		 * });
		 */	}

//		data.addListener(new ListChangeListener<Connected>() {
//
//			@Override
//			public void onChanged(Change<? extends Connected> c) {
//				// TODO Auto-generated method stub
//				initialize(null, null);
//			}
//			
//		});
	
	
	public void RunServerBtn() {
		// on click, change button color to #373057
		if (serverPortxt.getText().equals("")) {
			System.out.println("Please Enter Port");
			return;
		}
		
		String[] args = {serverPortxt.getText(), databasePasswordtxt.getText()};
		EchoServer.runServer(args);
	}
	
	public void RefreshClientsBtn() {
		table.getItems().clear();
		LoadTable();
		data = FXCollections.observableArrayList(EchoServer.users);
		table.setItems(data);
	}
	
	public void LoadTable() {
		colIp.setCellValueFactory(new PropertyValueFactory<>("Ip"));
		colHost.setCellValueFactory(new PropertyValueFactory<>("Host"));
		colStatus.setCellValueFactory(new PropertyValueFactory<>("Status"));
	}
	
	public void QuitBtn() {
		System.exit(0);
	}
}
