package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class Server_controller implements javafx.fxml.Initializable{
	private ServerSocket socket;
	private static int port = 8060;
	private Socket connection;
	private HashMap<String,Socket> map = new HashMap<String,Socket>();
	private ArrayList<String> nicks = new ArrayList<String>();
	
	@FXML private TextArea Chat_Area = new TextArea();
	@FXML private TextField message_txtfield = new TextField();
	@FXML private ListView<String> nick_list = new ListView<String>();
	
	//Send Messages
	@FXML
	private void sendmessages() {
		String message = message_txtfield.getText();
		for(Socket s:map.values()){
			PrintWriter pc;
			try {
				pc = new PrintWriter(s.getOutputStream());
				pc.println("SERVER: " + message);
				pc.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	//Close Program
	public void closewindow(){
		PrintWriter closer = null;
		for(Socket s:map.values()){
			try {
				closer = new PrintWriter(s.getOutputStream());
				closer.println("******SERVER CLOSING******");
				closer.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.exit(0);
			
		}
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		Thread main = new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try{
					socket = new ServerSocket(port);
					while(true){
						System.out.println("Waiting for connections at port " + port);
							connection = socket.accept();
							System.out.println("Connected to " + connection.getInetAddress().getHostName() + " at " + connection.getPort());
							Thread t1 = new Thread(new Runnable(){
								@Override
								public void run() {
									// TODO Auto-generated method stub
									ProcessUser(connection);
								}
								
							});
							t1.start();
					}

				}catch(IOException e){
					e.printStackTrace();
				}			
			}			
		});
		main.setDaemon(true);
		main.start();

	}

	private void ProcessUser(Socket sock){
		BufferedReader br = null;
		PrintWriter pr = null;
		PrintWriter pc = null;
		Boolean b = true;
		try {
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			pr = new PrintWriter(sock.getOutputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String Message = "";
		String nick = "";
		try{
			while(true){
				b = br.ready();
				if(b){
					nick = br.readLine();
					if(nicks.contains(nick)){
						pr.println("Nick Already Taken!!");
						pr.flush();
						continue;
					}
					else{
						pr.println("Nick Accepted!!");
						pr.flush();
						nicks.add(Message);
						System.out.println(nick + " connected!!");
						break;
					}
				}
			}
			for(Socket s:map.values()){
				pc = new PrintWriter(s.getOutputStream());
				pc.println("/$ADD" + nick + "$/");
				pc.flush();
				System.out.println("/$ADD" + nick + "$/");
			}
			map.put(nick,connection);
			nicks.add(nick);
			Platform.runLater(() -> {
				nick_list.getItems().add(nicks.get(nicks.size()-1));
			});
			for(String s:nicks){
				if(!s.equals("")){
					pr.println("/$ADD" + s + "$/");
					pr.flush();
					Thread.sleep(100);
					System.out.println(s);
				}				
			}
			
			while(true){
				if(br.ready()){
					Message = br.readLine();
					System.out.println(Message);
					String Usernick ="";
					if(Message.startsWith("@") && Message.endsWith("0")){
						Message = Message.substring(1,Message.length()-1);
						if(nicks.contains(Message)){							
								Usernick = Message;
								PrintWriter pm_pr = new PrintWriter(map.get(Usernick).getOutputStream());
								pm_pr.println("@" + nick);
								pm_pr.flush();
						}
						else{
							pr.println("****USER IS NOT ONLINE****");
							pr.flush();
						}
					}					
					else if(Message.startsWith("@PMMES(Y)/")){
						Usernick = Message.substring(10);
						PrintWriter pm_pr = new PrintWriter(map.get(Usernick).getOutputStream());
						pm_pr.println("PM REQUEST  TO " + nick + " ACCEPTED");
						pm_pr.flush();
					}
					else if(Message.startsWith("@PMMES(N)/")){
						Usernick = Message.substring(10);
						PrintWriter pm_pr = new PrintWriter(map.get(Usernick).getOutputStream());
						pm_pr.println("PM REQUEST  TO " + nick + " DECLINED");
						pm_pr.flush();
					}
					else if(Message.endsWith("h825#/")){
						Usernick = Message.split("h825#/")[1];
						PrintWriter pm_pr = new PrintWriter(map.get(Usernick).getOutputStream());
						pm_pr.println(Message);
						pm_pr.flush();
					}
					else if(Message.startsWith("$exit@" + nick)){
						final String rem_nick = nick;
						nicks.remove(nick);
						map.remove(rem_nick);
						Platform.runLater(() -> {
							nick_list.getItems().remove(rem_nick);
						});
						for(Socket s:map.values()){
							pc = new PrintWriter(s.getOutputStream());
							pc.println("/$DELETE" + nick + "$/");
							pc.flush();
						}
						System.out.println(rem_nick + " Disconnected.");
						break;
						
					}
					else{
						Chat_Area.appendText(Message + "\n");
						for(Socket s:map.values()){
							pc = new PrintWriter(s.getOutputStream());
							pc.println(Message);
							pc.flush();
						}
					}
				}
			}
		}catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				sock.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}		
	}
}














