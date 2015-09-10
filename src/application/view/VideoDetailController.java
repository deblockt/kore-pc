package application.view;


import org.xbmc.kore.host.HostInfo;
import org.xbmc.kore.host.HostManager;
import org.xbmc.kore.jsonrpc.ApiCallback;
import org.xbmc.kore.jsonrpc.Handler;
import org.xbmc.kore.jsonrpc.HostConnection;
import org.xbmc.kore.jsonrpc.method.Files.PrepareDownload;
import org.xbmc.kore.jsonrpc.type.FilesType;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsMovie;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
	private Button play;

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

		play.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				// récupération de l'url de la video
				HostInfo currentHost = HostManager.getInstance().getCurrentHostInfo();
				HostConnection connection = new HostConnection(currentHost);

				PrepareDownload prepare = new PrepareDownload(detailsMovie.file);
				prepare.execute(connection, new ApiCallback<FilesType.PrepareDownloadReturnType>() {

					@Override
					public void onSuccess(FilesType.PrepareDownloadReturnType result) {
						System.out.println("Succes de la réupération de l'URL");
						String uri = currentHost.getHttpURL() + "/" + result.path;
						System.out.println(uri);
						TransitionManager.showPlayer(uri);
					}

					@Override
					public void onError(int errorCode, String description) {
						System.out.println("Erreur lors de la récupération " + errorCode + " : " + description);

					}

				} , new Handler());
			}
		});
	}

	private void reinitData() {
		title.setText(detailsMovie.label);
		plot.setText(detailsMovie.plot);

		HostInfo host = HostManager.getInstance().getCurrentHostInfo();
		Image image = new Image(host.getImageUrl(detailsMovie.thumbnail), 300, -1, true, false, true);
		poster.setImage(image);

		if (detailsMovie.fanart != null) {
			BackgroundImage myBI= new BackgroundImage(new Image(host.getImageUrl(detailsMovie.fanart), true),
			        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
			          new BackgroundSize(100, 100, true, true, false, true));

			fanartBackground.setBackground(new Background(myBI));
		}
	}

	public void setVideoData(DetailsMovie detailsMovie) {
		this.detailsMovie = detailsMovie;
		this.reinitData();
	}

}
