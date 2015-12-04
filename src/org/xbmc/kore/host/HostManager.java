/*
 * Copyright 2015 Synced Synapse. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xbmc.kore.host;

import java.util.ArrayList;
import java.util.List;

import org.xbmc.kore.jsonrpc.HostConnection;

import application.service.ParameterNames;
import application.service.ParameterService;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Manages XBMC Hosts
 * Singleton that loads the list of registered hosts, keeps a
 * {@link HostConnection} to the active host
 * and allows for creation and removal of hosts
 */
public class HostManager {

	// Singleton instance
	private static volatile HostManager instance = null;

	/**
	 * Arraylist that will hold all the hosts in the database
	 */
	private List<HostInfo> hosts = new ArrayList<HostInfo>();

	/**
	 * Current host
	 */
	private SimpleObjectProperty<HostInfo> currentHostInfo = new SimpleObjectProperty<>();

    /**
     * Singleton constructor
     */
	protected HostManager() {
		String hostName = "";
		ParameterService paramService = ParameterService.getInstance();

		for (int index = 0; hostName != null; ++index) {
			hostName = paramService.getString(ParameterNames.getIndexedName(ParameterNames.HOST_NAME, index));
			if (hostName != null) {
				hosts.add(new HostInfo(
						hostName,
						paramService.getString(ParameterNames.getIndexedName(ParameterNames.HOST_IP, index)),
						paramService.getInt(ParameterNames.getIndexedName(ParameterNames.HOST_PROTOCOL, index), HostConnection.PROTOCOL_HTTP),
						paramService.getInt(ParameterNames.getIndexedName(ParameterNames.HOST_HTTP_PORT, index), HostInfo.DEFAULT_HTTP_PORT),
						paramService.getInt(ParameterNames.getIndexedName(ParameterNames.HOST_TCP_PORT, index), HostInfo.DEFAULT_TCP_PORT),
						paramService.getString(ParameterNames.getIndexedName(ParameterNames.HOST_USERNAME, index), ""),
						paramService.getString(ParameterNames.getIndexedName(ParameterNames.HOST_PASSWORD, index), ""),
						paramService.getBoolean(ParameterNames.getIndexedName(ParameterNames.HOST_USEEVENT, index), false),
						paramService.getInt(ParameterNames.getIndexedName(ParameterNames.HOST_EVENT_PORT, index), HostInfo.DEFAULT_EVENT_SERVER_PORT)
				));
			}
		}

		if (!hosts.isEmpty()) {
			currentHostInfo.set(hosts.get(paramService.getInt(ParameterNames.SELECTED_HOST, 0)));
		}
	}

	/**
	 * Singleton access method
	 * @param context Android app context
	 * @return HostManager singleton
	 */
	public static HostManager getInstance() {
		if (instance == null) {
			instance = new HostManager();
		}
		return instance;
	}

	/**
	 *
	 * @return
	 */
	public List<HostInfo> getListHostInfo() {
		return hosts;
	}

	/**
	 * return the current selected host
	 * @return
	 */
	public HostInfo getCurrentHostInfo(){
		return currentHostInfo.get();
	}

	/**
	 * get the observate host property
	 * @return
	 */
	public Property<HostInfo> currentHostInfoProperty() {
		return currentHostInfo;
	}

	/**
	 * set the urrent host info
	 */
	public void setCurrentHostInfo(HostInfo  hostinfo) {
		this.currentHostInfo.set(hostinfo);
		for (int i = 0; i < this.hosts.size(); ++i) {
			if (hosts.get(i).getAddress().equals(hostinfo.getAddress())) {
				ParameterService.getInstance().setParameter(ParameterNames.SELECTED_HOST, i);
				ParameterService.getInstance().save();
				break;
			}
		}
	}

	/**
	 * add an host into host manager
	 * @param info
	 */
	public void addHost(HostInfo info) {
		editHost(info, info);
	}

	public HostConnection getConnection() {
		return new HostConnection(this.getCurrentHostInfo());
	}

	public void editHost(HostInfo oldHostInfo, HostInfo newHostInfo) {
		int index = hosts.size();
		// host already exists
		for (int i = 0; i < this.hosts.size(); ++i) {
			if (hosts.get(i).getAddress().equals(oldHostInfo.getAddress())) {
				index = i;
				break;
			}
		}

		if (index != hosts.size()) {
			hosts.remove(index);
		}

		hosts.add(index, newHostInfo);

		// add all parameters on config file
		ParameterService paramService = ParameterService.getInstance();
		paramService.setParameter(ParameterNames.getIndexedName(ParameterNames.HOST_NAME, index), newHostInfo.getName());
		paramService.setParameter(ParameterNames.getIndexedName(ParameterNames.HOST_IP, index), newHostInfo.getAddress());
		paramService.setParameter(ParameterNames.getIndexedName(ParameterNames.HOST_PROTOCOL, index), newHostInfo.getProtocol());
		paramService.setParameter(ParameterNames.getIndexedName(ParameterNames.HOST_HTTP_PORT, index), newHostInfo.getHttpPort());
		paramService.setParameter(ParameterNames.getIndexedName(ParameterNames.HOST_TCP_PORT, index), newHostInfo.getTcpPort());
		paramService.setParameter(ParameterNames.getIndexedName(ParameterNames.HOST_USERNAME, index), newHostInfo.getUsername());
		paramService.setParameter(ParameterNames.getIndexedName(ParameterNames.HOST_PASSWORD, index), newHostInfo.getPassword());
		paramService.setParameter(ParameterNames.getIndexedName(ParameterNames.HOST_USEEVENT, index), newHostInfo.getUseEventServer());
		paramService.setParameter(ParameterNames.getIndexedName(ParameterNames.HOST_EVENT_PORT, index), newHostInfo.getEventServerPort());

		paramService.save();

		if (this.currentHostInfo.get() == null) {
			this.setCurrentHostInfo(newHostInfo);
		}
	}

	/**
	 * remove host info for this index
	 * @param index
	 */
	public void removeHost(int index) {
		hosts.remove(index);

		ParameterService paramService = ParameterService.getInstance();
		paramService.removeParameter(ParameterNames.getIndexedName(ParameterNames.HOST_NAME, index));
		paramService.removeParameter(ParameterNames.getIndexedName(ParameterNames.HOST_IP, index));
		paramService.removeParameter(ParameterNames.getIndexedName(ParameterNames.HOST_PROTOCOL, index));
		paramService.removeParameter(ParameterNames.getIndexedName(ParameterNames.HOST_HTTP_PORT, index));
		paramService.removeParameter(ParameterNames.getIndexedName(ParameterNames.HOST_TCP_PORT, index));
		paramService.removeParameter(ParameterNames.getIndexedName(ParameterNames.HOST_USERNAME, index));
		paramService.removeParameter(ParameterNames.getIndexedName(ParameterNames.HOST_PASSWORD, index));
		paramService.removeParameter(ParameterNames.getIndexedName(ParameterNames.HOST_USEEVENT, index));
		paramService.removeParameter(ParameterNames.getIndexedName(ParameterNames.HOST_EVENT_PORT, index));

		paramService.save();
	}

}
