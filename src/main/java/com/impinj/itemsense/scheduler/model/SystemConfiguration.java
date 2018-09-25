package com.impinj.itemsense.scheduler.model;

import java.util.ArrayList;

import lombok.Data;

public @Data class SystemConfiguration {
	// @JsonProperty(required = true)
	// private List<String> configFiles = new ArrayList<String>();

	// @JsonProperty(required = true)
	private ArrayList<ItemSenseConfig> itemSenseConfigs;
	// @JsonProperty(required = true)
	private String filePath;
}
