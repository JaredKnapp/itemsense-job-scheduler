package com.impinj.itemsense.scheduler.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.LoggerFactory;

import com.impinj.itemsense.client.coordinator.CoordinatorApiController;
import com.impinj.itemsense.client.coordinator.facility.Facility;
import com.impinj.itemsense.client.coordinator.job.Job;
import com.impinj.itemsense.client.coordinator.job.JobController;
import com.impinj.itemsense.client.coordinator.job.JobResponse;
import com.impinj.itemsense.client.coordinator.recipe.Recipe;
import com.impinj.itemsense.scheduler.AppConstants;
import com.impinj.itemsense.scheduler.model.ItemSenseConfig;
import com.impinj.itemsense.scheduler.model.ItemSenseConfigJob;
import com.impinj.itemsense.scheduler.service.quartz.JobResult;
import com.impinj.itemsense.scheduler.service.quartz.JobResult.Status;

import ch.qos.logback.classic.Logger;

/*
    ItemSenseService - connects to ItemSense to query parameters using the REST API.
*/

public class ItemSenseService {
	private static final Logger logger = (Logger) LoggerFactory.getLogger(ItemSenseService.class);

	private Client client;
	private CoordinatorApiController itemsenseCoordinatorController;
	private Job job;

	private ItemSenseConfig config;
	private ItemSenseConfigJob configJob;
	private JobResult jobResult;

	/**
	 * Constructor - when built with ItemSenseJobConfig, will start/stop ItemSense
	 * Jobs
	 * 
	 */
	public ItemSenseService(ItemSenseConfig config, ItemSenseConfigJob configJob, JobResult jobResult) {
		this.config = config;
		this.configJob = configJob;
		this.jobResult = jobResult;

		this.job = new Job();
		job.setReportToDatabaseEnabled(true);
		job.setReportToMessageQueueEnabled(true);

		if (configJob != null) {
			// TODO: validate recipe is configured in IS
			job.setRecipeName(configJob.getRecipe());
			job.setDurationSeconds(configJob.getDuration());
			job.setFacility(configJob.getFacility());
			job.setStartDelay("PT" + configJob.getStartDelay() + "S"); // Add PT<seconds>S formating
		}
	}

	public Client getClient() {
		if (client == null) {
			client = ClientBuilder.newClient()
					.register(JacksonFeature.class)
					.register(HttpAuthenticationFeature.basic(config.getUsername(), config.getPassword()));
		}

		return client;
	}

	public CoordinatorApiController getItemsenseCoordinatorController() {

		if (itemsenseCoordinatorController == null) {
			URI fullUri = URI.create(config.getUrl() + "/itemsense").normalize();
			itemsenseCoordinatorController = new CoordinatorApiController(getClient(), fullUri);
		}

		return itemsenseCoordinatorController;
	}

	public ArrayList<JobResponse> getJobsInFacility() {
		JobController jobController = getItemsenseCoordinatorController().getJobController();
		List<JobResponse> jobResponses = jobController.getJobs();
		ArrayList<JobResponse> retResponses = new ArrayList<>();

		for (JobResponse response : jobResponses) {
			Facility facilities[] = response.getFacilities();
			for (Facility facility : facilities) {
				if (facility.getName().contains(configJob.getFacility()))
					retResponses.add(response);
			}
		}
		return retResponses;
	}

	public ArrayList<JobResponse> getRunningJobsInFacility() {
		ArrayList<JobResponse> jobResponses = getJobsInFacility();
		ArrayList<JobResponse> retResponses = new ArrayList<>();
		for (JobResponse response : jobResponses) {
			if (isJobRunning(response.getStatus()))
				retResponses.add(response);
		}
		return retResponses;
	}

	public boolean isJobFailed(String status) {
		return (status.contains(AppConstants.ITEMSENSE_JOB_STATUS_FAIL));
	}

	public boolean isJobRunning(String status) {
		return (status.contains(AppConstants.ITEMSENSE_JOB_STATUS_RUNNING) || status.equals("REGISTERED")
				|| status.equals("WAITING") || status.equals("INITIALIZING") || status.equals("STARTING")
				|| status.equals("PUBLISHING_STATE") || status.equals("fPED_STOPPING")
				|| status.equals("COMPLETE_STOPPING"));
	}

	public boolean isJobStopped(String status) {
		return (status.equals("STOPPED") || status.equals("COMPLETE") || status.equals("COMPLETE_WITH_ERRORS")
				|| status.equals("FAILED"));
	}

	public JobResult runJob() {
		if (configJob.isStopRunningJobs()) {
			stopRunningJobsInFacility();
		}

		logger.info(String.format("Starting Job: %s facility: %s recipe: %s", config.getName(), configJob.getFacility(),
				job.getRecipeName()));

		// TODO: validate recipe exist before trying to start the job
		JobController jobController = getItemsenseCoordinatorController().getJobController();
		JobResponse jobResponse = jobController.startJob(job);
		if (isJobFailed(jobResponse.getStatus())) {
			String msg = String.format("FAILED to start Job in ItemSense: %s facility: %s  recipe: %s  jobId: %s",
					config.getName(), configJob.getFacility(), job.getRecipeName(), jobResponse.getId());
			this.jobResult.setStatus(Status.FAILED);
			this.jobResult.setResults(msg);

			logger.error(String.format("%s job: %s jobResponse: %s", msg, job, jobResponse));
			return this.jobResult;
		}

		// TODO: should there be retry logic?

		// the job has started, but is not yet "RUNNING"
		// hang out while it does initialization
		while (isJobRunning(jobResponse.getStatus())
				&& !jobResponse.getStatus().contains(AppConstants.ITEMSENSE_JOB_STATUS_RUNNING)) {
			jobResponse = jobController.getJob(jobResponse.getId());
			// TODO: Fix to work correctly against new Status from and stop reason code
			// ItemSense
			if (isJobFailed(jobResponse.getStatus())) {
				String msg = String.format(
						"FAILED to initialize Job in ItemSense: %s facility: %s recipe: %s jobId: %s", config.getName(),
						configJob.getFacility(), job.getRecipeName(), jobResponse.getId());
				this.jobResult.setStatus(Status.FAILED);
				this.jobResult.setResults(msg);

				logger.error(String.format("%s job: %s jobResponse: %s", msg, job, jobResponse));
				return this.jobResult;
			}
		}
		// TODO: should this be debug?
		String msg = String.format("Job is RUNNING: %s facility: %s recipe: %s jobId: %s", config.getName(),
				configJob.getFacility(), job.getRecipeName(), jobResponse.getId());
		this.jobResult.setStatus(Status.SUCCEEDED);
		this.jobResult.setResults(msg);

		logger.info(String.format("%s job: %s jobResponse: %s", msg, job, jobResponse));
		return this.jobResult;
	}

	public void stopRunningJobsInFacility() {
		String msg = String.format("ItemSense: %s Facility: %s", config.getName(), configJob.getFacility());
		JobController jobController = getItemsenseCoordinatorController().getJobController();
		ArrayList<JobResponse> runningJobs = getRunningJobsInFacility();

		for (JobResponse runningJobResponse : runningJobs) {
			logger.info("Stopping job: {} facility: {} JobID: {}", new Object[] {config.getName(), configJob.getFacility(), runningJobResponse.getId()});
			jobController.stopJob(runningJobResponse.getId());
			// given ItemSense can only have one job running at a time per
			// facility, loop until has actually stopped

			// TODO: UPDATE FOR NEW JOB STATUS
			JobResponse stoppedJobResponse = jobController.getJob(runningJobResponse.getId());
			while (!isJobStopped(stoppedJobResponse.getStatus())) {
				logger.debug(msg + " stopping job " + runningJobResponse.getId() + ": status: " + stoppedJobResponse.getStatus());
				stoppedJobResponse = jobController.getJob(runningJobResponse.getId());
			}

			logger.info("{} stopped job {}: status: {}", new Object[] {msg, runningJobResponse.getId(), stoppedJobResponse.getStatus()});
		}
	}

	public boolean testConnection() {
		boolean success = true;
		try {
			CoordinatorApiController coordinator = getItemsenseCoordinatorController();
			List<Recipe> recipes = coordinator.getRecipeController().getRecipes();
			success = (recipes == null ? false : 0 < recipes.size());
		} catch (Exception e) {
			logger.info("Could not validate ItemSense Connection: {}", e.getMessage());
			success = false;
		}
		return success;
	}

	public List<String> getFacilityNames() {
		try {
			CoordinatorApiController coordinator = getItemsenseCoordinatorController();
			List<Facility> facilities = coordinator.getFacilityController().getAllFacilities();
			List<String> facilityNames = new ArrayList<>();
			for (Facility f : facilities) {
				f.getName();
				facilityNames.add(f.getName());
			}
			return facilityNames;
		} catch (Exception e) {
			System.out.println("Unable to retrieve Facility Names");
		}
		return null;
	}

	public List<String> getRecipeNames() {
		try {
			CoordinatorApiController coordinator = getItemsenseCoordinatorController();
			List<Recipe> recipies = coordinator.getRecipeController().getRecipes();
			List<String> recipeNames = new ArrayList<>();
			for (Recipe r : recipies) {
				r.getName();
				recipeNames.add(r.getName());
			}
			return recipeNames;
		} catch (Exception e) {
			System.out.println("Unable to retrieve Recipe Names");
		}
		return null;
	}
}
