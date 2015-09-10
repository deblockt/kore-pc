package application.view;

import java.util.ArrayList;
import java.util.List;

import org.xbmc.kore.host.HostInfo;
import org.xbmc.kore.host.HostManager;

import application.service.ParameterNames;
import application.service.ParameterService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
	private ListView<String> kodyList;

	@FXML
	private Pane hostInfoPaneContent;

	@FXML
	private ComboBox<String> kodyComboBox;

	private List<HostInfoPane> editedHostInfo = new ArrayList<>();



	@FXML
	private void initialize() {
		String playerVideoPath = ParameterService.getInstance().getString(ParameterNames.VIDEO_PLAYER_PATH);
		videoPlayInput.setText(playerVideoPath);

		okButton.setOnAction((event) -> {
			// save host info
			for (HostInfoPane hostInfoPane : editedHostInfo) {
				HostManager.getInstance().addHost(hostInfoPane.getNewHostInfo());
			}
			ParameterService.getInstance().setParameter(ParameterNames.SELECTED_HOST, kodyComboBox.getSelectionModel().getSelectedIndex());
			ParameterService.getInstance().setParameter(ParameterNames.VIDEO_PLAYER_PATH, videoPlayInput.getText());
			ParameterService.getInstance().save();
			Stage stage = (Stage) okButton.getScene().getWindow();
			stage.close();
		});

		cancelButton.setOnAction((event) -> {
			Stage stage = (Stage) cancelButton.getScene().getWindow();
			stage.close();
		});

		List<HostInfo> infos = HostManager.getInstance().getListHostInfo();
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
