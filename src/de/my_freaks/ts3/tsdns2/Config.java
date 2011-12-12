package de.my_freaks.ts3.tsdns2;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Config {
	private Properties props;
	private long shutdownWaitTime;
	private int serverPort;
	private short threads;
	private int socketTimeout;
	private String srvRecordPrefix;
	private String defaultHost;
	private List<String> allowedDomains;
	private Level logLevel;

	public Config() {
		props = new Properties();
		try {
			BufferedInputStream propsFile = new BufferedInputStream(
					new FileInputStream("ts3dns2.properties"));
			props.load(propsFile);
			propsFile.close();
		} catch (IOException e) {
			throw new RuntimeException("Can't load Properties.", e);
		}
		shutdownWaitTime = TimeUnit.SECONDS.toMillis(Long
				.parseLong((String) props.getProperty("ShutdownWaitTime")));
		serverPort = Integer.parseInt(props.getProperty("ServerPort"));
		threads = Short.parseShort(props.getProperty("Threads"));
		socketTimeout = (int) TimeUnit.SECONDS.toMillis(Integer.parseInt(props
				.getProperty("SocketTimeout")));
		srvRecordPrefix = props.getProperty("SrvRecordPrefix");
		defaultHost = props.getProperty("DefaultHost");
		allowedDomains = Arrays.asList(props.getProperty("AllowedDomains")
				.split(","));
		logLevel = Level.parse(props.getProperty("LogLevel"));
	}

	public long getShutdownWaitTime() {
		return shutdownWaitTime;
	}

	public int getServerPort() {
		return serverPort;
	}

	public short getThreads() {
		return threads;
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}

	public String getSrvRecordPrefix() {
		return srvRecordPrefix;
	}

	public String getDefaultHost() {
		return defaultHost;
	}

	public boolean isDomainAllowed(String domain) {
		if (allowedDomains.isEmpty()) {
			return true;
		}

		for (String curDomain : allowedDomains) {
			if (domain.endsWith(curDomain)) {
				return true;
			}
		}
		return false;
	}

	public Level getLogLevel() {
		return logLevel;
	}
}
