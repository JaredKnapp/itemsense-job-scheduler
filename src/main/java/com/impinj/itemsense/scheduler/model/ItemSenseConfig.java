package com.impinj.itemsense.scheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public @Data class ItemSenseConfig {

	@Getter
	@Setter
        @JsonIgnore
	private String oid;

	@Getter
	@Setter
        @JsonIgnore
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
	private List<ItemSenseConfigJob> jobList;

        @JsonIgnore
	public List<ItemSenseConfigJob> getActiveJobs() {
		return (List<ItemSenseConfigJob>) getJobs().stream().filter(itemSenseJob -> itemSenseJob.isActive())
				.collect(Collectors.toList());
	}

        @JsonIgnore
	public List<ItemSenseConfigJob> getJobs() {
		if (jobList == null) {
			jobList = new ArrayList<ItemSenseConfigJob>();
		}
		return jobList;
	}
}
