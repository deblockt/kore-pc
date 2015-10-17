package application.view;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;
import org.xbmc.kore.host.HostInfo;
import org.xbmc.kore.host.HostManager;
import org.xbmc.kore.jsonrpc.Handler;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsFile;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.GridPane;

public class PlayingDetailSubscreenPane extends GridPane {

	@FXML
	private Label title;

	@FXML
	private ImageView poster;

	@FXML
	private Label time;

	@FXML
	private Slider timeSlider;

	@FXML
	private Button playButton;

	@FXML
	private Button pauseButton;

	@FXML
	private Button stopButton;

	private static Timer timer = new Timer();

	public PlayingDetailSubscreenPane(DetailsFile details) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PlayingDetailSubscreen.fxml"));

		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}


		title.setText(details.title);

		// set the background
		HostInfo host = HostManager.getInstance().getCurrentHostInfo();

		BackgroundImage myBI= new BackgroundImage(new Image(host.getImageUrl(details.fanart), true),
		        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
		          new BackgroundSize(100, 100, true, true, false, true));

		this.setBackground(new Background(myBI));

		// set the poster
		Image image = new Image(host.getImageUrl(details.thumbnail), 300, -1, true, false, true);
		poster.setImage(image);
		time.setText(formatDuration(details.runtime));

		timeSlider.setMax(details.runtime);
		timeSlider.setMin(0);
		timeSlider.valueProperty().addListener((observable, old, newValue) -> {
			new Handler().post(() -> time.setText((formatDuration(newValue.intValue()))));
		});

		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				timeSlider.valueProperty().setValue(timeSlider.valueProperty().getValue() + 1);
			}
		}, 0, 1000);

	}

	/**
	 * add seek listener (when slider move)
	 * @param listener
	 */
	public void addSeekListener(ChangeListener<? super Number> listener) {
		timeSlider.valueProperty().addListener(listener);
	}

	public void addPlayListener(EventHandler<MouseEvent> handler) {
		playButton.addEventFilter(MouseEvent.MOUSE_CLICKED, handler);
	}

	public void addStopListener(EventHandler<MouseEvent> handler) {
		stopButton.addEventFilter(MouseEvent.MOUSE_CLICKED, handler);
	}

	public void addPauseListener(EventHandler<MouseEvent> handler) {
		pauseButton.addEventFilter(MouseEvent.MOUSE_CLICKED, handler);
	}

	private String formatDuration(int duration) {
		int hour = duration / 3600;
		int minutes = (duration % 3600) / 60;
		int secondes = ((duration % 3600) % 60);

		return StringUtils.leftPad(String.valueOf(hour), 2, "0") + ":" +
			   StringUtils.leftPad(String.valueOf(minutes), 2, "0") + ":" +
			   StringUtils.leftPad(String.valueOf(secondes), 2, "0");
	}

	public void setPosition(int second) {
		timeSlider.valueProperty().setValue(second);
	}
}
