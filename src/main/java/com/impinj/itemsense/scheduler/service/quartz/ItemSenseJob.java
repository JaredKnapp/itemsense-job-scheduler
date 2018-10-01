package com.impinj.itemsense.scheduler.service.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.impinj.itemsense.scheduler.model.ItemSenseConfig;
import com.impinj.itemsense.scheduler.model.ItemSenseConfigJob;
import com.impinj.itemsense.scheduler.service.ItemSenseService;
import com.impinj.itemsense.scheduler.service.quartz.JobResult.Status;
import com.impinj.itemsense.scheduler.util.ConnectorConstants;

/**
 * Run an ItemSenseJob There are two ways to communicate with the UI
 * JobController. Either 1. return a jobResult in jobExecutionContext, or 2.
 * throw a JobExecutionException to fail the job.
 */
public class ItemSenseJob implements Job {

	private static final Logger logger = LoggerFactory.getLogger(ItemSenseJob.class);

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

		JobResult jobResult = new JobResult();

		// this initializes jobResult at beginning of job
		jobResult.setFromJobDetail(jobExecutionContext.getJobDetail());

		// if this isn't available in the execution context
		final JobKey jobKey = jobExecutionContext.getJobDetail().getKey();
		ItemSenseConfigJob jobConfig = (ItemSenseConfigJob) jobExecutionContext.getJobDetail().getJobDataMap()
				.get(ConnectorConstants.JOB_DATA_MAP_JOB_CONFIG);
		ItemSenseConfig itemsenseConfig = (ItemSenseConfig) jobExecutionContext.getJobDetail().getJobDataMap()
				.get(ConnectorConstants.JOB_DATA_MAP_ITEMSENSE_CONFIG);
		logger.info("ItemSenseJob.execute store " + itemsenseConfig.getName() + " started job: " + jobKey.getName()
				+ "   jobKey: " + jobKey);

		ItemSenseService isHelper = new ItemSenseService(itemsenseConfig, jobConfig, jobResult);
		try {
			jobResult = isHelper.runJob();
		} catch (Exception e) {
			// Note We could, in the future set refireImmediately, unscheduleAllTriigers, &
			// unscheduleTrigger here based on the jobConfigs since we have a way to get the
			// jobConfigs
			String message = e.getMessage();
			logger.warn(message + itemsenseConfig.getName() + " + JobKey: " + jobKey.getName() + "exception message: "
					+ e.getMessage(), e);
			jobResult.setStatus(Status.FAILED);
			jobResult.setResults(message);
		}
		jobExecutionContext.setResult(jobResult);
		logger.info("ItemSense.execute store " + itemsenseConfig.getName() + " completed job: " + jobKey.getName()
				+ "   jobKey: " + jobKey);
	}
}
