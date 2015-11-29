package application.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.xbmc.kore.host.HostInfo;
import org.xbmc.kore.host.HostManager;
import org.xbmc.kore.jsonrpc.Handler;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsMedia;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsMovie;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsTVShow;

import application.cache.Cache;
import application.cache.CacheFactory;
import application.cache.OneFileCache;
import application.component.AsyncImageView;
import application.service.PoundedElement;
import application.service.SmithWaterman;
import application.service.VideosLists;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class VideoListController {

	@FXML
	private Label videoButton;

	@FXML
	private Label tvShowButton;

	@FXML
	private Label reloadButton;

	@FXML
	private Label parameterButton;

	@FXML
	private Label playingButton;

	@FXML
	private Pane contentPane;

	@FXML
	private ScrollPane scrollPane;

	@FXML
	private TextField filter;

	@FXML
	private Label kodyName;

	private boolean filmSection;

	private static Map<String, Pane> tiles = new HashMap<>();

	private List<DetailsMovie> films = new ArrayList<>();
	protected List<DetailsTVShow> tvShows = new ArrayList<>();

	private boolean filmsLoaded = false;
	private boolean tvShowsLoaded = false;

	/**
	 * load film list
	 */
	private synchronized void loadFilmList() {
		if (!filmsLoaded) {
			VideosLists.INSTANCE.getFilmList((list) -> {
				films = list;
				filmsLoaded = true;
				new Handler().post(() -> showAllFilms());
			});
		}
	}

	public synchronized void loadTvShow() {
		if (!tvShowsLoaded) {
			System.out.println("Chargement des series");
			VideosLists.INSTANCE.getTvShow((list) -> {
				tvShows = list;
				tvShowsLoaded = true;
				System.out.println("Fin du chargement");
				new Handler().post(() -> showAllTvShow());
			});
		}
	}

	private static Cache<List<DetailsMovie>>  VIDEO_CACHE = new CacheFactory<>(new OneFileCache(".videoData")).getCache();
	private static Cache<List<DetailsTVShow>> TVSHOW_CACHE = new CacheFactory<>(new OneFileCache(".tvshowData")).getCache();


	/**
	 * filter films by search input
	 *
	 */

	private void filterFilms(String filter) {
		contentPane.getChildren().clear();
		// pas de filtre, alors on affiche tout
		if (StringUtils.isEmpty(filter)) {
			if (this.filmSection) {
				showAllFilms();
			} else {
				showAllTvShow();
			}
			return;
		}

		if (filmSection) {
			List<DetailsMovie> filtredList =  filterList(films);
			filtredList.forEach(this::addTile);
		} else {
			List<DetailsTVShow> filtredList =  filterList(tvShows);
			filtredList.forEach(this::addTile);
		}

		// on parcourt tous les tiles pour afficher les images
		for (Node child : contentPane.getChildren()) {
			for (Node subChild : ((Pane)child).getChildren()) {
				if (subChild instanceof AsyncImageView) {
					((AsyncImageView) subChild).loadIfVisible(scrollPane);
				}
			}
		}
	}

	/**
	 * retourne la liste avec tous les elements trouvé triés par pertinance
	 * @param data
	 * @return
	 */
	private <T extends DetailsMedia> List<T> filterList(List<T> data) {
		return data.stream()
			.map((detailsMovie) -> {
				int similarity = getSimilarity(detailsMovie.title, filter.getText());
				if (similarity > 70) {
					return new PoundedElement<T>(similarity, detailsMovie);
				}
				return null;
			})
			.filter((e) -> e != null)
			.sorted()
			.map((e) -> e.data)
			.collect(Collectors.toList());
	}

	public void showAllFilms() {
		this.filmSection = true;

		this.tvShowButton.getStyleClass().remove("selected");
		if (!this.videoButton.getStyleClass().contains("selected")) {
			this.videoButton.getStyleClass().add("selected");
		}

		loadFilmList();
		try {
			this.contentPane.getChildren().clear();
		} catch (Exception e) {
			e.printStackTrace();
		}

		films.forEach(this::addTile);
	}

	public void showAllTvShow() {
		System.out.println("showAllTvShow");
		this.filmSection = false;

		this.videoButton.getStyleClass().remove("selected");
		if (!this.tvShowButton.getStyleClass().contains("selected")){
			this.tvShowButton.getStyleClass().add("selected");
		}

		loadTvShow();
		this.contentPane.getChildren().clear();
		tvShows.forEach(this::addTile);
	}

	public void addTile(DetailsMovie detailsMovie) {
		Pane film = createTile(detailsMovie);
		contentPane.getChildren().add(film);
	}

	public void addTile(DetailsTVShow detailsMovie) {
		Pane film = createTile(detailsMovie);
		contentPane.getChildren().add(film);
	}


	/**
	 * return similarity score between 0 and 100
	 * @param title1
	 * @param title2
	 * @return
	 */
	private int getSimilarity(String title1, String title2) {
		return SmithWaterman.processToPercent(title1, title2);
	}

	private Pane createTile(DetailsMovie detailsMovie) {
		if (tiles.containsKey("movie"+detailsMovie.movieid)) {
			return tiles.get("movie"+detailsMovie.movieid);
		}

		Pane box = createTileContent(detailsMovie.thumbnail, detailsMovie.title, detailsMovie.label);

		// affichage du detail de la video
		box.setOnMouseClicked(e -> TransitionManager.showDetail(detailsMovie));

		tiles.put("movie"+detailsMovie.movieid, box);
		return box;
	}

	private Pane createTile(DetailsTVShow detailsTVShow) {
		if (tiles.containsKey("tvshow"+detailsTVShow.tvshowid)) {
			return tiles.get("tvshow"+detailsTVShow.tvshowid);
		}
		Pane box = createTileContent(detailsTVShow.thumbnail, detailsTVShow.title, detailsTVShow.label);

		// affichage du detail de la video
		box.setOnMouseClicked(e ->  TransitionManager.showTvShowDetails(detailsTVShow));

		tiles.put("tvshow"+detailsTVShow.tvshowid, box);
		return box;
	}


	private Pane createTileContent(String thumbnail, String title, String label) {
		Pane box = new VBox();
		box.getStyleClass().add("video_data");
		box.setMaxWidth(150);

		HostInfo host = HostManager.getInstance().getCurrentHostInfo();

		AsyncImageView imageView = new AsyncImageView(host.getImageUrl(thumbnail), String.valueOf(title.charAt(0)), scrollPane);

		Label sceneLabel = new Label();
		sceneLabel.setText(label);
		box.getChildren().add(imageView);
		box.getChildren().add(sceneLabel);
		return box;
	}



	@FXML
	private void initialize() {

		kodyName.setText(HostManager.getInstance().getCurrentHostInfo().getName());

		HostManager.getInstance().currentHostInfoProperty().addListener(
			(obser, oldValue, newValue) -> {
				kodyName.setText(newValue.getName());
				reload();
			}
		);

		videoButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> this.showAllFilms());
		tvShowButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> this.showAllTvShow());

		reloadButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> this.reload());

		parameterButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> TransitionManager.openParameters());
		playingButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> TransitionManager.showPlaying());

		filter.textProperty().addListener((observable, oldValue, newValue) -> filterFilms(newValue));


	}




	private void reload() {
		this.contentPane.getChildren().clear();
		if (this.filmSection) {
			VIDEO_CACHE.clearCache();
			this.films.clear();
			filmsLoaded = false;
			this.loadFilmList();
		} else {
			TVSHOW_CACHE.clearCache();
			this.tvShows.clear();
			tvShowsLoaded = false;
			this.loadTvShow();
		}
	}

}
