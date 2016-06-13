package Server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Server extends Application{

	@Override
	public void start(Stage stage) throws Exception {
		// TODO Auto-generated method stub
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/Server/Chat_Window.fxml"));
		Server_controller sc = new Server_controller();
		loader.setController(sc);
		Parent root = (Parent)loader.load();
		Scene scene = new Scene(root);
		stage.setTitle("Hello Warlock");
		stage.setScene(scene);
		stage.show();
		stage.setOnCloseRequest(e -> {
			try {
				Thread.sleep(200);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			sc.closewindow();
		});
	}
	public static void main(String[] args){
		launch(args);
	}
}