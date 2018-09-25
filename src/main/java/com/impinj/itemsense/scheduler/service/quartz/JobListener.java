package com.impinj.itemsense.scheduler.service.quartz;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;

public class JobListener extends JobListenerSupport {

	QuartzService parent;

	public JobListener(QuartzService quartzService) {
		this.parent = quartzService;
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		JobResult jobResult = null;
		if (jobException != null) {
			jobResult = new JobResult(context, jobException);
		} else {
			jobResult = (JobResult) context.getResult();
		}
		parent.saveJobResult(jobResult);
	}
}