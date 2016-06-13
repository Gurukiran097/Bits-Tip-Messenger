package test_client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class pm_controller implements javafx.fxml.Initializable{
	
	private String Snick = "";
	private String Rnick = "";
	private Client co = new Client();
	private Socket sock = co.ret_sock();
	private PrintWriter pr = null;
	
	
	@FXML private Button send_button = new Button();
	@FXML private TextArea Pm_Chat_Area = new TextArea();
	@FXML private TextField pm_message_txtfield = new TextField();
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		System.out.println("Pm started with " + Rnick);
		try{
			pr = new PrintWriter(sock.getOutputStream());
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	
	public void setRNick(String Rnick){
		this.Rnick = Rnick;
	}
	
	public String getRNick(){
		return Rnick;
	}
	public void setSNick(String Snick){
		this.Snick = Snick;
	}
	
	public String getSNick(){
		return Snick;
	}
	
	public String msg_send(){
		String Message = pm_message_txtfield.getText();
		Message = Message + "h825#/" + Rnick + "h825#/";
		pm_message_txtfield.setText("");
		return Message;
	}
	public void keyboard_pm(KeyEvent e){
		if((e.getCode() == KeyCode.ENTER) && !(e.isControlDown())){
			pm_send();
		}
	}
	public void pm_send(){
		try{
			String message = pm_message_txtfield.getText();
			pr.println(Snick + ": " + message + "h825#/" + Rnick + "h825#/");
			pr.flush();
			Pm_Chat_Area.appendText("\n" + Snick + ": " + message);
			pm_message_txtfield.setText("");
		}catch(Exception e){
			System.exit(0);
		}
		
	}
	public void Closepm(){
		pr.println(Snick + ":?<PM CHAT CLOSING/>?" + "h825#/" + Rnick + "h825#/");
		pr.flush();
		pr.close();
	}
	
	
	public void appendtxt(String text){
		Pm_Chat_Area.appendText(text);
	}
}
