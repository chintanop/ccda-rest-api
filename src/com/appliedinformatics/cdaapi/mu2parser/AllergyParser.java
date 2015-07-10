package com.appliedinformatics.cdaapi.mu2parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.openhealthtools.mdht.uml.cda.Act;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.Participant2;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.consol.AllergiesSection;
import org.openhealthtools.mdht.uml.cda.consol.AllergyObservation;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;
import org.openhealthtools.mdht.uml.hl7.datatypes.ANY;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;

import com.appliedinformatics.cdaapi.parser.CDAParserUtil;

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
		
		ArrayList allergyList = new ArrayList<HashMap<String, String>>();
		
		if(this.allergySection == null){
			return allergyList;
		}

		try{
				for (AllergyProblemAct apa: this.allergySection.getAllergyProblemActs()){
					HashMap amap = new HashMap<String, String>();
		
			
					for(AllergyObservation aobs: apa.getAllergyObservations()){
						String allergy_status = ((CD)aobs.getAllergyStatusObservation().getValues().get(0)).getDisplayName();
						
					
						String allergy_type = ((CD)aobs.getValues().get(0)).getDisplayName();
						
						String allergy_reaction = ""; 
						String allergy_severity = "";
						String allergy_agent = "";

						
						if (aobs.getSeverity()!=null && aobs.getSeverity().getValues().size()>0){
							allergy_severity = ((CD)aobs.getSeverity().getValues().get(0)).getDisplayName();
						
						}
						
						for(Participant2 p2 : aobs.getParticipants()){
							allergy_agent = ((CE)p2.getParticipantRole().getPlayingEntity().getCode()).getDisplayName();
							if (allergy_agent == null){ //Check if we have translations
								allergy_agent = CDAParserUtil.getTranslationDisplayName(p2.getParticipantRole().getPlayingEntity().getCode());
							}
						}
						
						
						for(EntryRelationship oer : aobs.getEntryRelationships()){
							Observation oer_obs = oer.getObservation();
							if (oer.getTypeCode() == x_ActRelationshipEntryRelationship.MFST){ //
								allergy_reaction = ((CD)oer_obs.getValues().get(0)).getDisplayName();
								
								if(allergy_reaction == null){
									allergy_reaction = CDAParserUtil.getTranslationDisplayName(oer_obs.getValues().get(0));
								}
								
							}
							
							if (oer.getTypeCode() == x_ActRelationshipEntryRelationship.SUBJ){ // look in to subj
								System.out.println("SCODE: "+oer_obs.getCode().getCode());

								
								if(oer_obs.getCode().getCode().equals("SEV")){ // severity

									allergy_severity = ((CD)oer_obs.getValues().get(0)).getDisplayName();
									if(allergy_severity == null){
										allergy_severity = CDAParserUtil.getTranslationDisplayName(oer_obs.getValues().get(0));
									}
								}
							}
							
						}
						
						//System.out.println("Agent: "+allergy_agent);
						//System.out.println("Reaction: "+allergy_reaction);
						//System.out.println("Severity: "+allergy_severity);
						
					
						amap.put("reaction", allergy_reaction);
						amap.put("status", allergy_status);
						amap.put("ts",CDAParserUtil.getTS(aobs.getEffectiveTime()));
						amap.put("type", allergy_type); 
						amap.put("substance", allergy_agent);
						amap.put("severity", allergy_severity);
						allergyList.add(amap);
					}
				}
			
				Collections.sort(allergyList, new DateComparator());
				
		}catch(Exception e){
			System.out.println("Error Allergy Parsing:");
			e.printStackTrace();
		}
		return allergyList;
	}
}
