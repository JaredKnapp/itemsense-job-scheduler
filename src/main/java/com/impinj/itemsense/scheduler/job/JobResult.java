package com.impinj.itemsense.scheduler.job;

import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

import lombok.Data;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.impinj.itemsense.client.coordinator.job.JobResponse;
import com.impinj.itemsense.scheduler.constants.ConnectorConstants;
import com.impinj.itemsense.scheduler.model.ItemSenseConfig;
import com.impinj.itemsense.scheduler.model.ItemSenseConfigJob;

/**
 * All the creation of Job Results happens here, so we can coordinate the use of all the JobResult fields.  Sure that makes this class
 * depend on all the sources, but that's dependency injection instead of scattered creation in other classes.  If you add
 * and change this class, all changes are here.
 * @author paul
 *
 */
@Data
public class JobResult {

    public enum Status {
        SUCCEEDED, FAILED
    }

    private String jobKey;
    private Status status; 
    private String runStatus = "(not wired yet)";
    private String results ;
    private String storeOid;
    private String storeName;
    private String jobName;
    private String jobType;
    private String jobClassSimpleName;
    // TODO use the new Java.time objects!
    private ZonedDateTime creationDateTime = ZonedDateTime.now(ZoneOffset.UTC);

    public JobResult() {

    }

    public JobResult(JobExecutionContext context, JobExecutionException jobException) {
        setFromJobDetail(context.getJobDetail());
        setResults(jobException.getCause().getLocalizedMessage());
        setStatus(Status.FAILED);
        setRunStatus("Quartz Job Exception");
    }

    public JobResult(JobResponse jobResponse, JobDetail jobDetail) {

        setFromJobDetail(jobDetail);
        if (jobResponse.getCreationTime() != null) {
            setCreationDateTime( jobResponse.getCreationTime().withZoneSameInstant(ZoneOffset.UTC));
        } else {
            // TODO some jobs have NO times in them, so what to do?
        }
        setStatus(Status.SUCCEEDED);
        setRunStatus(jobResponse.getStatus());
        setResults("Success");
    }
    
    public void setFromJobDetail(JobDetail jobDetail) {
    	
        setJobKey(jobDetail.getKey().getName());
        ItemSenseConfig itemsenseConfig = (ItemSenseConfig) jobDetail.getJobDataMap().get(ConnectorConstants.JOB_DATA_MAP_ITEMSENSE_CONFIG);
        setStoreName(itemsenseConfig.getName());
        ItemSenseConfigJob jobConfig = (ItemSenseConfigJob) jobDetail.getJobDataMap().get(ConnectorConstants.JOB_DATA_MAP_JOB_CONFIG);
        setJobName(jobConfig.getName());
        setJobType(jobConfig.getJobType());
        setJobClassSimpleName(jobDetail.getJobClass().getSimpleName());
    }
}
