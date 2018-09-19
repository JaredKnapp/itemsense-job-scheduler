package com.impinj.itemsense.scheduler.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.quartz.JobDetail;
import org.quartz.impl.triggers.CronTriggerImpl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.impinj.itemsense.scheduler.constants.ConnectorConstants;

import lombok.Data;

/**
 * A bean to contain a Quartz Trigger and Quartz Job, so I can display them
 * together. Yes, Jobs can have more than one trigger, but this bean is to load
 * a table of triggers.
 */

@Data
public class TriggeredJob {

	private String cron;
	@JsonIgnore
	private JobDetail jobDetail;
	private String storeOid;
	private String itemsense;
	private String facility;
	private String jobName;
	private String jobType;
	private String jobKey;
	private Date prevFire;
	private Date nextFire;

	public TriggeredJob(CronTriggerImpl trigger, JobDetail jobDetail) {
		if (trigger == null || jobDetail == null) {
			throw new RuntimeException("trigger is " + trigger + " JobDetail is " + jobDetail);
		}
		this.cron = trigger.getCronExpression();
		this.jobDetail = jobDetail;
		ItemSenseConfigJob jobConfig = (ItemSenseConfigJob) jobDetail.getJobDataMap()
				.get(ConnectorConstants.JOB_DATA_MAP_JOB_CONFIG);
		ItemSenseConfig ItemSenseConfigJob = (ItemSenseConfig) jobDetail.getJobDataMap()
				.get(ConnectorConstants.JOB_DATA_MAP_ITEMSENSE_CONFIG);
		// ConnectorConfig sharedConfig =
		// (ConnectorConfig)jobDetail.getJobDataMap().get(ConnectorConstants.JOB_DATA_MAP_SHARED_CONFIG);

		this.storeOid = ItemSenseConfigJob.getOid();
		this.itemsense = ItemSenseConfigJob.getName();
		this.facility = jobConfig.getFacility();
		this.jobName = jobConfig.getName();
		this.jobType = jobConfig.getJobType();
		this.jobKey = trigger.getKey().getName();
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		this.prevFire = trigger.getPreviousFireTime();
		this.nextFire = trigger.getNextFireTime();
	}
}
