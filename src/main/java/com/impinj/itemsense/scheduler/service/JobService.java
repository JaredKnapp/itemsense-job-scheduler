package com.impinj.itemsense.scheduler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.impinj.itemsense.scheduler.App;
import com.impinj.itemsense.scheduler.model.ItemSense;
import com.impinj.itemsense.scheduler.model.ItemSenseJob;

public class JobService {

	private static final Logger logger = LoggerFactory.getLogger(JobService.class);

	
    /**
     * This method will queue all active jobs if the store is active. If the
     * store is not active, no jobs will be queued.
     *
     * @param store
     */
    public void queueStoreJobs(ItemSense config) {
        if (config.isActive()) {
        	//config.getActiveJobs().stream().forEach(jobConfig -> scheduleJob(jobConfig, Job.class));
        }
    }

    public void queueAllQuartzJobs() {
        logger.info("Reloading all Quartz Jobs");
        App.getConfig().forEach(config -> queueStoreJobs(config));
    }

}
