package com.impinj.itemsense.scheduler.util;


import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Reads a object from file on the class path or file system.
 * Created by Paul Hill on 6/7/2016.
 */
public class JsonMapper<T> implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(JsonMapper.class);

    public static final String FILE_SEP = System.getProperty("file.separator");

    private final String dir;
    private final TypeReference<T> typeReference;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String filename;
    private String resourceFile;
    private final boolean fromClasspath;

    public JsonMapper(String jsonDir, String filename, TypeReference<T> tr, boolean fromClasspath) {
        this.typeReference = tr;
        this.dir = jsonDir;
        this.filename = filename;
        this.fromClasspath = fromClasspath;

        this.resourceFile = (!fromClasspath && dir!=null && dir.length()>0) ? resourceFile=dir + FILE_SEP + filename : filename;

  		logger.info("New JsonMapper: resourceFile: " + resourceFile + " job.config.fromClasspath=" + fromClasspath + " job.config.dir=" + dir + " job.config.masterfile.json=" + filename);
    }

    /**
     * @return One of the items read from a json file.
     * @throws FileNotFoundException 
     */
    public T read() throws FileNotFoundException {
        try {
            InputStream conf = getStream();
            T contents = mapper.readValue(conf, typeReference);
            return contents;
        } catch (FileNotFoundException fnfe) {
        	// this is ok the first time so warn and let the caller decide
        	throw fnfe;
        } catch (Exception e){
            throw new RuntimeException("Unable to read file " + resourceFile + " from " +
                   ((fromClasspath)? "classpath." : "file system."), e);
        }
    }

    private InputStream getStream() throws FileNotFoundException {
        InputStream stream;
        if (fromClasspath) {
            stream = this.getClass().getClassLoader().getResourceAsStream(resourceFile);
        } else {
            File source = new File(resourceFile);
            stream = new FileInputStream(source);
        }
        return stream;
    }

    public void write(T object) throws IOException {
  		logger.info("Write file to classpath: resourceFile: " + resourceFile);
    	if (fromClasspath==true) {
    		logger.warn("Fix application.properties: Attempting to write file to classpath: dir: " + dir + " filename: " + filename);
    	}
        File jsonFile = new File(resourceFile);
        ObjectWriter objectWriter = mapper.writerWithDefaultPrettyPrinter();
        try (Writer sw = new FileWriter(jsonFile) ) {
            objectWriter.writeValue(sw, object);
        }
    }
    
    @Override
    public void close() {
       // n/a
    }
    
    public String getResourceFile () {
    	return resourceFile;
    }
    	
}
