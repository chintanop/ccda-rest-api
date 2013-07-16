package com.appliedinformatics.cdaapi.parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.openhealthtools.mdht.uml.cda.Act;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.Participant2;
import org.openhealthtools.mdht.uml.cda.Section;
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

	Section allergySection  = null;
			
	public AllergyParser(Section allergySection) {
		this.allergySection = allergySection;
	}
	
	public ArrayList parse(){
		
		ArrayList allergyList = new ArrayList<HashMap>();

		if(this.allergySection == null){
			return allergyList;
		}
		
		for(Act act:allergySection.getActs()){

			for(EntryRelationship entryR : act.getEntryRelationships()){

				HashMap amap = new HashMap<String, String>();

				String allergy_type = "", allergy_agent = "", allergy_reaction = "", allergy_status = "";

				Observation obs = entryR.getObservation();
				if(obs!=null){
					//System.out.println(obs.getValues());
					//System.out.println(obs.getCode());

					if (obs.getValues().size() > 0){
						allergy_type = ((CD)obs.getValues().get(0)).getDisplayName();
					}
					
					if(allergy_type == ""){
						allergy_type = obs.getCode().getDisplayName();
					}
				}

				for(Participant2 p2 : obs.getParticipants()){
					System.out.println();
					allergy_agent = ((CE)p2.getParticipantRole().getPlayingEntity().getCode()).getDisplayName();
					
					if (allergy_agent == null){ //Check if we have translations
						allergy_agent = CDAParserUtil.getTranslationDisplayName(p2.getParticipantRole().getPlayingEntity().getCode());
					}
					//System.out.println(((CS)p2.getRealmCodes().get(0)).getDisplayName());
				}

				for(EntryRelationship oer : obs.getEntryRelationships()){
					Observation oer_obs = oer.getObservation();
					if (oer.getTypeCode() == x_ActRelationshipEntryRelationship.MFST){ //
						allergy_reaction = ((CD)oer_obs.getValues().get(0)).getDisplayName();
						
						if(allergy_reaction == null){
							allergy_reaction = CDAParserUtil.getTranslationDisplayName(oer_obs.getValues().get(0));
						}
						
					}

					if (oer.getTypeCode() == x_ActRelationshipEntryRelationship.REFR){ //
						allergy_status = ((CE)oer_obs.getValues().get(0)).getDisplayName();
						if(allergy_status == null){
							allergy_status = CDAParserUtil.getTranslationDisplayName(oer_obs.getValues().get(0));
						}
					}
				}

				amap.put("type", allergy_type);
				amap.put("substance", allergy_agent);
				amap.put("reaction", allergy_reaction);
				amap.put("status", allergy_status);
				allergyList.add(amap);
			}
		}
		
		return allergyList;
	}
}
