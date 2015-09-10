package application.service;

public class ParameterNames {

	public static final String VIDEO_PLAYER_PATH = "videoPlayerPath";

	public static final String HOST_NAME = "host[{0}].name";

	public static final String HOST_IP = "host[{0}].ip";

	public static final String HOST_HTTP_PORT = "host[{0}].http_port";

	public static final String HOST_TCP_PORT = "host[{0}].tcp_port";

	public static final String HOST_USERNAME = "host[{0}].username";

	public static final String HOST_PASSWORD = "host[{0}].password";

	public static final String HOST_USEEVENT = "host[{0}].use_event_server";

	public static final String HOST_EVENT_PORT = "host[{0}].event_port";

	public static final String HOST_PROTOCOL = "host[{0}].protocol";

	public static final String SELECTED_HOST = "selectedHost";

	/**
	 * retourne le nom du paramètre indexé
	 *
	 * @param nameTemplate le nom du paramètre
	 * @param index la valeur de l'index
	 * @return
	 */
	public static String getIndexedName(String nameTemplate, Integer index) {
		return nameTemplate.replace("{0}", String.valueOf(index));
	}
}
