package com.impinj.itemsense.scheduler.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.impinj.itemsense.scheduler.config.SystemConfiguration;
import com.impinj.itemsense.scheduler.model.ItemSenseConfig;
import com.impinj.itemsense.scheduler.model.ItemSenseConfigJob;
import com.impinj.itemsense.scheduler.util.JsonMapper;
import com.impinj.itemsense.scheduler.util.OIDGenerator;

public class DataService {
	private static final Logger logger = LoggerFactory.getLogger(JsonMapper.class);

	private final String jobConfigDir = "target/classes";
	private final String jobConfigMasterfileJson = "SystemConfiguration.json";
	private final Boolean filesFromClasspath = false;

	private SystemConfiguration systemConfig;

	public static ArrayList<ItemSenseConfig> load() throws IOException {
		DataService service = new DataService();
		return service.systemConfig.getItemSenseConfigs();
	}

	private DataService() throws IOException {
		loadSystemConfig();
	}

	private void loadSystemConfig() {
		logger.info("Loading Connector configuration.");
		try (JsonMapper<SystemConfiguration> mapper = new JsonMapper<SystemConfiguration>(jobConfigDir,
				jobConfigMasterfileJson, new TypeReference<SystemConfiguration>() {
				}, filesFromClasspath)) {
			systemConfig = mapper.read();
			logger.info("Successfully loaded " + mapper.getResourceFile());
			systemConfig.getItemSenseConfigs().forEach(configData -> {
				configData.setOid(OIDGenerator.next());
				if (configData.getJobList() != null) {
					logger.debug("itemSenseConfig.getJobList().size(): " + configData.getJobList().size());
					configData.getJobList().forEach(jobConfig -> {
						jobConfig.setOid(OIDGenerator.next());
						jobConfig.setName(configData.getName());
						jobConfig.setItemSenseOid(configData.getOid());
					});
				}
			});
		} catch (FileNotFoundException fnfe) {
			systemConfig = new SystemConfiguration();
			logger.warn(
					"Master Config file not found (ok for initial configuration.  From application.properties: job.config.dir"
							+ jobConfigDir + " job.config.masterfile.json: " + jobConfigMasterfileJson
							+ " job.config.fromClasspath: " + filesFromClasspath);
		}
	}

	// private void loadItemsenseConfigs() {
	// List<String> isFiles = config.getConfigFiles();
	// itemSenseConfigs = new ArrayList<>();
	// logger.debug("itemSense Files to load:" + isFiles);
	// isFiles.stream().forEach(isFile -> {
	// ItemSense isConfig = loadItemsenseConfig(isFile);
	//
	// String isOid = OIDGenerator.next();
	// isConfig.setOid(isOid);
	// isConfig.setFileName(isFile);
	//
	// logger.debug("itemsenseConfig loaded: " + isConfig);
	// itemSenseConfigs.add(isConfig);
	//
	// // now process the jobs
	// if (isConfig.getJobList() != null) {
	// logger.debug("itemSenseConfig.getJobList().size(): " +
	// isConfig.getJobList().size());
	// for (Job jobConfig : isConfig.getJobList()) {
	// jobConfig.setOid(OIDGenerator.next());
	// jobConfig.setName(isConfig.getName());
	// jobConfig.setItemSenseOid(isConfig.getOid());
	// }
	// }
	// // if (isconfig.getidcloudjoblist()!=null) {
	// // logger.debug("storeconfig.getidcloudjoblist().size(): " +
	// // isconfig.getidcloudjoblist().size());
	// // isconfig.getidcloudjoblist().stream().foreach( idcloudjob -> {
	// // idcloudjob.setoid(oidgenerator.next());
	// // idcloudjob.setstorename(storeconfig.getstorename());
	// // idcloudjob.setstoreoid(storeconfig.getoid());
	// // });
	// // }
	// });
	//
	// }

	private ItemSenseConfig loadItemsenseConfig(String isFile) {
		ItemSenseConfig config = null;
		logger.debug("Loading itemsense: " + isFile + " from " + jobConfigDir);
		try (JsonMapper<ItemSenseConfig> mapper = new JsonMapper<ItemSenseConfig>(jobConfigDir, isFile,
				new TypeReference<ItemSenseConfig>() {
				}, filesFromClasspath)) {
			config = mapper.read();
			config.setOid(OIDGenerator.next());
			// now process the jobs
			if (config.getJobList() != null) {
				logger.debug("itemSenseConfig.getJobList().size(): " + config.getJobList().size());
				for (ItemSenseConfigJob jobConfig : config.getJobList()) {
					jobConfig.setOid(OIDGenerator.next());
					jobConfig.setName(config.getName());
					jobConfig.setItemSenseOid(config.getOid());
				}
			}
		} catch (FileNotFoundException fnfe) {
			logger.error("ItemSense Config file not found: " + isFile + "(From application.properties: job.config.dir"
					+ jobConfigDir + " job.config.masterfile.json: " + jobConfigMasterfileJson
					+ " job.config.fromClasspath: " + filesFromClasspath + ")");
			return null;
		}
		return config;
	}

}
