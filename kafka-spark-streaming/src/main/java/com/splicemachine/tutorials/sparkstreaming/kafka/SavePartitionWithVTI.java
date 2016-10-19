/*
 * Copyright 2012 - 2016 Splice Machine, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.splicemachine.tutorials.sparkstreaming.kafka;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by Erin Driggers
 */
public class SavePartitionWithVTI implements VoidFunction<Iterator<String>>, Serializable{

    /**
     * Log object to log messages.  The log messages in this class will appear
     * in the HBase Region Server log file and the splice-derby.log file
     */
    private static final Logger LOG = Logger
            .getLogger(SavePartitionWithVTI.class);
    
    /**
     * JDBC Connection String to connect to Splice Machine
     */
    private String jdbcConnectionUrl;
    
    /**
     * Constructor for saving a partition to Splice Machine.  
     * 
     * @param jdbcConnectionUrl - JDBC url for splice machine
     */
    public SavePartitionWithVTI(String jdbcConnectionUrl) {
        this.jdbcConnectionUrl = jdbcConnectionUrl;
    }

    public void call(Iterator<String> sensorMessagesRdd) throws Exception {
  
        if(sensorMessagesRdd!=null) {

            LOG.info("Data to process:");   
            
          //Convert to list 
            List<String> sensorMessages = new ArrayList <String>();
            while(sensorMessagesRdd.hasNext()){
                sensorMessages.add(sensorMessagesRdd.next());
            }

            int numRcds = sensorMessages.size();
            
            if(numRcds > 0) {
                LOG.info("numRcds:" + numRcds); 
                Connection con = DriverManager.getConnection(jdbcConnectionUrl);
                
                String vtiStatement = "INSERT INTO IOT.SENSOR_MESSAGES " +
                        "select s.* from new com.splicemachine.tutorials.sparkstreaming.kafka.SensorMessageVTI(?) s (" + SensorMessage.getTableDefinition() + ")";
                
                processMessageList(con,sensorMessages, vtiStatement);
                /*
                PreparedStatement ps = con.prepareStatement(vtiStatement);
                
                ps.setObject(1,sensorMessages);
                try {
                    ps.execute();
                } catch (Exception e) {
                    LOG.error("Exception inserting data:" + e.getMessage(), e); 
                } finally {
                    LOG.info("Inserted Complete");
                }
                */
            }
                       
        }
    }
    
    /**
     * This loops through the list of messages and builds a shorten list of messages to
     * get around a current defect in Splice Machine where an object's size is being compared
     * to a string size and therefore only allowing objects of sizes 32,000.
     * 
     * Once this defect is fixed this code can be removed and a call to 'saveToDatabase' can
     * be made directly
     * 
     * @param messagesList
     * @param type
     * @param vti
     */
    public void processMessageList(Connection con, List<String> messagesList, String vti) {
        int length = 0;
        List<String> shortMessageList = new ArrayList<String>();
        for(String msg : messagesList) {
            int currentMessageLength = msg.length();
            if((length + currentMessageLength) < 10000) {
                length += currentMessageLength;
                shortMessageList.add(msg);
            } else {
                saveToDatabase(vti, shortMessageList, con);
                length = currentMessageLength;
                shortMessageList = new ArrayList<String>();
                shortMessageList.add(msg);
            }
        }                    
        if(shortMessageList.size() > 0) {
           saveToDatabase(vti, shortMessageList, con);
        } 
    }
    
    /**
     * Calls the VTI in splice machine passing in an array of strings
     * 
     * @param vti
     * @param shortMessageList
     * @param con
     * @param type
     * @return
     */
    public boolean saveToDatabase(String vti, List<String> shortMessageList, Connection con) {
        boolean success = false;
        

        try {
            PreparedStatement ps = con.prepareStatement(vti);
            ps.setObject(1,shortMessageList);

            LOG.error("NEW Prepared about to execute vti:" + shortMessageList.size());
            ps.execute();     
            success = true;
        } catch (Exception e) {
            success = false;
            LOG.error("Exception inserting data:" + " failed" + e.getMessage(), e); 
        } finally {
            if(!success) {
                LOG.info("Error for saving to the splice machine database");
            }
        } 
        
        return success;
    }

}
