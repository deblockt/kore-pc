package org.xbmc.kore.jsonrpc;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Handler {
	// TODO paramétrer le nombre de thread
	private final static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5);

	public void post(Runnable r) {
		//executor.execute(r);
		javafx.application.Platform.runLater(r);
	}

	public void postDelayed(Runnable runnable, int delay) {
		executor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
	}

	public void removeCallbacks(Runnable tcpCheckerRunnable) {
		executor.remove(tcpCheckerRunnable);
	}
}
