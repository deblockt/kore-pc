package application.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.xbmc.kore.host.HostInfo;
import org.xbmc.kore.host.HostManager;
import org.xbmc.kore.jsonrpc.ApiCallback;
import org.xbmc.kore.jsonrpc.Handler;
import org.xbmc.kore.jsonrpc.HostConnection;
import org.xbmc.kore.jsonrpc.method.VideoLibrary.GetEpisodes;
import org.xbmc.kore.jsonrpc.method.VideoLibrary.GetMovies;
import org.xbmc.kore.jsonrpc.method.VideoLibrary.GetTVShows;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsEpisode;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsMovie;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsTVShow;
import org.xbmc.kore.jsonrpc.type.VideoType.FieldsEpisode;
import org.xbmc.kore.jsonrpc.type.VideoType.FieldsMovie;
import org.xbmc.kore.jsonrpc.type.VideoType.FieldsTVShow;

import application.cache.Cache;
import application.cache.CacheFactory;
import application.cache.CachedApiMethod;
import application.cache.OneFileCache;

public class VideosLists {

	/**
	 * list of films
	 */
	private List<DetailsMovie> films = new ArrayList<>();
	private boolean loadingFilm = false;
	private List<Callback<List<DetailsMovie>>> filmCallbacks = new ArrayList<>();

	/**
	 * list of tvshow
	 */
	protected List<DetailsTVShow> tvShows = new ArrayList<>();
	private boolean loadingTvShows = false;
	private List<Callback<List<DetailsTVShow>>> tvShowCallbacks = new ArrayList<>();

	/**
	 * list of tvshow episods
	 */
	protected List<DetailsEpisode> episodes = new ArrayList<>();
	private boolean loadingEpisodes = false;
	private List<Callback<List<DetailsEpisode>>> episodesCallbacks = new ArrayList<>();



	public final static VideosLists INSTANCE = new VideosLists();
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

	private static final Cache<List<DetailsMovie>>  VIDEO_CACHE = new CacheFactory<>(new OneFileCache(".videoData")).getCache();
	private static final Cache<List<DetailsTVShow>> TVSHOW_CACHE = new CacheFactory<>(new OneFileCache(".tvshowData")).getCache();
	private static final CacheFactory<DetailsTVShow> EPISODE_CACHE_FACTORY = new CacheFactory<DetailsTVShow>((data) -> data.label+".episodes");

	private VideosLists() {

	}

	/**
	 * load film list
	 */
	public void getFilmList(Callback<List<DetailsMovie>> callback) {
		// si la liste est déjà chargée ou si c'est en train d'être chargé
		if (!films.isEmpty() || loadingFilm) {
			if (!loadingFilm) {
				callback.call(films);
			} else {
				filmCallbacks.add(callback);
			}
			return;
		}
		loadingFilm = true;
		filmCallbacks.add(callback);

		HostInfo currentHost = HostManager.getInstance().getCurrentHostInfo();
		HostConnection connection = new HostConnection(currentHost);

		CachedApiMethod<List<DetailsMovie>> movie = new CachedApiMethod<>(
			new GetMovies(FieldsMovie.allValues),
			VIDEO_CACHE
		);

		System.out.println("Chargement des films");
		movie.execute(connection, new ApiCallback<List<DetailsMovie>>() {

			@Override
			public void onSuccess(List<DetailsMovie> result) {
				System.out.println("SUCCESS");
				films.addAll(result);
				Collections.sort(films, COMPARATOR_MOVIE);
				loadingFilm = false;
				filmCallbacks.stream().forEach(callable -> callable.call(films));
			}

			@Override
			public void onError(int errorCode, String description) {
				System.out.println("Erreur sur la récupération des films");
				System.out.println(errorCode + ":" + description);
			}
		}, new Handler());
	}

	/**
	 * return all episodes for all tvshow
	 * callback is call for all tvshow
	 * @param callback
	 */
	public void getAllTvShowEpisods(Callback<List<DetailsEpisode>> callback) {
		if (!this.episodes.isEmpty() || loadingEpisodes) {
			if (!loadingEpisodes) {
				callback.call(episodes);
			} else {
				episodesCallbacks.add(callback);
			}
			return;
		}
		loadingFilm = true;
		episodesCallbacks.add(callback);

		System.out.println("Chargement des episodes");
		getTvShow((tvshows) -> {
			HostInfo currentHost = HostManager.getInstance().getCurrentHostInfo();
			HostConnection connection = new HostConnection(currentHost);

			for (DetailsTVShow detailsTVShow : tvshows) {
				CachedApiMethod<List<DetailsEpisode>> getTvShows = new CachedApiMethod<>(
					new GetEpisodes(detailsTVShow.tvshowid, FieldsEpisode.allValues),
					EPISODE_CACHE_FACTORY.getCache(detailsTVShow)
				);

				getTvShows.execute(connection, new ApiCallback<List<DetailsEpisode>>() {

					@Override
					public void onSuccess(List<DetailsEpisode> result) {
						episodes.addAll(result);
						loadingEpisodes = false;
						episodesCallbacks.stream().forEach(callable -> callable.call(result));
					}

					@Override
					public void onError(int errorCode, String description) {
						System.out.println("impossible de récupérer la liste des videos " + detailsTVShow.label);
						System.out.println(errorCode + ", " + description);
						episodesCallbacks.stream().forEach(callable -> callable.call(episodes));
					}
				}, new Handler());
			}
		});

	}

	public void getTvShow(Callback<List<DetailsTVShow>> callback) {
		if (!this.tvShows.isEmpty() || loadingTvShows) {
			if (!loadingTvShows) {
				callback.call(tvShows);
			} else {
				tvShowCallbacks.add(callback);
			}
			return;
		}
		loadingTvShows = true;
		tvShowCallbacks.add(callback);

		CachedApiMethod<List<DetailsTVShow>> getTvShows = new CachedApiMethod<>(
			new GetTVShows(FieldsTVShow.allValues),
			TVSHOW_CACHE
		);

		HostInfo currentHost = HostManager.getInstance().getCurrentHostInfo();
		HostConnection connection = new HostConnection(currentHost);
		getTvShows.execute(connection, new ApiCallback<List<DetailsTVShow>>() {

			@Override
			public void onSuccess(List<DetailsTVShow> result) {
				tvShows.addAll(result);
				loadingTvShows = false;
				Collections.sort(tvShows, COMPARATOR_TVSHOW);

				tvShowCallbacks.stream().forEach(callable -> callable.call(tvShows));
			}

			@Override
			public void onError(int errorCode, String description) {
				System.out.println("Erreur lors de la récupération des séries:  " + errorCode + " : " + description);
			}
		}, new Handler());

	}


}
