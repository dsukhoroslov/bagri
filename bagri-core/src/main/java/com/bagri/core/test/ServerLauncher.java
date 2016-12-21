package com.bagri.core.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerLauncher {
	
    private static final transient Logger logger = LoggerFactory.getLogger(ServerLauncher.class);
	
	private File home;
	private String command;
	private String[] props;
	private Process server;
	private Thread eth;
	private Thread ith;
	
	public ServerLauncher(String profile, String[] props, String dir) {
		String java_opts = "-server -Xms1g -Xmx1g " +
				"-Dnode.name=first -Dbdb.config.path=src/main/resources " +
				"-Dbdb.config.context.file=spring/cache-system-context.xml " +
				"-Dbdb.config.properties.file=" + profile + ".properties " +
				"-Dbdb.config.filename=config.xml -Dbdb.log.level=info " +
	            "-Dlogback.configurationFile=hz-logging.xml " +
				"-cp " + dir + "\\target\\*;" + dir + "\\target\\lib\\*";

		command = "java " + java_opts + " com.bagri.server.hazelcast.BagriCacheServer";
		home = new File(dir);
		this.props = props;
	}
	
	public void startServer() {
		//Locale.setDefault(new Locale("en", "US"));
		//ProcessBuilder pb;
		try {
			server = Runtime.getRuntime().exec(command, props, home);
			logger.info("startServer; Bagri server has been started");
			ith = new Thread(new StreamConsumer(server.getInputStream(), true));
			eth = new Thread(new StreamConsumer(server.getErrorStream(), true));
			ith.start();
			eth.start();
			// wait till server will be ready to accept client connections..
			Thread.sleep(20000);
		} catch (Throwable ex) {
			logger.error("startServer.error", ex);
		}
	}

	public void stopServer() {
		if (server != null) {
			eth.interrupt();
			ith.interrupt();
			server.destroy();
			logger.info("stopServer; Bagri server stopped with exitValue: {}", server.exitValue());
		} else {
			logger.error("stopServer.error; server is not started!");
		}
	}
	
	private class StreamConsumer implements Runnable {
		
		private BufferedReader br;
		private boolean print;
		
		StreamConsumer(InputStream in, boolean print) {
			br = new BufferedReader(new InputStreamReader(in));
			this.print = print;
		}

		@Override
		public void run() {
			String str = null;
			try {
				while ((str = br.readLine()) != null) {
					if (print) {
						logger.info(str);
					}
				}
			} catch (IOException e) {
				// interrupted
			}
			try {
				br.close();
			} catch (IOException e) {
				//
			}
		}
		
	}
}
