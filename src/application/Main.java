package application;


import org.xbmc.kore.host.HostManager;

import application.service.DLNAService;
import application.service.ParameterService;
import application.view.TransitionManager;
import application.view.VideoType;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			TransitionManager.setPrimaryStage(primaryStage);

			primaryStage.setTitle("Kody remote");
			if (getClass().getResourceAsStream("/images/ic_launcher.png") != null) {
				primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/ic_launcher.png")));
			}
			Scene scene = new Scene(new VBox());
			scene.getStylesheets().add(Main.class.getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);

			
			// TODO voir pouvoir le programme ne se ferme pas tous seul
			primaryStage.setOnCloseRequest((t) -> {
				Platform.exit();
                System.exit(0);
			});

			// if no host configurated show init config
			if (HostManager.getInstance().getCurrentHostInfo() != null) {
				TransitionManager.showVideoList(VideoType.VIDEO);
				TransitionManager.maximize();
			} else {
				TransitionManager.showInit();
			}
			primaryStage.setWidth(300);
			primaryStage.setHeight(200);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// start the DLNA service
		DLNAService.start();
		
		launch(args);
	}
}
