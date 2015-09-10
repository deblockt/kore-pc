package application.view;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.xbmc.kore.host.HostInfo;
import org.xbmc.kore.host.HostManager;
import org.xbmc.kore.jsonrpc.ApiCallback;
import org.xbmc.kore.jsonrpc.Handler;
import org.xbmc.kore.jsonrpc.HostConnection;
import org.xbmc.kore.jsonrpc.method.JSONRPC.Ping;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class KodyListController {

	private List<HostInfoPane> hostInfoPanes = new ArrayList<>();

	@FXML
	private Accordion kodyList;

	@FXML
	private ImageView loadingImage;

	@FXML
	private Button validateButton;

	@FXML
	public void initialize() {
		validateButton.setOnAction((e) -> checkValidity());
		loadingImage.setImage(new Image(this.getClass().getResourceAsStream("/images/loading.gif")));
	}

	/**
	 * check info validity and show error if error
	 */
	private void checkValidity() {
		List<HostInfoPane> valide = new ArrayList<>();
		List<HostInfoPane> invalide = new ArrayList<>();
		// check all hostInfo
		IntegerProperty numberTestFinish = new SimpleIntegerProperty();
		numberTestFinish.addListener((event, oldValue, newValue) -> {
			new Handler().post(new Runnable() {

				@Override
				public void run() {
					if (hostInfoPanes.size() ==  newValue.intValue()) {
						loadingImage.setVisible(false);
						if (valide.isEmpty()) {
							Alert alert = new Alert(AlertType.ERROR);
							alert.setTitle("Erreur");
							alert.setHeaderText("Desolé, nous n'arrivons pas à contacter votre serveur Kody (xbmc)");
							alert.setContentText("Merci de vérifier que le serveur soit correctement allumé, et les informations indiquées.");

							alert.showAndWait();
						} else if (!invalide.isEmpty()){
							Alert alert = new Alert(AlertType.CONFIRMATION);
							alert.setTitle("Attention");
							alert.setHeaderText("Certain serveurs Kody (xbmc) sont inaccessible.");
							alert.setContentText("Voulez vous tout de même continuer ?");

							Optional<ButtonType> result = alert.showAndWait();
							if (result.get() == ButtonType.OK){
								TransitionManager.showVideoList(VideoType.VIDEO);
								TransitionManager.maximize();
								for (HostInfoPane hostInfoPane : valide) {
									HostManager.getInstance().addHost(hostInfoPane.getNewHostInfo());
								}
								for (HostInfoPane hostInfoPane : invalide) {
									HostManager.getInstance().addHost(hostInfoPane.getNewHostInfo());
								}
							}
						} else {
							for (HostInfoPane hostInfoPane : valide) {
								HostManager.getInstance().addHost(hostInfoPane.getNewHostInfo());
							}
							TransitionManager.showVideoList(VideoType.VIDEO);
							TransitionManager.maximize();
						}
					}
				}


			});

		});
		loadingImage.setVisible(true);
		for (HostInfoPane hostInfoPane : hostInfoPanes) {
			HostInfo hostInfo = hostInfoPane.getNewHostInfo();
			Ping ping = new Ping();
			ping.execute(new HostConnection(hostInfo), new ApiCallback<String>() {

				@Override
				public void onSuccess(String result) {
					synchronized(numberTestFinish) {
						valide.add(hostInfoPane);
						numberTestFinish.setValue(numberTestFinish.get() + 1);
					}
				}

				@Override
				public void onError(int errorCode, String description) {
					synchronized(numberTestFinish) {
						System.out.println("error " + errorCode + " : " + description);
						invalide.add(hostInfoPane);
						numberTestFinish.setValue(numberTestFinish.get() + 1);
					}
				}
			}, new Handler());
		}
	}

	// create all default kody information
	public void setListKodyIp(List<String> rechable) {
		for (String string : rechable) {
			String name = string;
			try {
				name = InetAddress.getByName(string).getHostName();
			} catch (Exception e) {}

			HostInfo hostInfo = new HostInfo(
					name,
					string,
					HostConnection.PROTOCOL_HTTP,
					HostInfo.DEFAULT_HTTP_PORT,
					HostInfo.DEFAULT_TCP_PORT,
					"",
					"",
					false,
					HostInfo.DEFAULT_EVENT_SERVER_PORT
					);

			HostInfoPane infoPane = new HostInfoPane(hostInfo);
			TitledPane pane = new TitledPane(name, infoPane);
			pane.getStyleClass().add("background");
			infoPane.getStyleClass().add("background");
			kodyList.getPanes().add(pane);

			hostInfoPanes.add(infoPane);
		}
	}

}
