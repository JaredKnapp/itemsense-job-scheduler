package com.impinj.itemsense.scheduler.events;

import com.impinj.itemsense.scheduler.model.ItemSenseConfig;

import javafx.event.Event;
import javafx.event.EventType;

public class ConfigEvent extends Event{
	
	private static final long serialVersionUID = 20180715L;

	private ItemSenseConfig data;
	
	public ItemSenseConfig getData() {
		return data;
	}

    public static final EventType<ConfigEvent> SAVE = new EventType<>(Event.ANY, "SAVE");
    public static final EventType<ConfigEvent> DELETE = new EventType<>(Event.ANY, "DELETE");
    public static final EventType<ConfigEvent> CANCEL = new EventType<>(Event.ANY, "CANCEL");
	
	
	public ConfigEvent(ItemSenseConfig configData, EventType<? extends Event> eventType) {
		super(eventType);
		
		this.data = configData;
	}

}
