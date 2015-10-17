package application.view;

import org.xbmc.kore.host.HostInfo;
import org.xbmc.kore.host.HostManager;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsMovie;

import application.component.CacheImageFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class VideoDetailController {

	@FXML
	private ImageView poster;

	@FXML
	private Label title;

	@FXML
	private Text plot;

	@FXML
	private Pane playContainer;

	private DlnaButton play;

	@FXML
	private Label videoButton;

	@FXML
	private Label tvshowButton;

	@FXML
	private Pane fanartBackground;

	@FXML
	private VBox posterContainer;

	private DetailsMovie detailsMovie;

	@FXML
	private void initialize() {

		videoButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> TransitionManager.showVideoList(VideoType.VIDEO));
		tvshowButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> TransitionManager.showVideoList(VideoType.TV_SHOW));



	}

	private void reinitData() {
		title.setText(detailsMovie.label);
		plot.setText(detailsMovie.plot);

		HostInfo host = HostManager.getInstance().getCurrentHostInfo();
		Image image = CacheImageFactory.getImage(host.getImageUrl(detailsMovie.thumbnail), 300, -1, true, true, true);
		poster.setImage(image);

		if (detailsMovie.fanart != null) {
			BackgroundImage myBI= new BackgroundImage(CacheImageFactory.getImage(host.getImageUrl(detailsMovie.fanart), true),
			        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
			          new BackgroundSize(100, 100, true, true, false, true));

			fanartBackground.setBackground(new Background(myBI));
		}

		play = new DlnaButton(detailsMovie);
		playContainer.getChildren().clear();
		playContainer.getChildren().add(play);
	}

	public void setVideoData(DetailsMovie detailsMovie) {
		this.detailsMovie = detailsMovie;
		this.reinitData();
	}

}
