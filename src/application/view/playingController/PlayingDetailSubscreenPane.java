package application.view.playingController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;
import org.xbmc.kore.host.HostInfo;
import org.xbmc.kore.host.HostManager;
import org.xbmc.kore.jsonrpc.Handler;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsFile;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.text.Text;

public class PlayingDetailSubscreenPane extends GridPane {

	@FXML
	private Label title;

	@FXML
	private ImageView poster;

	@FXML
	private Label time;

	@FXML
	private Text plot;

	@FXML
	private Slider timeSlider;

	@FXML
	private Button playButton;

	@FXML
	private Button pauseButton;

	@FXML
	private Button stopButton;


	private boolean pause = false;

	private static Timer timer = new Timer();

	private SeekListenerManager seekListenerManager = new SeekListenerManager();

	public PlayingDetailSubscreenPane(DetailsFile details) {
		super();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PlayingDetailSubscreen.fxml"));

		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}


		title.setText(details.title);
		plot.setText(details.plot);

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
		timeSlider.valueProperty().addListener(seekListenerManager);

		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				if (!pause) {
					timeSlider.valueProperty().setValue(timeSlider.valueProperty().getValue() + 1);
				}
			}
		}, 0, 1000);

		pauseButton.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> this.pause = true);
		playButton.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> this.pause = false);

	}

	/**
	 * add seek listener (when slider move)
	 * @param listener
	 */
	public void addSeekListener(ChangeListener<? super Number> listener) {
		seekListenerManager.addListener(listener);
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
		seekListenerManager.dontCallNextChange();
		timeSlider.valueProperty().setValue(second);
	}

	public void pause() {
		this.pause  = true;
	}

	public void play() {
		this.pause = false;
	}

	public boolean isPaused() {
		return this.pause;
	}

   private class SeekListenerManager implements ChangeListener<Number> {

        private int i = 0;
        private Handler handler = new Handler();
        private List<ChangeListener<? super Number>> listeners = new ArrayList<>();
    	// dont call seek listener when change is done with setPosition
    	private boolean dontCallNextChange = false;

        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            final int currentI = i++;
            if (!dontCallNextChange && oldValue.intValue() + 1 != newValue.intValue()) {
	            Runnable prevRunnable = () -> {
	            	 // don't call if two skeep successively
	            	if (currentI == i - 1) {
		            	for (ChangeListener<? super Number> changeListener : listeners) {
		            		changeListener.changed(observable, oldValue, newValue);
						}
	            	}
	            };
	            // don't call if two skeep successively
	            handler.postDelayed(prevRunnable, 200);
            }

            dontCallNextChange = false;
        }

        public void addListener(ChangeListener<? super Number> listener) {
        	listeners.add(listener);
        }

        public void dontCallNextChange() {
        	dontCallNextChange = true;
        }
    }
}
