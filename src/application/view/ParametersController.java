package application.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.xbmc.kore.host.HostInfo;
import org.xbmc.kore.host.HostManager;
import org.xbmc.kore.jsonrpc.HostConnection;

import application.service.ParameterNames;
import application.service.ParameterService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ParametersController {

	@FXML
	private TextField videoPlayInput;

	@FXML
	private Button okButton;

	@FXML
	private Button cancelButton;

	@FXML
	public Button addButton;

	@FXML
	private Button removeButton;

	@FXML
	private ListView<String> kodyList;

	@FXML
	private Pane hostInfoPaneContent;

	@FXML
	private ComboBox<String> kodyComboBox;

	private List<HostInfoPane> editedHostInfo = new ArrayList<>();

	@FXML
	private void initialize() {
		HostManager manager = HostManager.getInstance();

		String playerVideoPath = ParameterService.getInstance().getString(ParameterNames.VIDEO_PLAYER_PATH);
		videoPlayInput.setText(playerVideoPath);

		okButton.setOnAction((event) -> {
			// save host info
			for (HostInfoPane hostInfoPane : editedHostInfo) {
				manager.editHost(hostInfoPane.getOldHostInfo(), hostInfoPane.getNewHostInfo());
			}
			int infoIndex = kodyComboBox.getSelectionModel().getSelectedIndex();

			HostInfo selectedInfo = manager.getListHostInfo().get(infoIndex);
			if (selectedInfo != null && !selectedInfo.getAddress().equals(manager.getCurrentHostInfo().getAddress())) {
				manager.setCurrentHostInfo(selectedInfo);
				ParameterService.getInstance().setParameter(ParameterNames.SELECTED_HOST, infoIndex);
			}

			ParameterService.getInstance().setParameter(ParameterNames.VIDEO_PLAYER_PATH, videoPlayInput.getText());
			ParameterService.getInstance().save();
			Stage stage = (Stage) okButton.getScene().getWindow();
			stage.close();
		});

		cancelButton.setOnAction((event) -> {
			Stage stage = (Stage) cancelButton.getScene().getWindow();
			stage.close();
		});

		List<HostInfo> infos = manager.getListHostInfo();
		addButton.setOnAction((event) -> {
			HostInfo hostInfo = new HostInfo(
				"Nouveau",
				"0.0.0.0",
				HostConnection.PROTOCOL_HTTP,
				HostInfo.DEFAULT_HTTP_PORT,
				HostInfo.DEFAULT_TCP_PORT,
				"",
				"",
				false,
				HostInfo.DEFAULT_EVENT_SERVER_PORT
			);
			kodyList.getItems().add(hostInfo.getName());
			kodyComboBox.getItems().add(hostInfo.getName());
			infos.add(hostInfo);
			kodyList.getSelectionModel().select(infos.size() - 1);
		});


		removeButton.setOnAction((event) -> {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirmation");
			alert.setHeaderText("Voulez vous vraiment suppimer " + kodyList.getSelectionModel().getSelectedItem());

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
				int index = kodyList.getSelectionModel().getSelectedIndex();
				manager.removeHost(index);
				kodyComboBox.getItems().remove(index);
				kodyList.getItems().remove(index);
				kodyList.getSelectionModel().selectFirst();
				editedHostInfo.remove(index);
			}
		});

		kodyList.getSelectionModel().selectedIndexProperty().addListener((e, oldValue, newValue) -> {
			HostInfo hostInfo = infos.get(newValue.intValue());
			HostInfoPane infoPane = new HostInfoPane(hostInfo);
			hostInfoPaneContent.getChildren().clear();
			hostInfoPaneContent.getChildren().add(infoPane);
			editedHostInfo.add(infoPane);
		});


		// init kody server list
		for (HostInfo hostInfo : infos) {
			kodyList.getItems().add(hostInfo.getName());
			kodyComboBox.getItems().add(hostInfo.getName());
		}
		kodyList.getSelectionModel().select(0);
		kodyComboBox.getSelectionModel().select(ParameterService.getInstance().getInt(ParameterNames.SELECTED_HOST, 0));
	}
}
