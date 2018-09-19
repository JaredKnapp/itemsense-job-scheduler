package com.impinj.itemsense.scheduler.util;

import lombok.Data;

@Data
public class ComboChoice {

	public ComboChoice(int id, String name) {
		this.id = id;
		this.name = name;
	}

	private int id;
	private String name;

}
