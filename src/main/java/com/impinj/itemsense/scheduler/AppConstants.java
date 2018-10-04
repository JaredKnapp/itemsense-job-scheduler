package com.impinj.itemsense.scheduler;

import java.util.UUID;

/*
   Static Contants 
*/

public final class AppConstants {
	public static final String APP_TITLE = "ItemSense Job Scheduler";
	public static final String APP_ID = UUID.randomUUID().toString();

	public static final String ITEMSENSE_EPC_FORMAT_PURE_ID = "PURE_ID";
	public static final String ITEMSENSE_DEFAULT_FACILITY_NAME = "DEFAULT";
	public static final String ITEMSENSE_JOB_STATUS_RUNNING = "RUNNING";
	public static final String ITEMSENSE_JOB_STATUS_STOPPED = "STOPPED";
	public static final String ITEMSENSE_JOB_STATUS_COMPLETE = "COMPLETE";
	public static final String ITEMSENSE_JOB_STATUS_FAIL = "FAIL";

	public static final String JOB_DATA_MAP_ITEMSENSE_CONFIG = "ItemSenseConfig";
	public static final String JOB_DATA_MAP_JOB_CONFIG = "JobConfig";

	// Hide the constructor
	private AppConstants() {
	}

}
