package com.impinj.itemsense.scheduler.model;

import java.util.List;

import lombok.Data;

public @Data class ItemSense {
	private String oid;
	private String fileName;

	private String name;
	private String url;
	private String username;
	private String password;
	private String token;
	private Boolean active;
	private String utcOffset;
	private List<Job> jobList;
}
