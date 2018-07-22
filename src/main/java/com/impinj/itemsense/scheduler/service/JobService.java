package com.impinj.itemsense.scheduler.service;

import java.util.TimeZone;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.quartz.CronExpression;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.ListenerManager;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.listeners.JobListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.impinj.itemsense.scheduler.App;
import com.impinj.itemsense.scheduler.constants.ConnectorConstants;
import com.impinj.itemsense.scheduler.job.JobResult;
import com.impinj.itemsense.scheduler.model.ItemSenseConfig;
import com.impinj.itemsense.scheduler.model.ItemSenseConfigJob;

public class JobService {

	private static final Logger logger = LoggerFactory.getLogger(JobService.class);
	
    /**
     * The place non-Spring (the Quartz jobs when they wake up) get the
     * configuration information.
     */
    private Scheduler scheduler;

    
    public JobService() {
        logger.info("Creating Quartz Job Scheduler");
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            ListenerManager listenerManager = scheduler.getListenerManager();
            //listenerManager.addJobListener(new JobServiceJobListener());
            scheduler.start();
        } catch (SchedulerException e) {
            throw new RuntimeException("Unable to configure Quartz Scheduler Factory.", e);
        }
    }
    
    /**
     * Quartz Jobs and the UIs both access this table to find results of jobs,
     * so we use and old sync'ed vector which is a vector!
     */
    private List<JobResult> jobResults = Collections.synchronizedList(new LinkedList<JobResult>());

	
    private JobKey jobKeyFromConf(ItemSenseConfigJob config) {
        return JobKey.jobKey(config.getOid(), App.getApplicationId());
    }

    private TriggerKey triggerKeyFromConf(ItemSenseConfigJob config) {
        return TriggerKey.triggerKey(config.getOid(), App.getApplicationId());
    }

	/**
	 * Builds the quartz Job and Trigger then submits the job to the quartz
	 * scheduler.
	 * <p>
	 * NOTE: Jobs should be built in a way that is independent of the connector app.
	 * In other words, all information the job needs to complete successfully should
	 * be provided in the JobDataMap via serializable objects.
	 *
	 * @param itemSenseConfigJob
	 * @param jobClass
	 * @throws Exception 
	 */
	public void scheduleJob(ItemSenseConfigJob itemSenseConfigJob, Class<? extends Job> jobClass) throws Exception {

		boolean hasNextValidTimeAfterNow = false;

		try {
			CronExpression cronExpression = new CronExpression(itemSenseConfigJob.getSchedule());
			cronExpression.setTimeZone(TimeZone.getTimeZone("GMT"));
			hasNextValidTimeAfterNow = cronExpression.getNextValidTimeAfter(new Date()) != null;
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

		// Schedule only jobs with perspective
		if (!hasNextValidTimeAfterNow) {
			logger.info("Skip scheduling Quartz Job " + jobClass.getSimpleName() + " " + itemSenseConfigJob.getJobKey()
					+ "(Group " + App.getApplicationId() + ") - has no next valid time after now");
			return;
		}

		logger.info("Scheduling Quartz Job " + jobClass.getSimpleName() + " " + itemSenseConfigJob.getJobKey() + "(Key " + itemSenseConfigJob.getOid() + ", Group "
				+ App.getApplicationId() + ")");

		// build the job data map and hand it to job builder, otherwise
		// jobdata contents are restricted to Standard serializable data types
		// (String, Long, etc)
		// TODO: TEST TO SEE IF CHANGES TO CONFIG SHARED IN PARTICULAR, impact the job.
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(ConnectorConstants.JOB_DATA_MAP_ITEMSENSE_CONFIG, App.getConfigByOID(itemSenseConfigJob.getItemSenseOid()));
		jobDataMap.put(ConnectorConstants.JOB_DATA_MAP_JOB_CONFIG, itemSenseConfigJob);

		JobDetail job = JobBuilder
				.newJob(jobClass)
				.withIdentity(jobKeyFromConf(itemSenseConfigJob))
				.usingJobData(jobDataMap)
				.build();

		Trigger trigger = newTrigger()
				.withIdentity(triggerKeyFromConf(itemSenseConfigJob))
				.withSchedule(cronSchedule(itemSenseConfigJob.getSchedule()).inTimeZone(TimeZone.getTimeZone("UTC")))
				.build();
		try {
			Date jobDate = scheduler.scheduleJob(job, trigger);
			logger.info("Scheduled job " + jobDate.toLocaleString());
		} catch (SchedulerException e) {
			throw new RuntimeException("Unable to Schedule Quartz Job " + jobClass.getSimpleName() + " " + itemSenseConfigJob.getJobKey(), e);
		}
	}

	/**
	 * This method will queue all active jobs if the store is active. If the store
	 * is not active, no jobs will be queued.
	 *
	 * @param store
	 */
	public void queueStoreJobs(ItemSenseConfig configData) {
		if (configData.isActive()) {
			configData.getActiveJobs().stream().forEach(jobConfig -> {
				try {
					scheduleJob(jobConfig, Job.class);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
	}

	public void queueAllQuartzJobs() {
		logger.info("Reloading all Quartz Jobs");
		App.getConfig().forEach(config -> queueStoreJobs(config));
	}

}
