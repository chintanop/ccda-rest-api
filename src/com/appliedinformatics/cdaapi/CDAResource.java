package com.appliedinformatics.cdaapi;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.Consumable;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.Material;
import org.openhealthtools.mdht.uml.cda.SubstanceAdministration;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.MedicationsSection;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.cda.util.ValidationResult;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.PIVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.SXCM_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.impl.ENImpl;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.util.Series;

import com.appliedinformatics.cdaapi.mu2parser.CDAParser;
import com.google.gson.Gson;

/**
 * 
 * CDAResource object. Exposes the API calls GET, POST on the CDA object
 *  
 * @author Chintan Patel <chintan@trialx.com>
 *
 */
public class CDAResource extends ServerResource{

	/**
	 * Handle the GET requests on the root
	 * 
	 * @param patient_id: obtained from the POST request 
	 * @param section: The patient record section ["demographics","medications", "results", "allergies", "problems"]
	 * 
	 * @return JSON object of the requested section
	 */
	 @Get
	 public String represent() {
		 	Form queryParams 	= getRequest().getResourceRef().getQueryAsForm(); 
		 
		 	Request request 	= getRequest();
		
		 	//String patientID 	= (String) queryParams.getFirstValue("patient"); 
		   // String section  	= (String) queryParams.getFirstValue("section"); 
		    
		 	String patientID 	= (String)request.getAttributes().get("patient_id");
		 	String section 		= (String)request.getAttributes().get("section");
		 	
			CDAAPIHandler app 	= (CDAAPIHandler)getApplication();
		
			HashMap record 		= (HashMap)app.getRecord(patientID);
			
			//System.out.println("Patient ID:"+patientID);
			//System.out.println("Section:"+section);
			
			Gson gson = new Gson();
			
			//TODO: handle XML op_format
			
			//To Handle cross-domain AJAX requests
			Series<Header> responseHeaders = (Series<Header>)getResponse().getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
			if (responseHeaders == null) {
				responseHeaders = new Series(Header.class);
  				getResponse().getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS,responseHeaders);
			}
			responseHeaders.add(new Header("Access-Control-Allow-Origin", "*"));
							
			//Handle JSONP
		 	String callback 	= (String) queryParams.getFirstValue("callback"); 
		 	if (callback!=null){
		 		return callback+"("+gson.toJson(record.get(section))+")";
		 	}
		 	
		 	return gson.toJson(record.get(section));
		    
	 }
	 
	 
	 /**
	  * Handle the POST requests. Takes a CDA document string as input and returns a unique 
	  * patient_id that can be used to make future GET requests for specific sections of patient record
	  *  
	  * @param entity
	  * @return patient_id, status of parsing, warnings, errors 
	  * @throws Exception
	  */
	 @Post
	 public String acceptRepresentation(Representation entity)   throws Exception {
		// System.out.println(entity.getText());
		 
		Form queryParams = new Form(getRequest().getEntity());
		
		String ccda_xml = (String)queryParams.getFirstValue("bbfile");
		
		//System.out.println(ccda_xml);
		
		InputStream is = new ByteArrayInputStream(ccda_xml.getBytes("UTF-8"));
		
		//System.out.println("CCDA XML"+ccda_xml);
		
		//Create a cdaParser
		CDAParser cdaParser = new CDAParser(is);

		//Get demographics
		HashMap ptDemo 		= cdaParser.getDemographics();

		String patient_id 	= (String)ptDemo.get("id");
		
		//Get all sections
		HashMap record = new HashMap();
		record.put("demographics", ptDemo);
		record.put("medications", cdaParser.getMedications());
		record.put("results", cdaParser.getResults());
		record.put("allergies", cdaParser.getAllergies());
		record.put("problems", cdaParser.getProblems());
		
		//Store the parsed sections into global application storage
		CDAAPIHandler app = (CDAAPIHandler)getApplication();
		app.addRecord(patient_id, record);
		
		//Return JSON
		//TODO: handle XML op_format parameter
		HashMap ptid = new HashMap();
		ptid.put("patient_id", patient_id);
		System.out.println(patient_id);
		
		HashMap recorda 		= (HashMap)app.getRecord(patient_id);
		
		System.out.println(recorda.get("demographics"));
		
		Gson gson = new Gson();
		
		//To Handle cross-domain AJAX requests: TODO: CORS doesn't seem to be working with Jquery even though headers are returning correctly
		Series<Header> responseHeaders = (Series<Header>)getResponse().getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
		if (responseHeaders == null) {
			responseHeaders = new Series(Header.class);
				getResponse().getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS,responseHeaders);
		}
		responseHeaders.add(new Header("Access-Control-Allow-Origin", "*"));
		
		//TODO: handle XML op_format
		
		//Handle JSONP - WORKS!
	 	String callback 	= (String) queryParams.getFirstValue("callback"); 
	 	if (callback!=null){
	 		return callback+"("+gson.toJson(ptid)+")";
	 	}
	 	
        return gson.toJson(ptid);
		
	 }
	 
	 @Override
	 public Application getApplication() {
	     return (Application) super.getApplication();
	 }
	
	 @Options
	 public void doOptions(Representation entity) {
		 
	    /* Form responseHeaders = (Form) getResponse().getAttributes().get("org.restlet.http.headers"); 
	     if (responseHeaders == null) { 
	         responseHeaders = new Form(); 
	         getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders); 
	     } 
	     responseHeaders.add("Access-Control-Allow-Origin", "*"); 
	     responseHeaders.add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
	     //responseHeaders.add("Access-Control-Allow-Headers", "Content-Type"); 
	    // responseHeaders.add("Access-Control-Allow-Credentials", "false"); 
	     responseHeaders.add("Access-Control-Max-Age", "60"); */
	     
	     Series<Header> responseHeaders = (Series<Header>)getResponse().getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
			if (responseHeaders == null) {
				responseHeaders = new Series(Header.class);
				getResponse().getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS,responseHeaders);
			}
			responseHeaders.add(new Header("Access-Control-Allow-Origin", "*"));
				
	 }
	 
}
