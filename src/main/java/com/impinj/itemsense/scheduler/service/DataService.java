package com.impinj.itemsense.scheduler.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.impinj.itemsense.scheduler.config.SystemConfiguration;
import com.impinj.itemsense.scheduler.model.ItemSenseConfig;
import com.impinj.itemsense.scheduler.model.ItemSenseConfigJob;
import com.impinj.itemsense.scheduler.util.JsonMapper;
import com.impinj.itemsense.scheduler.util.OIDGenerator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class DataService {
	private static final Logger logger = LoggerFactory.getLogger(JsonMapper.class);
	private static DataService service;

	public static DataService getService(boolean createIfNull) throws IOException {
		if (service == null && createIfNull)
			service = new DataService();
		return service;
	}

	private final String jobConfigDir = ".";  // Run config files from current folder
	private final String jobConfigMasterfileJson = "SystemConfiguration.json";

	private final Boolean filesFromClasspath = false;

	private SystemConfiguration systemConfig;
        
        final private String DefaultConfigString = "{" +
            "\"itemSenseConfigs\" : [{ \n"+
		"  \"active\": true, \n"+
		"  \"name\": \"Impinj Test Store 1\", \n"+
		"  \"utcOffset\": \"-7\", \n"+
		"  \"url\": \"http://<Enter Hostname>\", \n"+
		"  \"username\": \"admin\", \n"+
		"  \"password\": \"admindefault\", \n"+
		"  \"token\": \"\", \n"+
		"  \"jobList\": [ \n"+
			"    { \n"+
			"      \"active\": true, \n"+
			"      \"facility\": \"Training\", \n"+
			"      \"name\": \"Deep Inventory Every Minute\", \n"+
			"      \"recipe\": \"IMPINJ_Deep_Scan_Inventory\", \n"+
			"      \"jobType\": \"Inventory\", \n"+
			"      \"schedule\": \"0 0/5 * * * ?\", \n"+
			"      \"startDelay\": \"0\", \n"+
			"      \"duration\": 55, \n"+
			"      \"stopRunningJobs\": true \n"+
		    " } \n"+
	    " ] \n"+
	" }], \n"+
        " \"filePath\": \".\" \n"+
        "}";


	private DataService() throws IOException {
		loadSystemConfig();
	}

	public ItemSenseConfig getItemSenseConfigByOID(String oid) throws Exception {
		try {
			return this.systemConfig.getItemSenseConfigs().stream().filter(itemsense -> itemsense.getOid().equals(oid))
					.collect(Collectors.toList()).get(0);
		} catch (Exception e) {
			throw new Exception("OID Does not exist");
		}
	}

	public SystemConfiguration getSystemConfig() {
		return this.systemConfig;
	}

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

	private void loadSystemConfig() {
		logger.info("Loading Connector configuration.");
                // jobConfigMasterfileJson doesn't exist, create it with 1 default config to be used as an example.
                File file = new File(jobConfigDir+"/"+jobConfigMasterfileJson);
                BufferedWriter output=null;
                try {
                    if (!file.exists()) {
                        output = new BufferedWriter(new FileWriter(file));
                        output.write(DefaultConfigString);
                        output.close();
                    }
                 } catch ( IOException e ) {
                    logger.error("Not able to create "+jobConfigMasterfileJson);
                    System.exit(0); // quit
                 }
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

	public void saveSystemConfig() throws IOException {
		JsonMapper<SystemConfiguration> mapper = new JsonMapper<SystemConfiguration>(jobConfigDir,
				jobConfigMasterfileJson, new TypeReference<SystemConfiguration>() {
				}, filesFromClasspath);
		mapper.write(systemConfig);
		logger.info("Successfully Written " + mapper.getResourceFile());
		mapper.close();
	}
}
