package com.impinj.itemsense.scheduler.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public @Data class ItemSense {
	private String oid;
	private String fileName;

	@Getter
	@Setter
	private boolean active;

	@Getter
	@Setter
	private String name;
	
	@Getter
	@Setter
	private String url;
	
	@Getter
	@Setter
	private String username;
	
	@Getter
	@Setter
	private String password;
	
	@Getter
	@Setter
	private String token;
	
	@Getter
	@Setter
	private String utcOffset;
	
	@Getter
	@Setter
	private List<ItemSenseJob> jobList;
	
	public List<ItemSenseJob> getActiveJobs() {
		return (List<ItemSenseJob>) getJobs().stream().filter(itemSenseJob -> itemSenseJob.isActive()).collect(Collectors.toList());
	}

	public List<ItemSenseJob> getJobs() {
		if (jobList == null) {
			jobList = new ArrayList<ItemSenseJob>();
		}
		return jobList;
	}
}
