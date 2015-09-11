package application.view;


import java.util.HashMap;
import java.util.Map;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.meta.Service;
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
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsItem;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsMovie;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsTVShow;

import application.service.DLNAService;
import application.service.DLNAService.DlnaListener;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;

public class DlnaButton extends HBox {

	private Button play;

	private Button more;

	private ContextMenu contextMenu = new ContextMenu();

	private DetailsItem detailsItem;
	
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
		
		PlaylistType.Item item = new PlaylistType.Item();
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

			                final ActionCallback setTargetInvocation = new SetAVTransportURI(service, uri.toString(), "NOT_IMPLEMENTED") {

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
				new VideoLauncher((uri) -> TransitionManager.showPlayer(uri)) ,
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
