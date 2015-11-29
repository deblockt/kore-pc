package application.view.playingController;

import org.xbmc.kore.host.HostManager;
import org.xbmc.kore.jsonrpc.ApiCallback;
import org.xbmc.kore.jsonrpc.Handler;
import org.xbmc.kore.jsonrpc.method.Player.PlayPause;
import org.xbmc.kore.jsonrpc.type.PlayerType;
import org.xbmc.kore.jsonrpc.type.PlayerType.GetActivePlayersReturnType;
import org.xbmc.kore.jsonrpc.type.PlayerType.PositionTime;
import org.xbmc.kore.jsonrpc.type.PlayerType.PropertyValue;
import org.xbmc.kore.jsonrpc.type.PlayerType.SeekReturnType;
import org.xbmc.kore.jsonrpc.type.VideoType.DetailsFile;

public class KodyPlayingSubscreenPane extends PlayingDetailSubscreenPane {

	public KodyPlayingSubscreenPane(DetailsFile details, GetActivePlayersReturnType getActivePlayerResult, PropertyValue getPropertiesResult) {
		super(details);

		this.addStopListener(e -> {
        	org.xbmc.kore.jsonrpc.method.Player.Stop stop = new org.xbmc.kore.jsonrpc.method.Player.Stop(getActivePlayerResult.playerid);
        	stop.execute(HostManager.getInstance().getConnection(), new ApiCallback<String>() {
				@Override
				public void onSuccess(String result) {}
				@Override
				public void onError(int errorCode, String description) {}
			}, new Handler());
        });
        this.addPlayListener(e -> {
        	if (isPaused()) {
            	PlayPause stop = new PlayPause(getActivePlayerResult.playerid);
            	stop.execute(HostManager.getInstance().getConnection(), new ApiCallback<Integer>() {
					@Override
					public void onSuccess(Integer result) {}
					@Override
					public void onError(int errorCode, String description) {}
				}, new Handler());
        	}
        });
        this.addPauseListener(e -> {
        	if (!isPaused()) {
            	PlayPause stop = new PlayPause(getActivePlayerResult.playerid);
            	stop.execute(HostManager.getInstance().getConnection(), new ApiCallback<Integer>() {
					@Override
					public void onSuccess(Integer result) {}
					@Override
					public void onError(int errorCode, String description) {}
				}, new Handler());
        	}
        });
        this.addSeekListener((observable, oldValue, newValue) -> {
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

        this.setPosition(getPropertiesResult.time.hours * 3600 +
                getPropertiesResult.time.minutes * 60 +
                getPropertiesResult.time.seconds
                );

	}

}
