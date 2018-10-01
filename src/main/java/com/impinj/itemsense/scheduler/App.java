package com.impinj.itemsense.scheduler;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class App {
	private static final String _APPTITLE = "ItemSense Job Scheduler";
	private static final String appId = UUID.randomUUID().toString();

	private static final String ARG_SERVICE = "service";
	private static final String ARG_LOGGINGLEVEL = "logginglevel";
	private static final String ARG_HELP = "help";

	public static String getApplicationTitle() {
		return _APPTITLE;
	}

	public static String getApplicationId() {
		return appId;
	}

	public static void main(String[] args) {

		OptionParser parser = new OptionParser() {
			{
				accepts(ARG_SERVICE, "Start application as a service");
				accepts(ARG_LOGGINGLEVEL, "Logging Level:  OFF, ERROR, WARN, INFO, DEBUG, TRACE and ALL")
						.withRequiredArg().ofType(Level.class);
				accepts(ARG_HELP, "Print this help message").forHelp();
			}
		};

		OptionSet optionsSet = parser.parse(args);
		if (optionsSet.has(ARG_HELP)) {
			try {
				parser.printHelpOn(System.out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {

			if (optionsSet.has(ARG_LOGGINGLEVEL)) {
				Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
				root.setLevel((Level) optionsSet.valueOf(ARG_LOGGINGLEVEL));
			}

			if (optionsSet.has(ARG_SERVICE)) {
				AppService.launch();
			} else {
				AppClient.launch(AppClient.class, args);
			}

		}
	}
}
