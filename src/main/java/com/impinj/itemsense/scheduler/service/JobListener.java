package com.impinj.itemsense.scheduler.service;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;

import com.impinj.itemsense.scheduler.job.JobResult;

public class JobListener extends JobListenerSupport {

	JobService parent;

    public JobListener(JobService jobService) {
    	this.parent = jobService;
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