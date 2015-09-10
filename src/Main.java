import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;

import org.xbmc.kore.host.HostInfo;
import org.xbmc.kore.jsonrpc.ApiCallback;
import org.xbmc.kore.jsonrpc.Handler;
import org.xbmc.kore.jsonrpc.HostConnection;
import org.xbmc.kore.jsonrpc.method.JSONRPC.Ping;

import application.service.ParameterNames;

public class Main {

	public static void main(String[] args) throws SocketException {
		Scanner consoleIn = new Scanner(System.in);
		System.out.print("1)Print your IPv4 address\n2)Print your IPv6 adress\n3)Print reachable IPv4 hosts: ");
		int choice = consoleIn.nextInt();
		consoleIn.close();
		if (choice == 1 || choice == 2) {
			String protocolVersion = choice == 1 ? "IPv4" : "IPv6";
			InetAddress address = getWLANipAddress(protocolVersion);
			System.out.println(address != null ? address : protocolVersion
					+ " address not found. Is your internet down?");
		} else if (choice == 3) {
			InetAddress address = getWLANipAddress("IPv4");
			if (address != null) {
				printReachableHosts(address);
			} else {
				System.out.println("IPv4 Address not found. Is your internet down?");
			}
		} else {
			System.out.println("Unknown choice.");
		}
	}

	public static void printReachableHosts(InetAddress inetAddress) throws SocketException {
		String ipAddress = inetAddress.toString();
		final String templateipAddress = ipAddress.substring(1, ipAddress.lastIndexOf('.')) + ".";
		List<String> rechable = new LinkedList<>();
		Semaphore sem = new Semaphore(0);
		for (int i = 0; i < 256; i++) {
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
					System.out.println("OK " + otherAddress);
					rechable.add(otherAddress);
				}

				@Override
				public void onError(int errorCode, String description) {
					System.out.println("Errero " + otherAddress);
				}
			}, new Handler());
		}

	}

	public static InetAddress getWLANipAddress(String protocolVersion) throws SocketException {
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface netint : Collections.list(nets)) {
			if (netint.isUp() && !netint.isLoopback() && !netint.isVirtual()) {
				Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
				for (InetAddress inetAddress : Collections.list(inetAddresses)) {
					if (protocolVersion.equals("IPv4")) {
						if (inetAddress instanceof Inet4Address) {
							return inetAddress;
						}
					} else {
						if (inetAddress instanceof Inet6Address) {
							return inetAddress;
						}
					}
				}
			}
		}
		return null;
	}
}
