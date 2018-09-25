package com.impinj.itemsense.scheduler.service.quartz;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.quartz.CronExpression;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ListenerManager;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.impinj.itemsense.scheduler.App;
import com.impinj.itemsense.scheduler.model.ItemSenseConfig;
import com.impinj.itemsense.scheduler.model.ItemSenseConfigJob;
import com.impinj.itemsense.scheduler.model.TriggeredJob;
import com.impinj.itemsense.scheduler.service.DataService;
import com.impinj.itemsense.scheduler.util.ConnectorConstants;

import java.util.ArrayList;

public class QuartzService {

	private static final Logger logger = LoggerFactory.getLogger(QuartzService.class);
	private static final int JOB_RESULTS_STACK_SIZE = 1000;
	private static QuartzService service;

	public static QuartzService getService(boolean createIfNull) {
		if (service == null && createIfNull)
			service = new QuartzService();
		return service;
	}

	/**
	 * The place non-Spring (the Quartz jobs when they wake up) get the
	 * configuration information.
	 */
	private Scheduler scheduler;

	/**
	 * Quartz Jobs and the UIs both access this table to find results of jobs, so we
	 * use an old sync'ed vector which is a vector!
	 */
	// private static List<JobResult> jobResults = Collections.synchronizedList(new
	// LinkedList<JobResult>());
	private List<JobResult> jobResults = new ArrayList<>();

	private QuartzService() {
		logger.info("Creating Quartz Job Scheduler");
		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			ListenerManager listenerManager = scheduler.getListenerManager();
			listenerManager.addJobListener(new JobListener(this));
			scheduler.start();
		} catch (SchedulerException e) {
			throw new RuntimeException("Unable to configure Quartz Scheduler Factory.", e);
		}
	}

	private TriggerKey triggerKeyFromConf(ItemSenseConfigJob config) {
		return TriggerKey.triggerKey(config.getOid(), App.getApplicationId());
	}

	private JobKey jobKeyFromConf(ItemSenseConfigJob config) {
		return JobKey.jobKey(config.getOid(), App.getApplicationId());
	}

	private JobKey jobKeyFromTriggerKey(TriggerKey triggerKey) {
		return JobKey.jobKey(triggerKey.getName(), triggerKey.getGroup());
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
	private void scheduleJob(ItemSenseConfigJob itemSenseConfigJob, Class<? extends Job> jobClass) throws Exception {

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

		logger.info("Scheduling Quartz Job " + jobClass.getSimpleName() + " " + itemSenseConfigJob.getJobKey() + "(Key "
				+ itemSenseConfigJob.getOid() + ", Group " + App.getApplicationId() + ")");

		// build the job data map and hand it to job builder, otherwise
		// jobdata contents are restricted to Standard serializable data types
		// (String, Long, etc)
		// TODO: TEST TO SEE IF CHANGES TO CONFIG SHARED IN PARTICULAR, impact the job.
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(ConnectorConstants.JOB_DATA_MAP_ITEMSENSE_CONFIG,
				DataService.getService(true).getItemSenseConfigByOID(itemSenseConfigJob.getItemSenseOid()));
		jobDataMap.put(ConnectorConstants.JOB_DATA_MAP_JOB_CONFIG, itemSenseConfigJob);

		JobDetail job = JobBuilder.newJob(jobClass).withIdentity(jobKeyFromConf(itemSenseConfigJob))
				.usingJobData(jobDataMap).build();

		Trigger trigger = newTrigger().withIdentity(triggerKeyFromConf(itemSenseConfigJob))
				.withSchedule(cronSchedule(itemSenseConfigJob.getSchedule()).inTimeZone(TimeZone.getTimeZone("UTC")))
				.build();
		try {
			Date jobDate = scheduler.scheduleJob(job, trigger);
			logger.info("Scheduled job " + jobDate);
		} catch (SchedulerException e) {
			throw new RuntimeException(
					"Unable to Schedule Quartz Job " + jobClass.getSimpleName() + " " + itemSenseConfigJob.getJobKey(),
					e);
		}
	}

	/**
	 * This method will queue all active jobs if ItemSense is active. If ItemSense
	 * is not active, no jobs will be queued.
	 *
	 * @param store
	 */
	public void queueJobs(ItemSenseConfig configData) {
		if (configData.isActive()) {
			configData.getActiveJobs().stream().forEach(jobConfig -> {
				try {
					scheduleJob(jobConfig, ItemSenseJob.class);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
	}

	public void queueAllJobs() throws IOException {
		logger.info("Reloading all Quartz Jobs");
		DataService.getService(true).getSystemConfig().getItemSenseConfigs().forEach(config -> queueJobs(config));
	}

	/**
	 * Delete from the Quartz Queue
	 */
	public void dequeueAllJobs() {
		try {
			scheduler.getJobGroupNames().stream().forEach(groupName -> {
				try {
					scheduler.getJobKeys(GroupMatcher.groupEquals(groupName)).forEach(jobKey -> {
						try {
							scheduler.deleteJob(jobKey);
							logger.info("Removed Job " + jobKey);
						} catch (SchedulerException e) {
							logger.error("Unable to delete Job " + jobKey);
						}
					});
				} catch (Exception e) {
					logger.error("Unable to obtains jobs to delete ");
				}
			});
		} catch (Exception e) {
			logger.error("Unable to obtain job group name(s) to delete ");
		}
	}

	public List<JobResult> getJobResults() {
		return jobResults;
	}

	public List<TriggeredJob> getQuartzJobs() {
		try {
			return scheduler.getTriggerKeys(GroupMatcher.groupEquals(App.getApplicationId())).stream()
					.map(triggerKey -> {
						try {
							return new TriggeredJob((CronTriggerImpl) scheduler.getTrigger(triggerKey),
									scheduler.getJobDetail(jobKeyFromTriggerKey(triggerKey)));
						} catch (Exception e) {
							throw new RuntimeException(
									"Unable to find Quartz Job and Trigger for Trigger Key " + triggerKey, e);
						}
					}).collect(Collectors.toList());
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Unable to find Quartz Jobs", e);
		}
	}
	
	// TODO: Replace with Event Processing
	public void saveJobResult(JobResult jobResult) {
		// if the stack is full, remove the last item to make room for the jobResult
		// just heard...
		if (jobResults.size() == JOB_RESULTS_STACK_SIZE) {
			((LinkedList<JobResult>) jobResults).removeLast();
		}
		// add the result at "0" - the beginning of the list
		jobResults.add(0, jobResult);
	}

	public void shutdown() throws SchedulerException {
		this.dequeueAllJobs();
		scheduler.shutdown(false);
	}

}
