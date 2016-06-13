package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class Client extends Application implements javafx.fxml.Initializable{
	private static Socket sock;
	private static BufferedReader br = null;
	private static PrintWriter pr = null;
	private final int port = 8060;
	private String nick = "";
	private Stage Window = new Stage();
	private String pm_nick = "";
	private String message = "";
	private HashMap<String,String> pm_list = new HashMap<String,String>();
	int a = 1,b = 0;
		
	@FXML private TextField username_field;
	@FXML private Button login_button;
	@FXML private Label login_status;
	@FXML private TextField message_txtfield;
	@FXML private TextArea Chat_Area;
	@FXML private Button send_button;
	@FXML private ListView<String> nick_list = new ListView<String>();
	@FXML private Label status_label;
	
	
	
	//Login button action code Start
	public void login(){
		Connect();
		if(login_status.getText().equals("")){
			FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/test_client/Chat_Window.fxml"));
			loader2.setController(this);
			Parent root;
			try {
				root = (Parent)loader2.load();
				Scene scene = new Scene(root);
				Window.setTitle("Hello " + nick);
				Window.setScene(scene);
				Window.setResizable(true);			
				Thread t1 = new Thread(new Runnable(){
					
					@Override
					public void run() {
						// TODO Auto-generated method stub			
						while(true){
							try {
								if(br.ready()){
									message = br.readLine();
									System.out.println(message);
									//PM ADDITION START
									if(message.equals("******SERVER CLOSING******")){
										System.out.println("SERVER CLOSING");
										Chat_Area.appendText("******SERVER CLOSING******");
										message_txtfield.setText("");
										message_txtfield.setEditable(false);
										send_button.disableProperty();
										sock.close();
										try {
											Thread.sleep(500);
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										
									}
									else if(message.startsWith("/$") && message.endsWith("$/")){
										if(message.startsWith("/$ADD")){
											String nik = message;
											Platform.runLater(() -> {
												nick_list.getItems().add(nik.substring(5, nik.length()-2));
											});
										}
										else if(message.startsWith("/$DELETE")){
											String nik = message;
											Platform.runLater(() -> {
												nick_list.getItems().remove(nik.substring(8, nik.length()-2));
											});
											System.out.println(message);
										}
										
									}
									else if(message.endsWith("h825#/")){
										pm_list.put(findnick(message), message);
										continue;
									}
									else if(message.startsWith("@")){
										
										
										pr = new PrintWriter(sock.getOutputStream());
										pm_nick = message.substring(1);
										pr.flush();
										String pm_mes = "";
										pm_list.put(pm_nick, pm_mes);
										Platform.runLater(() -> {
											/*JOptionPane pane = new JOptionPane("Do you want to have a private conversation with " + pm_nick + "?",JOptionPane.QUESTION_MESSAGE,JOptionPane.YES_NO_OPTION);;
											JDialog dialog = pane.createDialog(null,"Private Message");
											dialog.setModal(false);
											dialog.setVisible(true);*/
											/*b = JOptionPane.showConfirmDialog(null,"Do you want to have a private conversation with " + pm_nick + "?");*/
											/*JPanel panel = new JPanel();
											JLabel label = new JLabel("Do you want to have a private conversation with " + pm_nick + "?");
											JButton yes = new JButton("Yes");
											JButton no = new JButton("No");
											yes.addActionListener(e -> {
												b = 1;
												panel.setVisible(false);
											});
											no.addActionListener(e -> {
												b = 0;
												panel.setVisible(false);
											});
											panel.add(label);
											panel.add(yes);
											panel.add(no);
											panel.setVisible(true);*/
											
											/*b = pm_confirm.display("Personal Message", "Do you want to have a private conversation with " + pm_nick + "?");*/
											while(true){
												if(b == JOptionPane.YES_OPTION){
													pr.println("@PMMES(Y)/" + pm_nick);
													pr.flush();
													FXMLLoader pmloader = new FXMLLoader(getClass().getResource("/test_client/Pm_Window.fxml"));
													pm_controller controller = new pm_controller();
													controller.setRNick(pm_nick);
													controller.setSNick(nick);
													pmloader.setController(controller);
													try {
														Parent pmroot = (Parent)pmloader.load();
														Stage stage = new Stage();
														stage.setScene(new Scene(pmroot));
														stage.setTitle("PM " + nick + " -> " + pm_nick);
														stage.setOnCloseRequest(e -> {
															e.consume();
															controller.Closepm();
														});
														stage.show();
														Thread pmrthr = new Thread(new Runnable(){
	
															@Override
															public void run() {
																// TODO Auto-generated method stub
																while(true){
																	try {
																		Thread.sleep(100);
																	} catch (InterruptedException e) {
																		// TODO Auto-generated catch block
																		e.printStackTrace();
																	}
																	if(!pm_list.get(controller.getRNick()).equals("")){
																		if(pm_list.get(controller.getRNick()).split("h825#/")[0].equals("*****PRIVATE CHAT HAS BEEN TERMINATED*****")){
																			controller.appendtxt(pm_list.get(controller.getRNick()).split("h825#/")[0] + "\n");
																			System.out.println("ITS TRUE");
																			pm_list.put(controller.getRNick(), "");
																			stage.setOnCloseRequest(e -> {
																				stage.close();
																			});
																			controller.disable();
																		}
																		else{
																			controller.appendtxt(pm_list.get(controller.getRNick()).split("h825#/")[0] + "\n");
																			pm_list.put(controller.getRNick(), "");
																		}
																		
																	}
																}
																
															}
															
														});
														pmrthr.start();
														break;
													} catch (Exception e) {
														// TODO Auto-generated catch block
														System.out.println("Came back to the start 3");
														e.printStackTrace();
														
													}
												}else if(b == JOptionPane.NO_OPTION){
													System.out.println("Negative");
													pr.println("@PMMES(N)/" + pm_nick);
													pr.flush();
													break;
												}
												System.out.println("Waiting...");
												try {
													Thread.sleep(150);
												} catch (Exception e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
											}
										});

									}
									else if(message.startsWith("PM REQUEST")){
										if(message.endsWith("ACCEPTED")){
											String Usernick = message.split(" ")[3];
											String pm_mes = "";
											pm_list.put(Usernick, pm_mes);
											Platform.runLater(() -> {
												FXMLLoader pmloader = new FXMLLoader(getClass().getResource("/test_client/Pm_Window.fxml"));
												pm_controller controller = new pm_controller();
												controller.setRNick(Usernick);
												controller.setSNick(nick);
												pmloader.setController(controller);
												try {
													Parent pmroot = (Parent)pmloader.load();
													Stage stage = new Stage();
													stage.setScene(new Scene(pmroot));
													stage.setTitle("PM " + nick + " -> " + Usernick);
													stage.setOnCloseRequest(e -> {
														e.consume();
														controller.Closepm();
														stage.close();
													});
													stage.show();
													
													Thread pmrthr = new Thread(new Runnable(){

														@Override
														public void run() {
															// TODO Auto-generated method stub															
															while(true){
																try {
																	Thread.sleep(100);
																} catch (InterruptedException e) {
																	// TODO Auto-generated catch block
																	e.printStackTrace();
																}
																if(pm_list.get(controller.getRNick()).endsWith("h825#/")){
																	System.out.println(pm_list.get(controller.getRNick()).split("h825#/")[0]);
																	if(pm_list.get(controller.getRNick()).split("h825#/")[0].equals("*****PRIVATE CHAT HAS BEEN TERMINATED*****")){
																		controller.appendtxt(pm_list.get(controller.getRNick()).split("h825#/")[0] + "\n");
																		System.out.println("ITS TRUE");
																		pm_list.put(controller.getRNick(), "");
																		stage.setOnCloseRequest(e -> {
																			stage.close();
																		});
																		controller.disable();
																	}
																	else{
																		controller.appendtxt(pm_list.get(controller.getRNick()).split("h825#/")[0] + "\n");
																		pm_list.put(controller.getRNick(), "");
																	}
																	
																}
															}
														}
														
													});
													pmrthr.start();
												} catch (Exception e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
											});
											

										}
										else if(message.endsWith("DECLINED")){
											Chat_Area.appendText(message + "\n");
										}
									}
									//PM ADDITION STOP
									else{
										/*changePmMes();*/
										Platform.runLater(() -> {
											Chat_Area.appendText(message + "\n");
											a++;
										});
										
									}
								}
								else{
									Thread.sleep(150);
								}
							} catch (IOException | InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					
				});
				t1.start();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	//Login button action code End
	//Mainwindow send button action code START
	public void send_message(){
		String message = message_txtfield.getText();
		if(message.startsWith("@") && message.endsWith("0")){
			pr.println(message);
			pr.flush();
		}
		else{
			pr.println(nick + ": " + message);
			pr.flush();
			System.out.println(nick + ": " + message);
		}		
		message_txtfield.setText("");
	}
	//Mainwindow send button action code END
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
	}
	//KeyBoard events
	//Login
	public void keyboard_log(KeyEvent e){
		if((e.getCode() == KeyCode.ENTER) && !(e.isControlDown())){
			login();
		}
	}
	//Send Message
	public void keyboard_send(KeyEvent e){
		if((e.getCode() == KeyCode.ENTER) && !(e.isControlDown())){
			send_message();
		}
	}
	//Connecting to Server
	public void Connect(){
		try {
			nick = username_field.getText();
			sock = new Socket("localhost",port);
			pr = new PrintWriter(sock.getOutputStream());
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));			
			String mes = "";
			if(!nick.equals("")){
				pr.println(nick);
				pr.flush();
				mes = br.readLine();
				if(mes.equals("Nick Already Taken!!")){
					username_field.setText("");
					login_status.setText("Nick Already taken");
				}
				else if(mes.equals("Nick Accepted!!")){
					login_status.setText("");
					}
				}
			else{
				login_status.setText("Please Enter a valid Nick");
			}	
			System.out.println(nick);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			login_status.setText("Server Offline");
		}
	}
	
	public Socket ret_sock(){
		return sock;
	}
	//Extracting nick from pm message
	private String findnick(String mes){
		int p;
		for(p = 0;p<mes.length();p++){
			if(mes.charAt(p) != ':'){
				continue;
			}
			else
				break;
		}
		return mes.substring(0,p);
	}
	//Closing the program
	public void closeprogram(){
		try{
			pr.println("$exit@" + nick);
			pr.flush();
		}catch(NullPointerException e){
			Window.close();
			System.exit(0);
		}
		Platform.runLater(() -> {
			try {
				Thread.sleep(500);
				sock.close();
				Window.close();
				System.exit(0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}


	@Override
	public void start(Stage stage) throws Exception {
		// TODO Auto-generated method stub
		Window = stage;
		FXMLLoader loader1 = new FXMLLoader(getClass().getResource("/test_client/Login.fxml"));
		loader1.setController(this);
		Parent root = (Parent)loader1.load();
		Scene scene = new Scene(root);
		Window.setOnCloseRequest(e -> {
			e.consume();
			closeprogram();
		});
		Window.setResizable(false);
		Window.setTitle("Enter your nick");
		Window.setScene(scene);
		Window.show();
	}
	public static void main(String[] args){
		launch(args);
	}
}
