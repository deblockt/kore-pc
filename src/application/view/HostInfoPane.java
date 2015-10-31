package application.view;

import java.io.IOException;

import org.xbmc.kore.host.HostInfo;
import org.xbmc.kore.jsonrpc.HostConnection;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class HostInfoPane extends GridPane {

	@FXML
	private TextField name;

	@FXML
	private TextField host;

	@FXML
	private ComboBox<String> protocol;

	@FXML
	private Spinner<Integer> httpPort;

	@FXML
	private Spinner<Integer> tcpPort;

	@FXML
	private Spinner<Integer> eventPort;

	@FXML
	private TextField username;

	@FXML
	private TextField password;

	private final HostInfo hostInfos;

	public HostInfoPane(final HostInfo hostInfos) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("HostInfoPane.fxml"));

		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		protocol.getItems().add("TCP");
		protocol.getItems().add("HTTP");

		httpPort.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, hostInfos.getHttpPort()));
		tcpPort.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, hostInfos.getTcpPort()));
		eventPort.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, hostInfos.getEventServerPort()));

		name.setText(hostInfos.getName());
		host.setText(hostInfos.getAddress());
		protocol.getSelectionModel().select(hostInfos.getProtocol());
		username.setText(hostInfos.getUsername());
		password.setText(hostInfos.getPassword());
		this.hostInfos = hostInfos;
	}

	public HostInfo getOldHostInfo() {
		return this.hostInfos;
	}

	public HostInfo getNewHostInfo() {
		return new HostInfo(
				name.getText(),
				host.getText(),
				"HTTP".equals(protocol.getValue()) ? HostConnection.PROTOCOL_HTTP : HostConnection.PROTOCOL_TCP,
				httpPort.getValue(),
				tcpPort.getValue(),
				username.getText(),
				password.getText(),
				true,
				eventPort.getValue()
			);
	}
}
