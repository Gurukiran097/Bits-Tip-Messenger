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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

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
		message_txtfield.setText("");
		Platform.runLater(() -> {
			Chat_Area.appendText("SERVER: " + message + "\n");
		});
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
	
	public void keyboard_send(KeyEvent e){
		if((e.getCode() == KeyCode.ENTER) && !(e.isControlDown())){
			sendmessages();
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
				System.out.println("Message sent ");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
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
				if(br.ready()){
					nick = br.readLine();
					if(nicks.contains(nick)){
						pr.println("Nick Already Taken!!");
						pr.flush();						
					}
					else{
						pr.println("Nick Accepted!!");
						pr.flush();
						nicks.add(Message);
						System.out.println(nick + " connected!!");
						break;
					}
				}
				else{
					Thread.sleep(100);
				}
			}
			for(Socket s:map.values()){
				pc = new PrintWriter(s.getOutputStream());
				pc.println("/$ADD" + nick + "$/");
				pc.flush();
				System.out.println("/$ADD" + nick + "$/");
			}
			Thread.sleep(100);
			map.put(nick,connection);
			nicks.add(nick);
			Platform.runLater(() -> {
				nick_list.getItems().add(nicks.get(nicks.size()-1));
			});
			for(String s:nicks){
				if(!s.equals("")){
					pr.println("/$ADD" + s + "$/");
					pr.flush();//1555555555555555555555555555555555555
					System.out.println(s);
				}				
			}
			
			while(true){
				if(br.ready()){
					Message = br.readLine();
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
						pm_pr.println("PM REQUEST TO " + nick + " ACCEPTED");
						pm_pr.flush();
					}
					else if(Message.startsWith("@PMMES(N)/")){
						Usernick = Message.substring(10);
						PrintWriter pm_pr = new PrintWriter(map.get(Usernick).getOutputStream());
						pm_pr.println("PM REQUEST TO " + nick + " DECLINED");
						pm_pr.flush();
					}
					else if(Message.endsWith("h825#/")){
						System.out.println(Message.split(" ")[1].split("h825#/")[0]);
						if(Message.split(" ")[1].split("h825#/")[0].equals("?<PMCHATCLOSING/>?")){
							System.out.println("***** CLOSING CLOSING *****");
							Usernick = Message.split("h825#/")[1];
							PrintWriter pm_pr = new PrintWriter(map.get(Usernick).getOutputStream());
							pm_pr.println("*****PRIVATE CHAT HAS BEEN TERMINATED*****h825#/" + Usernick + "h825#/");
							pm_pr.flush();
						}
						else {
							Usernick = Message.split("h825#/")[1];
							PrintWriter pm_pr = new PrintWriter(map.get(Usernick).getOutputStream());
							pm_pr.println(Message);
							pm_pr.flush();
						}
						
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
						String m = Message;
						Platform.runLater(() -> {
							Chat_Area.appendText(m + "\n");
						});					
						for(Socket s:map.values()){
							pc = new PrintWriter(s.getOutputStream());
							pc.println(Message);
							pc.flush();
						}
						System.out.println(Message);
					}
				}
				else{
					Thread.sleep(150);
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














