package application.view.playingController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.support.avtransport.callback.GetMediaInfo;
import org.fourthline.cling.support.model.MediaInfo;
import org.xbmc.kore.host.HostConnectionObserver;
import org.xbmc.kore.host.HostConnectionObserver.PlayerEventsObserver;
import org.xbmc.kore.host.HostInfo;
import org.xbmc.kore.host.HostManager;
import org.xbmc.kore.jsonrpc.Handler;
import org.xbmc.kore.jsonrpc.type.ListType.ItemsAll;
import org.xbmc.kore.jsonrpc.type.PlayerType.GetActivePlayersReturnType;
import org.xbmc.kore.jsonrpc.type.PlayerType.PropertyValue;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsFile;

import application.service.Callback;
import application.service.DLNAService;
import application.service.DLNAService.DlnaListener;
import application.service.VideosLists;
import application.view.TransitionManager;
import application.view.VideoType;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;

public class PlayingController  {

    @FXML
    private TabPane tabPane;

    @FXML
    private Label backButton;

    private Map<String, Tab> tabs = new HashMap<>();

    private List<DetailsFile> videos = new ArrayList<>();

    private Boolean tvOrVideoloaded = false;
    private Boolean allLoaded = false;
    private Map<Callback<DetailsFile>, String> callbackSearchFile = new HashMap<>();

    public PlayingController() {
    	System.out.println("PlayingController_constructor");
    }

    @FXML
    private void initialize() {

    	System.out.println("PlayingController_initialize");
        VideosLists.INSTANCE.getFilmList((list) -> {
            System.out.println("films loaded");
            videos.addAll(list);
            videoAdded(list);
            synchronized (tvOrVideoloaded) {
                if (tvOrVideoloaded) {
                    allLoaded = true;
                }
                tvOrVideoloaded = true;
            }
        }, null);

        VideosLists.INSTANCE.getAllTvShowEpisods((list) -> {
            videos.addAll(list);
            videoAdded(list);
            if (!VideosLists.INSTANCE.isLoadingEpisodes()) {
	            synchronized (tvOrVideoloaded) {
	                if (tvOrVideoloaded) {
	                    allLoaded = true;
	                }
	                tvOrVideoloaded = true;
	            }
            }
        }, null);

        backButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
            TransitionManager.showVideoList(VideoType.VIDEO);
        });

        load();
    }

    private void load() {
        DLNAService.addListener(new DlnaListener() {
            private final ServiceId[] neededServices = new ServiceId[]{new UDAServiceId("AVTransport")};

            @Override
            public ServiceId[] getNeededServices() {
                return neededServices;
            }

            @Override
            public void deviceRemoved(RemoteDevice device) {
                tabPane.getTabs().remove(tabs.remove(device.getDetails().getSerialNumber()));
            }

            @Override
            public void deviceAdded(RemoteDevice device) {
                RemoteService service = device.findService(new UDAServiceId("AVTransport"));

                GetMediaInfo mediaInfo = new GetMediaInfo(service) {

                    @Override
                    public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                        System.out.println("ERREUR LORS DE GETMEDIAINFO");
                    }

                    @Override
                    public void received(ActionInvocation arg0, MediaInfo mediaInfo) {

                        System.out.println(service.getDevice().getDetails().getFriendlyName() + " : MEDIA INFO " + mediaInfo.getCurrentURI() + ", "+ mediaInfo.getCurrentURIMetaData());
                        if (mediaInfo.getCurrentURI() != null) {
                            new Handler().post(() -> {
                                createDlnaTab(device, service, mediaInfo);
                            });
                        }
                    }
                };

                DLNAService.getControlPoint().execute(mediaInfo);
            }
        });

        // add XBMC
        createKodyTab();
    }

    private void createKodyTab() {
        HostInfo hostInfo = HostManager.getInstance().getCurrentHostInfo();

        Tab tab = new Tab(hostInfo.getName());
        tab.setContent(new LoadingContent());
        tabPane.getTabs().add(tab);
        System.out.println("add kody tab");
        HostConnectionObserver observer = new HostConnectionObserver(HostManager.getInstance().getConnection());
        observer.registerPlayerObserver(new PlayerEventsObserver() {
            private PlayingDetailSubscreenPane detail;

            @Override
            public void systemOnQuit() {

            }

            @Override
            public void playerOnStop() {
                tabPane.getTabs().remove(tab);
            }

            @Override
            public void playerOnPlay(GetActivePlayersReturnType getActivePlayerResult, PropertyValue getPropertiesResult,
                    ItemsAll getItemResult) {

            	// new screen init data
                if (detail == null) {
                    String uri = getItemResult.file;
                    String httpUri = HostManager.getInstance().getCurrentHostInfo().getHttpURL();
                    System.out.println("GET DETAIL FILE " + uri );
                    getDetailsFile(httpUri+uri, (details) -> {
                        detail = new KodyPlayingSubscreenPane(details, getActivePlayerResult, getPropertiesResult);
                        tab.setContent(detail);
                        System.out.println("MODIFICATION DU CONTENU DE L'ONGLET");
                    });
                }

                // the screen is already loaded set the cursor position
                if (detail != null) {
	                detail.play();
	                detail.setPosition(getPropertiesResult.time.hours * 3600 +
	                           getPropertiesResult.time.minutes * 60 +
	                           getPropertiesResult.time.seconds
	                           );
                }
            }

            @Override
            public void playerOnPause(GetActivePlayersReturnType getActivePlayerResult, PropertyValue getPropertiesResult,
                    ItemsAll getItemResult) {
                if (detail != null) {
                	detail.pause();
                	detail.setPosition(getPropertiesResult.time.hours * 3600 +
	                           getPropertiesResult.time.minutes * 60 +
	                           getPropertiesResult.time.seconds
	                           );
                }
            }

            @Override
            public void playerOnConnectionError(int errorCode, String description) {

            }

            @Override
            public void playerNoResultsYet() {

            }

            @Override
            public void observerOnStopObserving() {

            }

            @Override
            public void inputOnInputRequested(String title, String type, String value) {

            }
        }, true);



    }

    private void createDlnaTab(RemoteDevice device, RemoteService service, MediaInfo mediaInfo) {

        String uri = mediaInfo.getCurrentURI();
        try {
            uri = java.net.URLDecoder.decode(uri, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String httpUri = HostManager.getInstance().getCurrentHostInfo().getHttpURL();

        System.out.println("GET DETAIL FILE " + uri );
        if (uri.startsWith(httpUri)) {
        	Tab tab = new Tab(device.getDetails().getFriendlyName());
        	tab.setContent(new LoadingContent());

        	tabPane.getTabs().add(tab);
            tabs.put(device.getDetails().getSerialNumber(), tab);

            getDetailsFile(uri, (details) -> {
                PlayingDetailSubscreenPane detail = new DlnaPlayingSubscreenPane(details, service);
                tab.setContent(detail);
                System.out.println("MODIFICATION DU CONTENU DE L'ONGLET");
            });
        }


    }

    public void getDetailsFile(String uri, Callback<DetailsFile> callback) {
    	String httpUri = HostManager.getInstance().getCurrentHostInfo().getHttpURL();
        if (uri.startsWith(httpUri)) {
            boolean isAllLoaded = allLoaded;
        	String[] splited = uri.split("/");
            String filename = splited[splited.length - 1];
            for (DetailsFile detailsTVShow : new ArrayList<>(videos)) {
                if (isSameFile(filename, detailsTVShow.file)) {
                	callback.call(detailsTVShow);
                	return;
                }
            }

            if (!isAllLoaded) {
            	callbackSearchFile.put(callback, filename);
            }
        }

    }

    /**
     * check if the URL is the same files
     * @param name the filename
     * @param url the url
     * @return
     */
    private boolean isSameFile(String name, String url) {
    	 String[] splited2 = url.split("/");
         return name.equals(splited2[splited2.length - 1]);
    }

    /**
     * call when a video is added call callback is the video is readed
     * @param list
     */
    private void videoAdded(List<? extends DetailsFile> list) {
    	if (callbackSearchFile.size() == 0) {
    		return;
    	}

    	for (DetailsFile detailsFile : list) {
    		for (Entry<Callback<DetailsFile>, String> entry : callbackSearchFile.entrySet()) {
				if (isSameFile(entry.getValue(), detailsFile.file)) {
					entry.getKey().call(detailsFile);
				}
			}
		}
    }



}
