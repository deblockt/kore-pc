package application.view;

import java.io.IOException;
import java.util.List;

import org.xbmc.kore.jsonrpc.type.VideoType.DetailsMovie;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsTVShow;

import application.Main;
import application.service.ParameterService;
import application.service.ParameterNames;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TransitionManager {

	private static Stage primaryStage;

	private static Parent FILM_LIST_VIEW;
	private static VideoListController FILM_LIST_CONTROLLER;

	/**
	 * Affiche le detail d'une video
	 *
	 * @param detailsMovie
	 */
	public static void showInit() {
		FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("view/Init.fxml"));
        try {
        	Parent root =  loader.load();
            primaryStage.getScene().setRoot(root);
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
	}

	/**
	 * show the playing string
	 */
	public static void showPlaying() {
		FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("view/Playing.fxml"));
        try {
        	Parent root =  loader.load();
            primaryStage.getScene().setRoot(root);
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
	}

	/**
	 * Affiche le detail d'une video
	 *
	 * @param detailsMovie
	 */
	public static void showDetail(DetailsMovie detailsMovie) {
		FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("view/VideoDetail.fxml"));
        try {
        	Parent root =  loader.load();

            VideoDetailController videoDetailController = loader.getController();
            videoDetailController.setVideoData(detailsMovie);

            primaryStage.getScene().setRoot(root);
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
	}

	/**
	 * Affiche le detail d'une video
	 *
	 * @param detailTvShow
	 */
	public static void showTvShowDetails(DetailsTVShow detailTvShow) {
		FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("view/TvShowDetail.fxml"));
        try {
        	Parent root =  loader.load();

            TvShowDetailController videoDetailController = loader.getController();
            videoDetailController.setTvShowDetails(detailTvShow);

            primaryStage.getScene().setRoot(root);
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
	}

	/**
	 * affichage la liste des videos
	 */
	public static void showVideoList(VideoType videoType) {
		if (FILM_LIST_VIEW == null) {
			FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(Main.class.getResource("view/VideoList.fxml"));
	        try {
	        	FILM_LIST_VIEW =  loader.load();
	        	FILM_LIST_CONTROLLER = loader.getController();
	        } catch (Exception e) {
	        	throw new RuntimeException(e);
	        }
		}

        primaryStage.getScene().setRoot(FILM_LIST_VIEW);
        if (VideoType.VIDEO.equals(videoType)) {
        	FILM_LIST_CONTROLLER.showAllFilms();
        } else {
        	FILM_LIST_CONTROLLER.showAllTvShow();
        }
	}



	public static void showPlayer(String uri, String title) {
		try {
			String cmd = ParameterService.getInstance().getString(ParameterNames.VIDEO_PLAYER_PATH) + " --fullscreen "+uri+" --input-title-format=\""+title+"\" --no-video-title-show";
			System.out.println(cmd);
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void setPrimaryStage(Stage primaryStage) {
		TransitionManager.primaryStage = primaryStage;
	}

	/**
	 * show the initial kody list screen
	 * @param rechable list of kody ip
	 */
	public static void showKodyList(List<String> rechable) {
		FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource("view/KodyList.fxml"));
        try {
        	Parent root =  loader.load();

            KodyListController videoDetailController = loader.getController();
            videoDetailController.setListKodyIp(rechable);

            primaryStage.getScene().setRoot(root);
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
	}

	/**open a pop-up parameter
	 *
	 * @return
	 */
	public static void openParameters() {
		Parent popup;
		try {
			FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(Main.class.getResource("view/Parameters.fxml"));

			popup = loader.load();

	        Stage dialog = new Stage();

	        dialog.initModality(Modality.APPLICATION_MODAL);
	        dialog.initOwner(primaryStage);
	        dialog.setTitle("Paramètres");

	        Scene dialogScene = new Scene(popup, 600, 400);
	        dialogScene.getStylesheets().add(Main.class.getResource("application.css").toExternalForm());
            dialog.setScene(dialogScene);
            dialog.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void maximize() {
		primaryStage.setMaximized(true);
	}

}
