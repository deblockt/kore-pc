package application.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.xbmc.kore.host.HostInfo;
import org.xbmc.kore.host.HostManager;
import org.xbmc.kore.jsonrpc.ApiCallback;
import org.xbmc.kore.jsonrpc.Handler;
import org.xbmc.kore.jsonrpc.HostConnection;
import org.xbmc.kore.jsonrpc.method.VideoLibrary.GetEpisodes;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsEpisode;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsSeason;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsTVShow;
import org.xbmc.kore.jsonrpc.type.VideoType.FieldsEpisode;

import application.cache.CacheFactory;
import application.cache.CachedApiMethod;
import application.view.component.DlnaButton;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;

public class TvShowDetailController {

	private DetailsTVShow detailTvShow;

	protected List<DetailsSeason> seasons;

	@FXML
	private Label title;

	@FXML
	private Text plot;

	@FXML
	private ImageView poster;

	@FXML
	private ListView<Integer> seasonList;

	@FXML
	private ListView<DetailsEpisode> episodList;

	@FXML
	private Pane playContainer;

	@FXML
	private Pane episodDetails;

	@FXML
	private Pane backgroundTvShowDetail;

	@FXML
	private Label episodTitle;

	@FXML
	private Label episodSaisonAndNum;

	@FXML
	private TextFlow episodPlot;

	@FXML
	private Pane seasonDetail;

	@FXML
	private Label videoButton;

	@FXML
	private Label tvshowButton;

	private HashMap<Integer, List<DetailsEpisode>> episodes;

	private DlnaButton dlnaButton = new DlnaButton();

	private static final CacheFactory<DetailsTVShow> EPISODE_CACHE_FACTORY = new CacheFactory<DetailsTVShow>((data) -> data.label+".episodes");


	public void setTvShowDetails(DetailsTVShow detailTvShow) {
		this.detailTvShow = detailTvShow;
		title.setText(detailTvShow.label);
		plot.setText(detailTvShow.plot);
		HostInfo host = HostManager.getInstance().getCurrentHostInfo();
		poster.setImage(new Image(host.getImageUrl(detailTvShow.thumbnail), true));

		// set the fanart background
		if (detailTvShow.fanart != null) {
			BackgroundImage myBI= new BackgroundImage(new Image(host.getImageUrl(detailTvShow.fanart), true),
		        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
		          new BackgroundSize(100, 100, true, true, false, true));

			backgroundTvShowDetail.setBackground(new Background(myBI));
		}
		this.loadTvShowDetail();

		playContainer.getChildren().add(dlnaButton);
	}

	@FXML
	private void initialize() {

		seasonList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			episodList.setItems(FXCollections.observableArrayList(episodes.get(newValue)));
		});

		episodList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			dlnaButton.setDetailsEpisode(newValue);

			if (newValue != null) {
				episodDetails.setVisible(true);
				episodTitle.setText(newValue.title);
				episodSaisonAndNum.setText("Saison " + newValue.season + ", Episode " + newValue.episode);
				episodPlot.getChildren().clear();
				Text plot = new Text(newValue.plot);
				plot.setFill(Color.WHITE);
				episodPlot.getChildren().add(plot);
			} else {
				episodDetails.setVisible(false);
			}

		});

		episodList.setCellFactory(new Callback<ListView<DetailsEpisode>, ListCell<DetailsEpisode>>(){

            @Override
            public ListCell<DetailsEpisode> call(ListView<DetailsEpisode> p) {

                ListCell<DetailsEpisode> cell = new ListCell<DetailsEpisode>(){

                    @Override
                    protected void updateItem(DetailsEpisode t, boolean bln) {
                        super.updateItem(t, bln);
                        if (t != null) {
                        	HBox box = new HBox();
                        	Label lepisode = new Label(String.valueOf(t.episode));
                        	lepisode.setMinWidth(30);
                        	box.getChildren().add(lepisode);
                        	box.getChildren().add(new Label(t.title));

                        	if (t.playcount > 0) {
                        		// use for align right the seen label
                        		Pane pane = new Pane();
                        		HBox.setHgrow(pane, Priority.ALWAYS);
                        		box.getChildren().add(pane);

                        		Label seen = new Label("Vu");
                        		seen.getStyleClass().add("seen");

                        		HBox.setMargin(seen, new Insets(0, 10, 0, 0));

                        		box.getChildren().add(seen);
                        	}
                        	setGraphic(box);
                        } else {
                        	setText("");
                        	setGraphic(null);
                        }
                    }

                };

                return cell;
            }
        });

		seasonList.setCellFactory(new Callback<ListView<Integer>, ListCell<Integer>>(){

            @Override
            public ListCell<Integer> call(ListView<Integer> p) {

                ListCell<Integer> cell = new ListCell<Integer>(){

                    @Override
                    protected void updateItem(Integer t, boolean bln) {
                        super.updateItem(t, bln);
                        if (t != null) {
                            setText("Saison " + t);
                        }
                    }

                };

                return cell;
            }
        });

		videoButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
            TransitionManager.showVideoList(VideoType.VIDEO);
		});

		tvshowButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
            TransitionManager.showVideoList(VideoType.TV_SHOW);
		});

		episodDetails.setVisible(false);

		// update poster size with screen size
		Region parentRegion = (Region) poster.getParent();
		poster.fitWidthProperty().bind(parentRegion.widthProperty());
		poster.fitHeightProperty().bind(parentRegion.heightProperty());
	}

	private void loadTvShowDetail() {

		CachedApiMethod<List<DetailsEpisode>> getEpisodes = new CachedApiMethod<>(
			new GetEpisodes(detailTvShow.tvshowid, FieldsEpisode.allValues),
			EPISODE_CACHE_FACTORY.getCache(detailTvShow)
		);
		HostInfo hostInfo = HostManager.getInstance().getCurrentHostInfo();
		HostConnection connection = new HostConnection(hostInfo);
		getEpisodes.execute(connection, new ApiCallback<List<DetailsEpisode>>() {

			@Override
			public void onSuccess(List<DetailsEpisode> result) {
				episodes = new HashMap<>();

				for (DetailsEpisode detailsEpisode : result) {
					if (!episodes.containsKey(detailsEpisode.season)) {
						episodes.put(detailsEpisode.season, new ArrayList<>());
					}
					episodes.get(detailsEpisode.season).add(detailsEpisode);
				}

				// on tri les episodes par ordre
				for (List<DetailsEpisode> episodes : episodes.values()) {
					Collections.sort(episodes, (epi1, epi2) ->  epi1.episode - epi2.episode);
				}

				List<Integer> seasons = new ArrayList<>(episodes.keySet());
				Collections.sort(seasons);
				seasonList.setItems(FXCollections.observableArrayList(seasons));
				seasonList.getSelectionModel().clearAndSelect(0);
			}

			@Override
			public void onError(int errorCode, String description) {
				// TODO Auto-generated method stub

			}
		}, new Handler());


	}

}
