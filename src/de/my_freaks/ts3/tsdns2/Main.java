package de.my_freaks.ts3.tsdns2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {
	private ExecutorService threadPool;
	private ThreadFactory threadFactory;
	private ServerSocket serverSock;
	private static Config config;
	private static Random rand = new Random(System.nanoTime());
	private static Logger LOGGER = Logger.getLogger(Main.class
			.getCanonicalName());
	public static final boolean DEBUG = false;

	public static void main(String[] args) {
		config = new Config();
		initLogger();
		new Main().run();
	}

	private void init() throws IOException {
		serverSock = new ServerSocket(getConfig().getServerPort());
		threadFactory = new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread ret = new Thread(r);
				ret.setDaemon(true);
				return ret;
			}
		};
		threadPool = Executors.newFixedThreadPool(getConfig().getThreads(),
				threadFactory);
	}

	private void run() {
		try {
			LOGGER.log(Level.INFO, "Startup.");
			init();
			LOGGER.log(Level.INFO, "Initialized.");
			while (!serverSock.isClosed()) {
				Socket sock;
				if (!DEBUG) {
					sock = serverSock.accept();
				} else {
					sock = new Socket() {
						public java.io.InputStream getInputStream()
								throws IOException {
							return System.in;
						}
						public java.io.OutputStream getOutputStream()
								throws IOException {
							return System.out;
						}
					};
				}
				threadPool.execute(new LookupThread(sock));
				if (DEBUG) {
					break;
				}
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error.", e);
			throw new RuntimeException("Error.", e);
		}

		LOGGER.log(Level.INFO, "Shutdown.");
		threadPool.shutdown();
		try {
			Thread.sleep(getConfig().getShutdownWaitTime());
		} catch (InterruptedException e) {
			// Ignore
		}
		LOGGER.log(Level.WARNING, "Exit now.");
		threadPool.shutdownNow();
	}

	public static Random getRandom() {
		return rand;
	}

	public static Config getConfig() {
		return config;
	}

	private static void initLogger() {
		Logger logger = Logger.getLogger("");
		logger.setLevel(getConfig().getLogLevel());
		FileHandler fileHandler;
		try {
			fileHandler = new FileHandler("log.txt");
		} catch (IOException e) {
			throw new RuntimeException("Can't initialize logger.", e);
		}
		fileHandler.setFormatter(new SimpleFormatter());
		logger.addHandler(fileHandler);
	}
}
