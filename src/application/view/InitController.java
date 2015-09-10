package application.view;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.xbmc.kore.host.HostInfo;
import org.xbmc.kore.jsonrpc.ApiCallback;
import org.xbmc.kore.jsonrpc.Handler;
import org.xbmc.kore.jsonrpc.HostConnection;
import org.xbmc.kore.jsonrpc.method.JSONRPC.Ping;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class InitController {
	@FXML
	private ImageView loadingImage;

	private IntegerProperty numberTestFinish = new SimpleIntegerProperty();

	private List<String> rechable = new LinkedList<>();

	private final static Integer NUMBER_IP_TEST = 256;

	@FXML
	private void initialize() {
		loadingImage.setImage(new Image(this.getClass().getResourceAsStream("/images/loading.gif")));

		// lancement de la recherche sur le reseau
		numberTestFinish.set(0);
		numberTestFinish.addListener((e, oldValue, newValue) -> {
			System.out.println(newValue);
			// scan finished
			if (NUMBER_IP_TEST.equals(newValue)) {
				System.out.println("finish");
				loadingImage.setVisible(false);
				TransitionManager.showKodyList(rechable);
			}
		});
		try {
			loadRechableKody();
		} catch (SocketException e) {
			// TODO Impossible d'effectuer une recherche automatique
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @throws SocketException
	 */
	private void loadRechableKody() throws SocketException {
		final InetAddress inetAddress = getWLANipAddress();
		final String ipAddress = inetAddress.toString();
		final String templateipAddress = ipAddress.substring(1, ipAddress.lastIndexOf('.')) + ".";

		for (int i = 0; i < NUMBER_IP_TEST; i++) {
			final String otherAddress = templateipAddress + String.valueOf(i);

			HostInfo info = new HostInfo(
				"test",
				otherAddress,
				HostConnection.PROTOCOL_HTTP,
				HostInfo.DEFAULT_HTTP_PORT,
				HostInfo.DEFAULT_TCP_PORT,
				"",
				"",
				false,
				HostInfo.DEFAULT_EVENT_SERVER_PORT
			);

			Ping ping = new Ping();
			ping.execute(new HostConnection(info), new ApiCallback<String>() {

				@Override
				public void onSuccess(String result) {
					rechable.add(otherAddress);
					synchronized(numberTestFinish) {
						numberTestFinish.set(numberTestFinish.get() + 1);
					};
				}

				@Override
				public void onError(int errorCode, String description) {
					synchronized(numberTestFinish) {
						numberTestFinish.set(numberTestFinish.get() + 1);
					}
				}
			}, new Handler());
		}

	}

	/**
	 * return ipv4 ip
	 * @return
	 * @throws SocketException
	 */
	public static InetAddress getWLANipAddress() throws SocketException {
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface netint : Collections.list(nets)) {
			if (netint.isUp() && !netint.isLoopback() && !netint.isVirtual()) {
				Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
				for (InetAddress inetAddress : Collections.list(inetAddresses)) {
					if (inetAddress instanceof Inet4Address) {
						return inetAddress;
					}
				}
			}
		}
		return null;
	}
}
