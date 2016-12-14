package org.openpaas.servicebroker.pinpoint.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openpaas.servicebroker.pinpoint.exception.PinpointServiceException;
import org.openpaas.servicebroker.pinpoint.model.PinpointJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Pinpoint 를 Binding을 할때 Collector 정보 정의
 */

public class PinpointConfig {

    public static final String SERVICE_DIR = "/var/vcap/store/broker";
//    public static final String SERVICE_DIR = "/home/vcap/app/broker";
    public static final String sFile = "pinpoint.json";

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(PinpointConfig.class);

    public void mkdirs(){
        File bindingDir = new File(SERVICE_DIR);
        if (!bindingDir.exists()) {
            logger.debug("mkdirs : " + bindingDir.getAbsolutePath());
            bindingDir.mkdirs();
        }

    }
    public void createFile() throws PinpointServiceException{
        File bindingFile = new File(SERVICE_DIR + "/" + sFile);
        if (!bindingFile.exists()) {
            try {
                logger.debug("createFile : " + bindingFile.getAbsolutePath());
                bindingFile.createNewFile();
                FileWriter writer = new FileWriter(bindingFile.getAbsolutePath());
                PinpointJson dummy = PinpointJson.createDummy();
                String json = dummy.toJsonString();
                writer.write(json);
                writer.flush();
                writer.close();
            }catch (IOException e){
                throw new PinpointServiceException(this.getClass().getName()+":createFile");
            }
        }
    }
    public File getJsonFile() throws PinpointServiceException{
        mkdirs();
        createFile();
        File bindingFile = new File(SERVICE_DIR + "/" + sFile);
        return bindingFile;
    }
    public PinpointJson getPinpointJson() throws  PinpointServiceException{
        ObjectMapper objectMapper = new ObjectMapper();
        PinpointJson pinpointJson;
        try {
            pinpointJson = objectMapper.readValue(getJsonFile(), PinpointJson.class);
        }catch (IOException e){
            throw new PinpointServiceException(e.toString());
        }
        return pinpointJson;
    }
    public void writePinpointJson(String sPinpointJson) throws  PinpointServiceException{
        File bindingFile = new File(SERVICE_DIR + "/" + sFile);
        try{
            FileWriter  writer = new FileWriter(bindingFile.getAbsolutePath());
            writer.write(sPinpointJson);
            writer.flush();
            writer.close();

        }catch(IOException e){
            throw new PinpointServiceException(this.getClass().getName()+":writePinpointJson:"+e.getLocalizedMessage());
        }

    }

}
