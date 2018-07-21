package com.impinj.itemsense.scheduler.model;

import lombok.Data;

public @Data class ItemSenseJob {
	private String itemSenseOid;
	private String oid;
	private boolean active;
	private String facility;
	private String name;
	private String recipe;
	private String jobType;
	private String schedule;
	private String startDelay;
	private Integer duration;
	private boolean stopRunningJobs;

}
