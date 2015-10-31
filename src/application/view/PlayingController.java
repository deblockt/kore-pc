package application.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.support.avtransport.callback.GetMediaInfo;
import org.fourthline.cling.support.avtransport.callback.GetPositionInfo;
import org.fourthline.cling.support.avtransport.callback.GetTransportInfo;
import org.fourthline.cling.support.avtransport.callback.Pause;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.Seek;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.SeekMode;
import org.fourthline.cling.support.model.TransportInfo;
import org.xbmc.kore.host.HostConnectionObserver;
import org.xbmc.kore.host.HostConnectionObserver.PlayerEventsObserver;
import org.xbmc.kore.host.HostInfo;
import org.xbmc.kore.host.HostManager;
import org.xbmc.kore.jsonrpc.ApiCallback;
import org.xbmc.kore.jsonrpc.Handler;
import org.xbmc.kore.jsonrpc.method.Player.PlayPause;
import org.xbmc.kore.jsonrpc.type.ListType.ItemsAll;
import org.xbmc.kore.jsonrpc.type.PlayerType;
import org.xbmc.kore.jsonrpc.type.PlayerType.GetActivePlayersReturnType;
import org.xbmc.kore.jsonrpc.type.PlayerType.PositionTime;
import org.xbmc.kore.jsonrpc.type.PlayerType.PropertyValue;
import org.xbmc.kore.jsonrpc.type.PlayerType.SeekReturnType;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsFile;

import application.service.DLNAService;
import application.service.DLNAService.DlnaListener;
import application.service.VideosLists;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

    private Boolean loaded = false;




    @FXML
    private void initialize() {
        VideosLists.INSTANCE.getFilmList((list) -> {
            System.out.println("films loaded");
            videos.addAll(list);
            synchronized (loaded) {
                if (loaded) {
                    System.out.println("run dlna search");
                    load();
                }
                loaded = true;
            }
        });

        VideosLists.INSTANCE.getAllTvShowEpisods((list) -> {

            System.out.println("episodes loaded " + list.size());
            videos.addAll(list);
            synchronized (loaded) {
                if (loaded) {
                    System.out.println("run dlna search");
                    load();
                }
                loaded = true;
            }
        });

        backButton.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
            TransitionManager.showVideoList(VideoType.VIDEO);
        });


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
                if (detail == null) {
                    String uri = getItemResult.file;
                    String httpUri = HostManager.getInstance().getCurrentHostInfo().getHttpURL();
                    DetailsFile details =  getDetailsFile(httpUri+uri);
                    if (details != null) {
                        detail = new PlayingDetailSubscreenPane(details);
                        detail.addStopListener(e -> {
                        	org.xbmc.kore.jsonrpc.method.Player.Stop stop = new org.xbmc.kore.jsonrpc.method.Player.Stop(getActivePlayerResult.playerid);
                        	stop.execute(HostManager.getInstance().getConnection(), new ApiCallback<String>() {
								@Override
								public void onSuccess(String result) {}
								@Override
								public void onError(int errorCode, String description) {}
							}, new Handler());
                        });
                        detail.addPlayListener(e -> {
                        	if (detail.isPaused()) {
	                        	PlayPause stop = new PlayPause(getActivePlayerResult.playerid);
	                        	stop.execute(HostManager.getInstance().getConnection(), new ApiCallback<Integer>() {
									@Override
									public void onSuccess(Integer result) {}
									@Override
									public void onError(int errorCode, String description) {}
								}, new Handler());
                        	}
                        });
                        detail.addPauseListener(e -> {
                        	if (!detail.isPaused()) {
	                        	PlayPause stop = new PlayPause(getActivePlayerResult.playerid);
	                        	stop.execute(HostManager.getInstance().getConnection(), new ApiCallback<Integer>() {
									@Override
									public void onSuccess(Integer result) {}
									@Override
									public void onError(int errorCode, String description) {}
								}, new Handler());
                        	}
                        });
                        detail.addSeekListener((observable, oldValue, newValue) -> {
                        	System.out.println("call seek");
                        	int hours = newValue.intValue() / 3600;
                            int minutes = (newValue.intValue() % 3600) / 60;
                            int seconds = ((newValue.intValue() % 3600) % 60);

                        	PositionTime positionTime = new PositionTime(hours, minutes, seconds, 0);
                        	org.xbmc.kore.jsonrpc.method.Player.Seek seek = new org.xbmc.kore.jsonrpc.method.Player.Seek(getActivePlayerResult.playerid, positionTime);

                        	seek.execute(HostManager.getInstance().getConnection(), new ApiCallback<PlayerType.SeekReturnType>() {
								@Override
								public void onSuccess(SeekReturnType result) {}
								@Override
								public void onError(int errorCode, String description) {}
							}, new Handler());
                        });
                        tabPane.getTabs().add(tab);
                        tab.setContent(detail);
                    }
                }

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
        Tab tab = new Tab(device.getDetails().getFriendlyName());

        String uri = mediaInfo.getCurrentURI();
        try {
            uri = java.net.URLDecoder.decode(uri, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        DetailsFile details =  getDetailsFile(uri);
        if (details != null) {
            tabPane.getTabs().add(tab);
            tabs.put(device.getDetails().getSerialNumber(), tab);
            PlayingDetailSubscreenPane detail = new PlayingDetailSubscreenPane(details);

            GetPositionInfo positionInfo = new GetPositionInfo(service) {

                @Override
                public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                    System.out.println("erreur GETPOSITIONINFO " + arg1 + "," + arg2);
                }

                @Override
                public void received(ActionInvocation arg0, PositionInfo arg1) {
                    // convert to second
                    String relTime = arg1.getRelTime();

                    if (relTime != null) {
                        String[] splited = relTime.split(":");
                        int second = Integer.parseInt(splited[0]) * 3600 + Integer.parseInt(splited[1]) * 60 + Integer.parseInt(splited[2]);
                        detail.setPosition(second);
                    }
                }
            };
            DLNAService.getControlPoint().execute(positionInfo);

            GetTransportInfo transportInfo = new GetTransportInfo(service) {

                @Override
                public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {}

                @Override
                public void received(ActionInvocation arg0, TransportInfo arg1) {
                    System.out.println("getCurrentTransportState " + arg1.getCurrentTransportState().getValue());
                    System.out.println("getCurrentTransportState " + arg1.getCurrentTransportStatus().getValue());
                    System.out.println("getInputMap " + arg0.getInputMap());
                }
            };
            DLNAService.getControlPoint().execute(transportInfo);

            detail.addSeekListener((observable, oldValue, newValue) -> {
            	Seek seek = new Seek(service, SeekMode.REL_TIME, formatDuration(newValue.intValue())) {
                    @Override
                    public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                        System.out.println("Failure ");
                    }
                };
                System.out.println("Send seek " + formatDuration(newValue.intValue()));
                DLNAService.getControlPoint().execute(seek);
            });

            detail.addPlayListener((e) -> {
                try {
                    Play play = new Play(service) {
                        @Override
                        public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                            System.out.println("Erreur : " + arg1 + "," + arg1);
                        }
                    };
                    DLNAService.getControlPoint().execute(play);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            detail.addPauseListener((e) -> {
                try {
                    Pause pause = new Pause(service) {
                        @Override
                        public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                            System.out.println("Erreur : " + arg1 + "," + arg1);
                        }
                    };
                    DLNAService.getControlPoint().execute(pause);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            detail.addStopListener((e) -> {
                Stop stop = new Stop(service) {
                    @Override
                    public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                        System.out.println("Erreur : " + arg1 + "," + arg1);
                    }
                };
                DLNAService.getControlPoint().execute(stop);
            });
            tab.setContent(detail);
        }
    }

    public DetailsFile getDetailsFile(String uri) {
        String httpUri = HostManager.getInstance().getCurrentHostInfo().getHttpURL();

        if (uri.startsWith(httpUri)) {

            String[] splited = uri.split("/");
            String filename = splited[splited.length - 1];
            for (DetailsFile detailsTVShow : videos) {
                String[] splited2 = detailsTVShow.file.split("/");
                if (filename.equals(splited2[splited2.length - 1])) {
                    return detailsTVShow;
                }
            }
        }

        return null;
    }

    private String formatDuration(int duration) {
        int hour = duration / 3600;
        int minutes = (duration % 3600) / 60;
        int secondes = ((duration % 3600) % 60);

        return StringUtils.leftPad(String.valueOf(hour), 2, "0") + ":" +
        StringUtils.leftPad(String.valueOf(minutes), 2, "0") + ":" +
        StringUtils.leftPad(String.valueOf(secondes), 2, "0");
    }
}
