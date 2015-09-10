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
import org.xbmc.kore.jsonrpc.method.Files.PrepareDownload;
import org.xbmc.kore.jsonrpc.method.VideoLibrary.GetEpisodes;
import org.xbmc.kore.jsonrpc.type.FilesType;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsEpisode;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsSeason;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsTVShow;
import org.xbmc.kore.jsonrpc.type.VideoType.FieldsEpisode;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
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
	private Button play;

	@FXML
	private Pane episodDetails;

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

			seasonDetail.setBackground(new Background(myBI));
		}
		this.loadTvShowDetail();
	}

	@FXML
	private void initialize() {

		seasonList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			episodList.setItems(FXCollections.observableArrayList(episodes.get(newValue)));
		});


		episodList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			// HostInfo hostInfo = HostManager.getInstance().getCurrentHostInfo();

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
                            setText(t.episode + " - " + t.title);
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

		play.setOnAction((event) -> {
			// récupération de l'url de la video
			HostInfo currentHost = HostManager.getInstance().getCurrentHostInfo();
			HostConnection connection = new HostConnection(currentHost);

			PrepareDownload prepare = new PrepareDownload(episodList.getSelectionModel().getSelectedItem().file);
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
		});


		videoButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
            TransitionManager.showVideoList(VideoType.VIDEO);
		});

		tvshowButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
            TransitionManager.showVideoList(VideoType.TV_SHOW);
		});

		episodDetails.setVisible(false);
	}

	private void loadTvShowDetail() {

		GetEpisodes getEpisodes = new GetEpisodes(detailTvShow.tvshowid, FieldsEpisode.allValues);

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
