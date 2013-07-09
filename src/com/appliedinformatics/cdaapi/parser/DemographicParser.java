package com.appliedinformatics.cdaapi.parser;

import java.util.HashMap;

import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.ENXP;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;
import org.openhealthtools.mdht.uml.cda.RecordTarget;

/**
 * 
 * @author Chintan Patel <chintan@trialx.com>
 *
 * DemographicParser: Parses demographic fields from CDA into JSON
 *
 */
public class DemographicParser {

	ClinicalDocument cd = null;
	
	public DemographicParser(ClinicalDocument cd){
		this.cd = cd;
	}
	
	public HashMap parse(){
		
		RecordTarget recordTarget = cd.getRecordTargets().get(0);
		
		HashMap ptInfo = new HashMap();
		
		org.openhealthtools.mdht.uml.cda.Patient patient = recordTarget.getPatientRole().getPatient();
		
		String id ="" ,first = "", last = "", birthTime = "", gender = "", marital_status = "", provider_org = "", languages = "";
		String street = "", city = "", state = "", country = "";

		
		for(PN pname : patient.getNames()){
			if(pname.getFamilies().size()>0){
				last = ((ENXP)pname.getFamilies().get(0)).getText();
			}
			if(pname.getGivens().size()>0){
				first = ((ENXP)pname.getGivens().get(0)).getText();
			}
		}
	
		if(patient.getBirthTime() != null){
			birthTime = patient.getBirthTime().getValue();	
		}
		
		CE gcode = patient.getAdministrativeGenderCode();
		if (gcode != null)
			gender = gcode.getDisplayName();
		if(gender == null)
				gender = "";
		
		CE mcode = patient.getMaritalStatusCode();
		if (mcode != null)
			marital_status = mcode.getDisplayName();
		
		for(org.openhealthtools.mdht.uml.cda.LanguageCommunication lc : patient.getLanguageCommunications()){
			languages += lc.getLanguageCode().getDisplayName()+" ";
		}
		
		
		if(recordTarget.getPatientRole().getProviderOrganization()!=null)
			provider_org = recordTarget.getPatientRole().getProviderOrganization().getNames().get(0).getText();
		
		for(AD ad: recordTarget.getPatientRole().getAddrs()){
			
			if (ad.getStreetAddressLines().size()>0)
				street = ad.getStreetAddressLines().get(0).getText();
			
			if (ad.getCities().size()>0)
				city = ad.getCities().get(0).getText();
			
			if(ad.getStates().size()>0)
				state = ad.getStates().get(0).getText();
			
			if(ad.getCountries().size()>0)
				country = ad.getCountries().get(0).getText();
		}
		
		id = recordTarget.getPatientRole().getIds().get(0).getExtension();
		ptInfo.put("id", id);
		ptInfo.put("first", first.trim());
		ptInfo.put("last", last.trim());
		ptInfo.put("gender", gender);
		ptInfo.put("birth_time", birthTime.trim());
		ptInfo.put("marital_status", marital_status.trim());
		ptInfo.put("provider_org", provider_org.trim() );
		ptInfo.put("languages", languages);
		ptInfo.put("street", street.trim());
		ptInfo.put("city", city.trim() );
		ptInfo.put("state", state.trim());
		ptInfo.put("country", country.trim() );
		return ptInfo;
	}
}
