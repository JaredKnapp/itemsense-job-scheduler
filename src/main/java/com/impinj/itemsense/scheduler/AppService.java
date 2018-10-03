package com.impinj.itemsense.scheduler;

import java.io.IOException;

import org.quartz.SchedulerException;
import org.slf4j.LoggerFactory;

import com.impinj.itemsense.scheduler.service.DataService;
import com.impinj.itemsense.scheduler.service.quartz.QuartzService;

import ch.qos.logback.classic.Logger;

class AppService {

	private static final Logger logger = (Logger) LoggerFactory.getLogger(AppService.class);

	private static class ShutdownHook extends Thread {
		public void run() {
			logger.info("Shutting down {}.", App.getApplicationTitle());
			try {
				QuartzService service = QuartzService.getService(false);
				if (service != null) {
					service.shutdown();
				}
			} catch (SchedulerException e) {
				logger.error("Caught SchedulerException: {}.", e);
			}
		}
	}

	public static void launch() {
		logger.info("Starting {}.", App.getApplicationTitle());
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		try {
			DataService.getService(true);
			QuartzService.getService(true).queueAllJobs();
		} catch (IOException e) {
			logger.error("Caught IOException: {}.", e);
		}
	}

}
