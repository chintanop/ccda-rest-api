package com.appliedinformatics.cdaapi.mu2parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.openhealthtools.mdht.uml.cda.Act;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.Participant2;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.consol.AllergiesSection;
import org.openhealthtools.mdht.uml.cda.consol.AllergyObservation;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;

/**
 * 
 * @author Chintan Patel <chintan@trialx.com>
 *
 * AllergyParser: Parses the CDA section on Allergy into JSON objects
 *
 */
public class AllergyParser {

	AllergiesSection allergySection  = null;
			
	public AllergyParser(AllergiesSection allergySection) {
		this.allergySection = allergySection;
	}
	
	public ArrayList parse(){
		
		ArrayList allergyList = new ArrayList<HashMap>();
		
		if(this.allergySection == null){
			return allergyList;
		}

		for (AllergyProblemAct apa: this.allergySection.getAllergyProblemActs()){
			HashMap amap = new HashMap<String, String>();

	
			for(AllergyObservation aobs: apa.getAllergyObservations()){
				String allergy_status = ((CD)aobs.getAllergyStatusObservation().getValues().get(0)).getDisplayName();
				
			
				String allergy_reaction = ((CD)aobs.getValues().get(0)).getDisplayName();
				
				String allergy_severity = "";
				
				if (aobs.getSeverity()!=null && aobs.getSeverity().getValues().size()>0){
					allergy_severity = ((CD)aobs.getSeverity().getValues().get(0)).getDisplayName();
				}
				
				String allergy_agent= null;
				for(Participant2 p2 : aobs.getParticipants()){
					allergy_agent = ((CE)p2.getParticipantRole().getPlayingEntity().getCode()).getDisplayName();
					if (allergy_agent == null){ //Check if we have translations
						allergy_agent = CDAParserUtil.getTranslationDisplayName(p2.getParticipantRole().getPlayingEntity().getCode());
					}
				}
				
				amap.put("reaction", allergy_reaction);
				amap.put("status", allergy_status);
				amap.put("ts",CDAParserUtil.getTS(aobs.getEffectiveTime()));
				amap.put("type", ""); 
				amap.put("substance", allergy_agent);
				amap.put("severity", allergy_severity);
				allergyList.add(amap);
			}
		}
		
		
		return allergyList;
	}
}
