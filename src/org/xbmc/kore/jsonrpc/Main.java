package org.xbmc.kore.jsonrpc;

import org.xbmc.kore.host.HostInfo;
import org.xbmc.kore.jsonrpc.method.JSONRPC.Ping;


public class Main {

	public static void main(String[] args) {
		// TODO regarder à quoi sert le server port
		HostInfo info = new HostInfo("pi",
				"192.168.4.11",
				HostConnection.PROTOCOL_HTTP,
				HostInfo.DEFAULT_HTTP_PORT,
				HostInfo.DEFAULT_TCP_PORT, "", "", false, HostInfo.DEFAULT_EVENT_SERVER_PORT);
		HostConnection connection = new HostConnection(info);

		Ping ping = new Ping();
		ping.execute(connection, new ApiCallback<String>() {

			@Override
			public void onSuccess(String result) {
				System.out.println("SUCCESS " + result);
			}

			@Override
			public void onError(int errorCode, String description) {
				System.out.println("ERROR " + errorCode +" : " + description);
			}

		}, new Handler());

	}
}
