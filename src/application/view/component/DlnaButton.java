package application.view.component;


import java.util.HashMap;
import java.util.Map;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.xbmc.kore.host.HostInfo;
import org.xbmc.kore.host.HostManager;
import org.xbmc.kore.jsonrpc.ApiCallback;
import org.xbmc.kore.jsonrpc.Handler;
import org.xbmc.kore.jsonrpc.method.Files.PrepareDownload;
import org.xbmc.kore.jsonrpc.method.Player;
import org.xbmc.kore.jsonrpc.type.FilesType;
import org.xbmc.kore.jsonrpc.type.FilesType.PrepareDownloadReturnType;
import org.xbmc.kore.jsonrpc.type.PlaylistType;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsEpisode;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsFile;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsMovie;

import application.service.DLNAService;
import application.service.DLNAService.DlnaListener;
import application.view.TransitionManager;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;

public class DlnaButton extends HBox {

	private Button play;

	private Button more;

	private ContextMenu contextMenu = new ContextMenu();

	private DetailsFile detailsItem;

	PlaylistType.Item item;

	private interface Closure<T> {
		public void run(T param);
	}

	private class VideoLauncher implements ApiCallback<FilesType.PrepareDownloadReturnType> {

		private Closure<String> closure;

		public VideoLauncher(Closure<String> closure) {
			this.closure = closure;
		}
		@Override
		public void onSuccess(PrepareDownloadReturnType result) {
			System.out.println("Succes de la réupération de l'URL");
			HostInfo currentHost = HostManager.getInstance().getCurrentHostInfo();
			String uri = currentHost.getHttpURL() + "/" + result.path;
			closure.run(uri);
		}

		@Override
		public void onError(int errorCode, String description) {
			System.out.println("Erreur lors de la récupération " + errorCode + " : " + description);
		}

	}

	public DlnaButton() {
		initDlnaListener();
		initButtons();
	}

	public DlnaButton(DetailsMovie detailsMovie) {
		this();

		this.setDetailsMovie(detailsMovie);


	}
	/**
	 * set the details movie
	 * @param detailsMovie
	 */
	public void setDetailsMovie(DetailsMovie detailsMovie) {
		this.detailsItem = detailsMovie;

		item = new PlaylistType.Item();
        item.movieid = detailsMovie.movieid;
	}

	/**
	 * set the details episode
	 * @param detailsMovie
	 */
	public void setDetailsEpisode (DetailsEpisode detailsEpisode) {
		this.detailsItem = detailsEpisode;
		if (this.detailsItem != null) {
			item = new PlaylistType.Item();
			item.episodeid = detailsEpisode.episodeid;
		} else {
			item = null;
		}
	}

	/**
	 * init the dlna listener
	 * add menu item on ContextMenu
	 * @param detailsItem
	 */
	private void initDlnaListener() {
		DLNAService.addListener(new DlnaListener() {
			private final ServiceId[] neededServices = new ServiceId[]{new UDAServiceId("AVTransport")};
			private Map<String, MenuItem> menus = new HashMap<>();

			@Override
			public ServiceId[] getNeededServices() {
				return neededServices;
			}

			@Override
			public void deviceRemoved(RemoteDevice device) {
				MenuItem item = menus.remove(device.getDetails().getSerialNumber());
				contextMenu.getItems().remove(item);
			}

			@Override
			public void deviceAdded(RemoteDevice device) {
				MenuItem menu = new MenuItem(device.getDetails().getFriendlyName());
				menu.setOnAction((e) -> {
					PrepareDownload prepare = new PrepareDownload(detailsItem.file);

					prepare.execute(
						HostManager.getInstance().getConnection(),
						new VideoLauncher((uri) -> {
							String metaData = initMetadata(uri, detailsItem.runtime, detailsItem.art.poster, detailsItem.title);
							RemoteService service = device.findService(new UDAServiceId("AVTransport"));

			                final ActionCallback playAction =
			                        new Play(service) {
			                            @Override
			                            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			                                System.out.println("fail play");
			                                System.out.println(operation.getResponseDetails() + " : " + operation.getStatusMessage());
			                                System.out.println(defaultMsg);
			                            }

			                            @Override
			                            public void success(ActionInvocation invocation) {
			                                System.out.println("success play");
			                            }

			                        };

			                final ActionCallback setTargetInvocation = new SetAVTransportURI(service, uri.toString(), metaData) {

			                    @Override
			                    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
			                        System.out.println("fail SetTargetInvocation");
			                        System.out.println(operation.getResponseDetails() + " : " + operation.getStatusMessage());
			                        System.out.println(defaultMsg);
			                    }

			                    @Override
			                    public void success(ActionInvocation invocation) {
			                        System.out.println("OK SetTargetInvocation");
					                DLNAService.getControlPoint().execute(playAction);
			                    }
			                };

			                DLNAService.getControlPoint().execute(setTargetInvocation);
						}) ,
						new Handler()
					);
				});
				menus.put(device.getDetails().getSerialNumber(), menu);
				contextMenu.getItems().add(menu);
			}
		});
	}

	private final String META_DATA = "<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\">	<item id=\"video/all_videos/1764\" parentID=\"video/all_videos\" restricted=\"1\">		<dc:title>%title%</dc:title>		<dc:creator>unknown</dc:creator>		<upnp:artist>unknown</upnp:artist>		<upnp:actor>unknown</upnp:actor>		<upnp:author>unknown</upnp:author>		<upnp:genre>Unknown</upnp:genre>		<upnp:albumArtURI>			%fanart%		</upnp:albumArtURI>				<res duration=\"%duration%\" size=\"5510872\" protocolInfo=\"http-get:*:video/mp4:DLNA.ORG_PN=AVC_MP4_BL_L3L_SD_AAC;DLNA.ORG_OP=01;DLNA.ORG_FLAGS=01700000000000000000000000000000\">			%uri%		</res>		<upnp:class>object.item.videoItem</upnp:class>	</item></DIDL-Lite>";
	private String initMetadata(String uri, Integer duration, String fanart, String title) {
		int hour = duration / 3600;
		int minutes = (duration % 3600) / 60;
		int secondes = ((duration % 3600) % 60);
		return META_DATA.replace("%fanart%", fanart)
						.replace("%uri%", uri)
						.replace("%duration%", hour+":"+minutes+":"+secondes)
						.replace("%title%", title);
	}
	/**
	 * init all buttons
	 * @param detailsMovie
	 */
	private void initButtons() {
		MenuItem xbmc = new MenuItem("XBMC");
		contextMenu.getItems().add(xbmc);

		play = new Button("Lancer");
		play.getStyleClass().add("play-button-left");
		play.getStyleClass().add("play-button");

		more = new Button(" ^ ");
		more.getStyleClass().add("play-button-right");
		more.getStyleClass().add("play-button");

		more.setOnAction((e) -> {
			if (!contextMenu.isShowing()) {
				contextMenu.show(more, Side.TOP, 0, 0);
			} else {
				contextMenu.hide();
			}
		});

		this.getChildren().add(play);
		this.getChildren().add(more);



		play.setOnAction((e) -> {
			// récupération de l'url de la video
			PrepareDownload prepare = new PrepareDownload(detailsItem.file);
			prepare.execute(
				HostManager.getInstance().getConnection(),
				new VideoLauncher((uri) -> TransitionManager.showPlayer(uri, detailsItem.label)) ,
				new Handler()
			);
		});

		xbmc.setOnAction((e) -> {
			// lancement de la video sur xbmc

			Player.Open action = new Player.Open(item);
			action.execute(HostManager.getInstance().getConnection(), new ApiCallback<String>() {
				@Override
				public void onSuccess(String result) {
					System.out.println("Lancé avec succès");
				}

				@Override
				public void onError(int errorCode, String description) {
					System.out.println("Erreur impossible de lancer la video");
				}
			}, new Handler());
		});
	}
}
