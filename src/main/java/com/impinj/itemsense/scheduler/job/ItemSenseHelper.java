package com.impinj.itemsense.scheduler.job;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.impinj.itemsense.client.coordinator.CoordinatorApiController;
import com.impinj.itemsense.client.coordinator.facility.Facility;
import com.impinj.itemsense.client.coordinator.facility.FacilityController;
import com.impinj.itemsense.client.coordinator.job.Job;
import com.impinj.itemsense.client.coordinator.job.JobController;
import com.impinj.itemsense.client.coordinator.job.JobResponse;
import com.impinj.itemsense.client.coordinator.recipe.Recipe;
import com.impinj.itemsense.client.coordinator.recipe.RecipeController;
import com.impinj.itemsense.client.coordinator.user.User;
import com.impinj.itemsense.client.data.DataApiController;
import com.impinj.itemsense.client.data.item.Item;
import com.impinj.itemsense.scheduler.constants.ConnectorConstants;
import com.impinj.itemsense.scheduler.job.JobResult.Status;
import com.impinj.itemsense.scheduler.model.ItemSenseConfig;
import com.impinj.itemsense.scheduler.model.ItemSenseConfigJob;

public class ItemSenseHelper {
	private static final Logger logger = LoggerFactory.getLogger(ItemSenseHelper.class);

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
	public ItemSenseHelper(ItemSenseConfig config, ItemSenseConfigJob configJob, JobResult jobResult) {
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
			job.setStartDelay(configJob.getStartDelay());
		}
	}

	public boolean isJobFailed(String status) {
		return (status.contains(ConnectorConstants.ITEMSENSE_JOB_STATUS_FAIL));
	}

	public boolean isJobStopped(String status) {
		return (status.equals("STOPPED") || status.equals("COMPLETE") || status.equals("COMPLETE_WITH_ERRORS")
				|| status.equals("FAILED"));
	}

	public boolean isJobRunning(String status) {
		return (status.contains(ConnectorConstants.ITEMSENSE_JOB_STATUS_RUNNING) || status.equals("REGISTERED")
				|| status.equals("WAITING") || status.equals("INITIALIZING") || status.equals("STARTING")
				|| status.equals("PUBLISHING_STATE") || status.equals("STOPPED_STOPPING")
				|| status.equals("COMPLETE_STOPPING"));
	}

	public Client getClient() {
		// TODO: support auth token
		// lazy instantiation
		if (client == null) {
//			client = ClientBuilder.newClient()
//					.register(HttpAuthenticationFeature.basic(config.getUsername(), config.getPassword()));

			client = ClientBuilder.newClient().register(JacksonFeature.class)
					.register(HttpAuthenticationFeature.basic(config.getUsername(), config.getPassword()));

		}

		return client;
	}

	public CoordinatorApiController getItemsenseCoordinatorController() {

		String fullUrl = config.getUrl() + "/itemsense";

		if (itemsenseCoordinatorController == null) {
			itemsenseCoordinatorController = new CoordinatorApiController(getClient(), URI.create(fullUrl));
		}

		return itemsenseCoordinatorController;
	}

	public boolean testConnectionXX() {
		final String url = "http://192.168.0.98/itemsense";
		final String username = "admin";
		final String password = "admindefault";
		Client client = ClientBuilder.newClient().register(JacksonFeature.class)
				.register(HttpAuthenticationFeature.basic(username, password));

		CoordinatorApiController configApi = new CoordinatorApiController(client, URI.create(url));

		List<Facility> facilities = configApi.getFacilityController().getAllFacilities();

		if (facilities == null) {
			logger.error("Facilities is null - aborting");
			return false;
		}

		for (Facility facility : facilities) {
			logger.info(facility.toString());
		}

		DataApiController dataApi = new DataApiController(client, URI.create(url));

		List<Item> items = dataApi.getItemController().getAllItems();

		if (items == null) {
			logger.error("Items is null - aborting");
			return false;
		}

		for (Item item : items) {
			logger.info(item.toString());
		}

		return true;
	}

	public boolean testConnection() {
		boolean success = true;
		try {
			CoordinatorApiController coordinator = getItemsenseCoordinatorController();
			List<Recipe> recipes = coordinator.getRecipeController().getRecipes();
			success = (recipes==null ? false : 0 < recipes.size());
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("Could not validate ItemSense Connection: " + this.toString());
			success = false;
		}
		return success;
	}

	public ArrayList<JobResponse> getJobsInFacility() {

		JobController jobController = getItemsenseCoordinatorController().getJobController();
		List<JobResponse> jobs = jobController.getJobs();

		if (jobs != null) {
			return (ArrayList<JobResponse>) jobs.stream().filter(response -> {
				return Arrays.asList(response.getFacilities()).contains(configJob.getFacility());
			}).collect(Collectors.toList());
		} else {
			return new ArrayList<JobResponse>();
		}
	}

	public ArrayList<JobResponse> getRunningJobsInFacility() {
		ArrayList<JobResponse> jobResponses = (ArrayList<JobResponse>) getJobsInFacility().stream()
				.filter(jr -> isJobRunning(jr.getStatus())).collect(Collectors.toList());
		logger.info("Job (count: %d) RUNNING: %s facility: %s", jobResponses.size(), config.getName(),
				configJob.getFacility());
		return jobResponses;
	}

	public void stopRunningJobsInFacility() {
		String msg = String.format("ItemSense: %s Facility: %s", config.getName(), configJob.getFacility());
		JobController jobController = getItemsenseCoordinatorController().getJobController();
		ArrayList<JobResponse> runningJobs = getRunningJobsInFacility();

		for (JobResponse runningJobResponse : runningJobs) {
			logger.info("Stopping job: %s facility: %s JobID: %s", config.getName(), configJob.getFacility(),
					runningJobResponse.getId());
			jobController.stopJob(runningJobResponse.getId());
			// given ItemSense can only have one job running at a time per
			// facility, loop until has actually stopped

			// TODO: UPDATE FOR NEW JOB STATUS
			JobResponse stoppedJobResponse = jobController.getJob(runningJobResponse.getId());
			while (!isJobStopped(stoppedJobResponse.getStatus())) {
				logger.debug(msg + " stopping job " + runningJobResponse.getId() + ": status: "
						+ stoppedJobResponse.getStatus());
				stoppedJobResponse = jobController.getJob(runningJobResponse.getId());
			}

			logger.info(
					msg + " stopped job " + runningJobResponse.getId() + ": status: " + stoppedJobResponse.getStatus());
		}
	}

	public JobResult runJob() {
		if (configJob.isStopRunningJobs()) {
			stopRunningJobsInFacility();
		}

		logger.info("Starting Job: " + config.getName() + " facility: " + configJob.getFacility() + " job: " + job);

		// TODO: validate recipe exist before trying to start the job
		JobController jobController = getItemsenseCoordinatorController().getJobController();
		JobResponse jobResponse = jobController.startJob(job);
		if (isJobFailed(jobResponse.getStatus())) {
			String msg = String.format("FAILED to start Job in ItemSense: %s facility: %s  recipe: %s  jobId: %s",
					config.getName(), configJob.getFacility(), job.getRecipeName(), jobResponse.getId());
			this.jobResult.setStatus(Status.FAILED);
			this.jobResult.setResults(msg);

			logger.error("%s job: %s jobResponse: %s", msg, job, jobResponse);
			return this.jobResult;
		}

		// TODO: should there be retry logic?

		// the job has started, but is not yet "RUNNING"
		// hang out while it does initialization
		while (isJobRunning(jobResponse.getStatus())
				&& !jobResponse.getStatus().contains(ConnectorConstants.ITEMSENSE_JOB_STATUS_RUNNING)) {
			jobResponse = jobController.getJob(jobResponse.getId());
			// TODO: Fix to work correctly against new Status from and stop reason code
			// ItemSense
			if (isJobFailed(jobResponse.getStatus())) {
				String msg = String.format("FAILED to start Job in ItemSense: %s facility: %s recipe: %s jobId: %s",
						config.getName(), configJob.getFacility(), job.getRecipeName(), jobResponse.getId());
				this.jobResult.setStatus(Status.FAILED);
				this.jobResult.setResults(msg);

				logger.error("%s job: %s jobResponse: %s", msg, job, jobResponse);
				return this.jobResult;
			}
		}
		// TODO: should this be debug?
		String msg = String.format("Job is RUNNING: %s facility: %s recipe: %s jobId: %s", config.getName(),
				configJob.getFacility(), job.getRecipeName(), jobResponse.getId());
		this.jobResult.setStatus(Status.SUCCEEDED);
		this.jobResult.setResults(msg);

		logger.info("%s job: %s jobResponse: %s", msg, job, jobResponse);
		return this.jobResult;
	}

}
