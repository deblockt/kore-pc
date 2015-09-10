package application.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.xbmc.kore.host.HostInfo;
import org.xbmc.kore.host.HostManager;
import org.xbmc.kore.jsonrpc.ApiCallback;
import org.xbmc.kore.jsonrpc.ApiMethod;
import org.xbmc.kore.jsonrpc.Handler;
import org.xbmc.kore.jsonrpc.HostConnection;
import org.xbmc.kore.jsonrpc.method.VideoLibrary.GetMovies;
import org.xbmc.kore.jsonrpc.method.VideoLibrary.GetTVShows;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsMedia;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsMovie;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsTVShow;
import org.xbmc.kore.jsonrpc.type.VideoType.FieldsMovie;
import org.xbmc.kore.jsonrpc.type.VideoType.FieldsTVShow;

import application.cache.Cache;
import application.cache.CacheFactory;
import application.cache.OneFileCache;
import application.component.AsyncImageView;
import application.service.PoundedElement;
import application.service.SmithWaterman;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
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
	private Pane contentPane;

	@FXML
	private ScrollPane scrollPane;

	@FXML
	private TextField filter;

	@FXML
	private Label kodyName;

	private List<DetailsMovie> films = new ArrayList<>();
	private boolean loadingFilm = false;

	/**
	 * list des tvshow
	 */
	protected List<DetailsTVShow> tvShows = new ArrayList<>();
	private boolean loadingTvShows = false;

	private boolean filmSection;

	private static Map<String, Pane> tiles = new HashMap<>();

	private static Comparator<DetailsMovie> COMPARATOR_MOVIE = (o1, o2) -> {
		String title1 = StringUtils.isBlank(o1.sorttitle) ? o1.title : o1.sorttitle;
		String title2 = StringUtils.isBlank(o2.sorttitle) ? o2.title : o2.sorttitle;
		return title1.compareTo(title2);
	};

	private static Comparator<DetailsTVShow> COMPARATOR_TVSHOW = (o1, o2) -> {
		String title1 = StringUtils.isBlank(o1.sorttitle) ? o1.title : o1.sorttitle;
		String title2 = StringUtils.isBlank(o2.sorttitle) ? o2.title : o2.sorttitle;
		return title1.compareTo(title2);
	};

	private static Cache<List<DetailsMovie>>  VIDEO_CACHE = new CacheFactory<>(new OneFileCache(".videoData")).getCache();
	private static Cache<List<DetailsTVShow>> TVSHOW_CACHE = new CacheFactory<>(new OneFileCache(".tvshowData")).getCache();

	/**
	 * load film list
	 */
	private synchronized void loadFilmList() {
		// si la liste est déjà chargée ou si c'est en train d'être chargé
		if (!films.isEmpty() || loadingFilm) {
			return;
		}
		loadingFilm = true;

		HostInfo currentHost = HostManager.getInstance().getCurrentHostInfo();
		HostConnection connection = new HostConnection(currentHost);

		ApiMethod<List<DetailsMovie>> movie = //new CachedApiMethod<>(
			new GetMovies(
				FieldsMovie.TITLE, FieldsMovie.FILE, FieldsMovie.PLOT,
				FieldsMovie.THUMBNAIL, FieldsMovie.SORTTITLE, FieldsMovie.FANART
			)/*,
			VIDEO_CACHE
		)*/;

		movie.execute(connection, new ApiCallback<List<DetailsMovie>>() {

			@Override
			public void onSuccess(List<DetailsMovie> result) {
				for (DetailsMovie movie : result) {
					films.add(movie);
				}
				loadingFilm = false;
				Collections.sort(films, COMPARATOR_MOVIE);
				showAllFilms();
			}

			@Override
			public void onError(int errorCode, String description) {
				System.out.println("Erreur sur la récupération des films");
				System.out.println(errorCode + ":" + description);
			}
		}, new Handler());
	}



	public synchronized void loadTvShow() {
		if (!this.tvShows.isEmpty() || loadingTvShows) {
			return;
		}
		loadingTvShows = true;

		ApiMethod<List<DetailsTVShow>> getTvShows = //new CachedApiMethod<>(
			new GetTVShows(FieldsTVShow.allValues)/*,
			TVSHOW_CACHE
		)*/;

		HostInfo currentHost = HostManager.getInstance().getCurrentHostInfo();
		HostConnection connection = new HostConnection(currentHost);
		getTvShows.execute(connection, new ApiCallback<List<DetailsTVShow>>() {

			@Override
			public void onSuccess(List<DetailsTVShow> result) {
				for (DetailsTVShow movie : result) {
					tvShows.add(movie);
				}
				loadingTvShows = false;
				Collections.sort(tvShows, COMPARATOR_TVSHOW);
				showAllTvShow();
			}

			@Override
			public void onError(int errorCode, String description) {
				System.out.println("Erreur lors de la récupération des séries:  " + errorCode + " : " + description);
			}
		}, new Handler());

	}

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
			List<PoundedElement<DetailsMovie>> filtredList =  filterList(films);
			for (PoundedElement<DetailsMovie> poundedElement : filtredList) {
				addTile(poundedElement.data);
			}
		} else {
			List<PoundedElement<DetailsTVShow>> filtredList =  filterList(tvShows);
			for (PoundedElement<DetailsTVShow> poundedElement : filtredList) {
				addTile(poundedElement.data);
			}
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
	private <T extends DetailsMedia> List<PoundedElement<T>> filterList(List<T> data) {
		List<PoundedElement<T>> retour = new LinkedList<>();

		for (T detailsMovie : data) {
			int similarity = getSimilarity(detailsMovie.title, filter.getText());
			if (similarity > 70) {
				retour.add(new PoundedElement<T>(similarity, detailsMovie));
			}
		}

		Collections.sort(retour);
		return retour;
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
		for (DetailsMovie detailsMovie : films) {
			addTile(detailsMovie);
		}
	}

	public void showAllTvShow() {
		this.filmSection = false;

		this.videoButton.getStyleClass().remove("selected");
		if (!this.tvShowButton.getStyleClass().contains("selected")){
			this.tvShowButton.getStyleClass().add("selected");
		}

		loadTvShow();
		this.contentPane.getChildren().clear();
		for (DetailsTVShow detailstvShow : tvShows) {
			addTile(detailstvShow);
		}
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

		box.setOnMouseClicked(new EventHandler<MouseEvent>(){

			@Override
			public void handle(MouseEvent arg0) {
				// affichage du detail de la video
				TransitionManager.showDetail(detailsMovie);
			}
		});

		tiles.put("movie"+detailsMovie.movieid, box);
		return box;
	}

	private Pane createTile(DetailsTVShow detailsTVShow) {
		if (tiles.containsKey("tvshow"+detailsTVShow.tvshowid)) {
			return tiles.get("tvshow"+detailsTVShow.tvshowid);
		}
		Pane box = createTileContent(detailsTVShow.thumbnail, detailsTVShow.title, detailsTVShow.label);

		box.setOnMouseClicked(new EventHandler<MouseEvent>(){

			@Override
			public void handle(MouseEvent arg0) {
				// affichage du detail de la video
				TransitionManager.showTvShowDetails(detailsTVShow);
			}
		});

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

		videoButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> this.showAllFilms());
		tvShowButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> this.showAllTvShow());

		reloadButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> this.reload());

		parameterButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> TransitionManager.openParameters());
		filter.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				filterFilms(newValue);
			}
		});

	}




	private void reload() {
		this.contentPane.getChildren().clear();
		if (this.filmSection) {
			VIDEO_CACHE.clearCache();
			this.films.clear();
			this.loadFilmList();
		} else {
			TVSHOW_CACHE.clearCache();
			this.tvShows.clear();
			this.loadTvShow();
		}
	}

}
