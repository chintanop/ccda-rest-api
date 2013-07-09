package com.appliedinformatics.cdaapi;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.appliedinformatics.cdaapi.parser.CDAParser;
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
		 	Form queryParams = getRequest().getResourceRef().getQueryAsForm(); 
		 
		 	Request request = getRequest();
		
		 	//String patientID = (String) queryParams.getFirstValue("patient"); 
		   // String section 	 = (String) queryParams.getFirstValue("section"); 
		    
		 	String patientID = (String)request.getAttributes().get("patient_id");
		 	String section = (String)request.getAttributes().get("section");
		 	
			CDAAPIHandler app = (CDAAPIHandler)getApplication();
		
			HashMap record = (HashMap)app.getRecord(patientID);
			
			//System.out.println("Patient ID:"+patientID);
			//System.out.println("Section:"+section);
			
			Gson gson = new Gson();
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
		 
		//Create a cdaParser
		CDAParser cdaParser = new CDAParser(entity.getStream());

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
		HashMap ptid = new HashMap();
		ptid.put("patient_id", patient_id);
		System.out.println(patient_id);
		Gson gson = new Gson();
		return gson.toJson(ptid);
	 }
	 
	 @Override
	 public Application getApplication() {
	     return (Application) super.getApplication();
	 }
	 
	 /*
	 public ArrayList getMedications(ContinuityOfCareDocument ccd){
			
			ArrayList medList = new ArrayList<HashMap>();
			MedicationsSection medSection = ccd.getMedicationsSection();
			for (SubstanceAdministration sa : medSection.getSubstanceAdministrations()) {
				
				HashMap<String, Object> med = new HashMap<String, Object>();
				
				Consumable consumable = sa.getConsumable();
				ManufacturedProduct manufacturedProduct = consumable.getManufacturedProduct();
				Material mf = manufacturedProduct.getManufacturedMaterial();
				ENImpl mname = (ENImpl) mf.getName();
				
				String med_name = "";
				if (mname != null) {
					med_name = mname.getText();
				} else {
					med_name = mf.getCode().getDisplayName();
				}
				
				//Dose + repeat number
				IVL_PQ dose 	= sa.getDoseQuantity();
				String dosage =  ""+dose.getValue();
				
				//Time
				String effectiveTimeStr = "";
				
				for(SXCM_TS effectiveTime :sa.getEffectiveTimes()){
					System.out.println(effectiveTime);
					if (effectiveTime instanceof PIVL_TS){
						PIVL_TS periodTS = (PIVL_TS)effectiveTime;
						effectiveTimeStr += ""+periodTS.getPeriod().getValue()+"^"+periodTS.getPeriod().getUnit();
					}
					/*if (effectiveTime instanceof IVL_TS){
						IVL_TS ivlTS = (IVL_TS)effectiveTime;
						effectiveTimeStr += ""+ivlTS.getValue();
					}*/
	/*			}
				//Route code
				CE routeCode = sa.getRouteCode();			
				String route = "";
				if(routeCode!=null){
					if(routeCode.getDisplayName()!=null){
						route  = routeCode.getDisplayName();
					}
					if(routeCode.getOriginalText()!=null){
						route  = routeCode.getOriginalText().getText();
					}
				}
				
				med.put("name", med_name);
				med.put("dose", dosage);
				med.put("route", route);
				med.put("effectiveTime", effectiveTimeStr);	
				medList.add(med);
			}	
			return medList;
		}*/
	 
}
