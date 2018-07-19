package com.impinj.itemsense.scheduler.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.impinj.itemsense.scheduler.config.SystemConfiguration;
import com.impinj.itemsense.scheduler.model.ItemSense;
import com.impinj.itemsense.scheduler.util.JsonMapper;

public class DataService {
	private static final Logger logger = LoggerFactory.getLogger(JsonMapper.class);

	private final String jobConfigDir = "target/classes";
	private final String jobConfigMasterfileJson = "SystemConfiguration.json";
	private final Boolean filesFromClasspath = false;

	private SystemConfiguration config;

	public static ArrayList<ItemSense> load() throws IOException {
		DataService service = new DataService();
		return service.config.getItemSenseConfigs();
	}

	private DataService() throws IOException {
		loadSystemConfig();
//		loadItemsenseConfigs();
	}

	private void loadSystemConfig() {
		logger.info("Loading Connector configuration.");
		try (JsonMapper<SystemConfiguration> mapper = new JsonMapper<SystemConfiguration>(jobConfigDir,
				jobConfigMasterfileJson, new TypeReference<SystemConfiguration>() {
				}, filesFromClasspath)) {
			config = mapper.read();
			logger.info("Successfully loaded " + mapper.getResourceFile());
		} catch (FileNotFoundException fnfe) {
			config = new SystemConfiguration();
			logger.warn(
					"Master Config file not found (ok for initial configuration.  From application.properties: job.config.dir"
							+ jobConfigDir + " job.config.masterfile.json: " + jobConfigMasterfileJson
							+ " job.config.fromClasspath: " + filesFromClasspath);
		}
	}

//	private void loadItemsenseConfigs() {
//		List<String> isFiles = config.getConfigFiles();
//		itemSenseConfigs = new ArrayList<>();
//		logger.debug("itemSense Files to load:" + isFiles);
//		isFiles.stream().forEach(isFile -> {
//			ItemSense isConfig = loadItemsenseConfig(isFile);
//
//			String isOid = OIDGenerator.next();
//			isConfig.setOid(isOid);
//			isConfig.setFileName(isFile);
//
//			logger.debug("itemsenseConfig loaded: " + isConfig);
//			itemSenseConfigs.add(isConfig);
//
//			// now process the jobs
//			if (isConfig.getJobList() != null) {
//				logger.debug("itemSenseConfig.getJobList().size(): " + isConfig.getJobList().size());
//				for (Job jobConfig : isConfig.getJobList()) {
//					jobConfig.setOid(OIDGenerator.next());
//					jobConfig.setName(isConfig.getName());
//					jobConfig.setItemSenseOid(isConfig.getOid());
//				}
//			}
//			// if (isconfig.getidcloudjoblist()!=null) {
//			// logger.debug("storeconfig.getidcloudjoblist().size(): " +
//			// isconfig.getidcloudjoblist().size());
//			// isconfig.getidcloudjoblist().stream().foreach( idcloudjob -> {
//			// idcloudjob.setoid(oidgenerator.next());
//			// idcloudjob.setstorename(storeconfig.getstorename());
//			// idcloudjob.setstoreoid(storeconfig.getoid());
//			// });
//			// }
//		});
//
//	}

	private ItemSense loadItemsenseConfig(String isFile) {
		logger.debug("Loading itemsense: " + isFile + " from " + jobConfigDir);
		try (JsonMapper<ItemSense> mapper = new JsonMapper<ItemSense>(jobConfigDir, isFile,
				new TypeReference<ItemSense>() {
				}, filesFromClasspath)) {
			return mapper.read();
		} catch (FileNotFoundException fnfe) {
			logger.error("ItemSense Config file not found: " + isFile + "(From application.properties: job.config.dir"
					+ jobConfigDir + " job.config.masterfile.json: " + jobConfigMasterfileJson
					+ " job.config.fromClasspath: " + filesFromClasspath + ")");
			return null;
		}
	}

}
