package com.impinj.itemsense.scheduler;

import java.io.IOException;

import org.slf4j.LoggerFactory;

import com.impinj.itemsense.scheduler.service.DataService;
import com.impinj.itemsense.scheduler.service.quartz.QuartzService;
import com.impinj.itemsense.scheduler.util.ShutdownHook;

import ch.qos.logback.classic.Logger;

class AppService {

	private static final Logger logger = (Logger) LoggerFactory.getLogger(AppService.class);

	public static void launch() {
		logger.info("Starting {}.", AppConstants.APP_TITLE);
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		try {
			DataService.getService(true);
			QuartzService.getService(true).queueAllJobs();
		} catch (IOException e) {
			logger.error("Caught IOException", e);
		}
	}

}
