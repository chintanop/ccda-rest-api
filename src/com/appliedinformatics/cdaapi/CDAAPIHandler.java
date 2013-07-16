package com.appliedinformatics.cdaapi;

import java.util.HashMap;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;

/**
 * 
 * @author Chintan Patel <chintan@trialx.com>
 * 
 * The API main runner. Takes input as specified port and runs on that port.
 *
 */
public class CDAAPIHandler extends Application{

	//An in-memory store of patient records for retrieval on future requests
	 public static HashMap<String, HashMap> patient_records = new HashMap<String, HashMap>();
	   
	 /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public Restlet createInboundRoot() {
        // Create a router Restlet that routes each call to a
        // new instance of CDAResource
    	
        Router router = new Router(getContext());
        // Defines only one route
       // router.attachDefault(CDAResource.class);
        
        router.attach("/bbplus", CDAResource.class);
        router.attach("/bbplus/{patient_id}/{section}", CDAResource.class);
        return router;
    }
    

    /**
     * Adds a patient record to the inmemory store
     * @param id: The unique patient record id
     * @param record: The record
     */
    public void addRecord(String id, HashMap record){
    		patient_records.put(id, record);
    }
    
    /**
     * Get the patient record associated with a given ID
     * @param id
     * @return HashMap: patient record elements
     */
    public HashMap getRecord(String id){
    	return patient_records.get(id);
    }
    
    /**
     * The main entry point of the application, if no argument specified then starts
     * the server on default port of 8182
     * @param args
     */
    public static void main(String[] args) {
        try {
        	
        	// Default Port
        	int port = 8182;
        	
        	if (args.length == 1){
        		try{
        			port = Integer.parseInt(args[0]);
        		}catch(NumberFormatException nfe){
        			System.out.println("Please enter a numeric argument for the port");
        			System.exit(0);
        		}
        	}
        	
            // Create a new Component.
            Component component = new Component();

            // Add a new HTTP server listening on port
            component.getServers().add(Protocol.HTTP, port);

            // Attach the sample application.
            component.getDefaultHost().attach(new CDAAPIHandler());

            // Start the component.
            component.start();
        } catch (Exception e) {
            // Something is wrong.
            e.printStackTrace();
        }
    }
      
}
