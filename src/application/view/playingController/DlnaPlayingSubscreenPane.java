package application.view.playingController;

import org.apache.commons.lang3.StringUtils;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.support.avtransport.callback.GetPositionInfo;
import org.fourthline.cling.support.avtransport.callback.GetTransportInfo;
import org.fourthline.cling.support.avtransport.callback.Pause;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.Seek;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.SeekMode;
import org.fourthline.cling.support.model.TransportInfo;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsFile;

import application.service.DLNAService;

public class DlnaPlayingSubscreenPane extends PlayingDetailSubscreenPane {

	public DlnaPlayingSubscreenPane(DetailsFile details, RemoteService service) {
		super(details);
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
                    setPosition(second);
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

        this.addSeekListener((observable, oldValue, newValue) -> {
        	Seek seek = new Seek(service, SeekMode.REL_TIME, formatDuration(newValue.intValue())) {
                @Override
                public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                    System.out.println("Failure ");
                }
            };
            System.out.println("Send seek " + formatDuration(newValue.intValue()));
            DLNAService.getControlPoint().execute(seek);
        });

        this.addPlayListener((e) -> {
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
        this.addPauseListener((e) -> {
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
        this.addStopListener((e) -> {
            Stop stop = new Stop(service) {
                @Override
                public void failure(ActionInvocation arg0, UpnpResponse arg1, String arg2) {
                    System.out.println("Erreur : " + arg1 + "," + arg1);
                }
            };
            DLNAService.getControlPoint().execute(stop);
        });
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
