package application.service;

import java.util.ArrayList;
import java.util.List;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

public class DLNAService {
	/**
	 * service d'acces au DLNA
	 */
	private static final UpnpService UPNP_SERVICE = new UpnpServiceImpl();

	private static final List<DlnaListener> LISTENERS = new ArrayList<>();

	/**
	 * start the service if not started not dlna can be discovered
	 */
	public static void start() {
		// Add a listener for device registration events
		UPNP_SERVICE.getRegistry().addListener(new DefaultRegistryListener() {
			@Override
			public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
				System.out.println("device added " + device.getDetails().getFriendlyName() + " ! ");
				for (DlnaListener listener: LISTENERS) {
	                if (mustCallListener(listener, device)) {
	                    listener.deviceAdded(device);
	                }
	            }
			}

			@Override
			public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
				for (DlnaListener listener: LISTENERS) {
	                if (mustCallListener(listener, device)) {
	                    listener.deviceRemoved(device);
	                }
	            }
			}
		});

		// Broadcast a search message for all devices
		UPNP_SERVICE.getControlPoint().search(new STAllHeader());
	}

	/**
     * add a listener this listeners is call for each device found with this services
     *
     * @param listener the listener
     *
     */
    public static void addListener(final DlnaListener listener) {
    	LISTENERS.add(listener);

        // check for all existing devices
        for (final RemoteDevice device : UPNP_SERVICE.getRegistry().getRemoteDevices()) {
            if (mustCallListener(listener, device)) {
                listener.deviceAdded(device);
            }
        }
    }

    /**
     * remove a listener
     *
     * @param listener the listener to remove
     */
    public static void removeListener(final DlnaListener listener) {
    	LISTENERS.remove(listener);
    }

    /**
     * check if the device need to be send to the listener
     *
     * @param listener the listener
     * @param device the device
     *
     * @return true if the listener must be called
     */
    private static boolean mustCallListener(final DlnaListener listener, final RemoteDevice device) {
        for (final ServiceId serviceId : listener.getNeededServices()) {
            final Service<?, ?> service = device.findService(serviceId);
            if (service == null) {
                return false;
            }
        }

        return true;
    }

    /**
     * return the control point
     * @return
     */
	public static ControlPoint getControlPoint() {
		return UPNP_SERVICE.getControlPoint();
	}

	/**
     * Listener use for dlna device
     */
    public interface DlnaListener {
        /**
         * return the require services for this listener
         * @return
         */
        ServiceId[] getNeededServices();

        /**
         * call when a device is added
         * @param device the added device
         */
        void deviceAdded(final RemoteDevice device);

        /**
         * call when a device is removed
         * @param device the removed device
         */
        void deviceRemoved(final RemoteDevice device);
    }

}
