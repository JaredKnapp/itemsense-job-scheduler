package com.impinj.itemsense.scheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public @Data class ItemSenseConfigJob {

    private static final String KEY_SEP = ".";

	@Getter
	@Setter
        @JsonIgnore
	private String itemSenseOid;

	@Getter
	@Setter
        @JsonIgnore
	private String oid;
	
	@Getter
	@Setter
	private boolean active;

	@Getter
	@Setter
	private String facility;

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private String recipe;

	@Getter
	@Setter
	private String jobType;

	@Getter
	@Setter
	private String schedule;

	@Getter
	@Setter
	private String startDelay;

	@Getter
	@Setter
	private Integer duration;

	@Getter
	@Setter
	private boolean stopRunningJobs;

        @JsonIgnore
        public String getJobKey() {
            return facility + KEY_SEP + name + KEY_SEP + recipe;
        }
}
