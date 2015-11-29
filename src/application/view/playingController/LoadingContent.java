package application.view.playingController;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

public class LoadingContent extends GridPane {
	@FXML
	private ImageView loadingImageView;

	public LoadingContent() {
		super();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("LoadingContent.fxml"));

		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		loadingImageView.setImage(new Image(this.getClass().getResourceAsStream("/images/loading.gif")));
	}
}
