package com.impinj.itemsense.scheduler.util;

import org.quartz.SchedulerException;
import org.slf4j.LoggerFactory;

import com.impinj.itemsense.scheduler.App;
import com.impinj.itemsense.scheduler.AppConstants;
import com.impinj.itemsense.scheduler.service.quartz.QuartzService;

import ch.qos.logback.classic.Logger;

public class ShutdownHook extends Thread {
	private static final Logger logger = (Logger) LoggerFactory.getLogger(App.class);

	public void run() {
		logger.info("Shutting down {}.", AppConstants.APP_TITLE);
		try {
			QuartzService service = QuartzService.getService(false);
			if (service != null) {
				service.shutdown();
			}
		} catch (SchedulerException e) {
			logger.error("Caught SchedulerException.", e);
		}
	}
}