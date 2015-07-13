package com.appliedinformatics.cdaapi.mu2parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.consol.EpisodeObservation;
import org.openhealthtools.mdht.uml.cda.consol.ProblemConcernAct;
import org.openhealthtools.mdht.uml.cda.consol.ProblemObservation;
import org.openhealthtools.mdht.uml.cda.consol.ProblemSection;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;

/**
 * 
 * @author Chintan Patel <chintan@trialx.com>
 * 
 * ProblemParser - Parses problems/conditions from a CDA into HashMap/Dictionary
 * 
 */
public class ProblemParser {

	ProblemSection problemSection = null;
	
	public ProblemParser(ProblemSection problemSection){
			this.problemSection = problemSection;
	}
	
	public ArrayList parse(){
		ArrayList condList = new ArrayList<HashMap>();
		
		if(problemSection == null){
			return condList;
		}
		
		try{
		for(ProblemConcernAct problemAct: problemSection.getProblemConcerns()){
		
				for(Observation obs : problemAct.getObservations()){
					
					if(obs instanceof ProblemObservation){
						
						HashMap cond = new HashMap<String, String>();
						ProblemObservation pobs = (ProblemObservation)obs;
						
						String problem_name = ((CD)pobs.getValues().get(0)).getDisplayName();
						
						String problem_status = "";
						
						if(pobs.getProblemStatus() !=null && pobs.getProblemStatus().getValues().size()>0){
								
								problem_status = ((CD)pobs.getProblemStatus().getValues().get(0)).getDisplayName();
						}
						//System.out.println(((CD)pobs.getValues().get(0)).getDisplayName());
						//System.out.println("PHS:"+pobs.getProblemHealthStatus());
						//System.out.println(((CD)pobs.getProblemStatus().getValues().get(0)).getDisplayName());
						//System.out.println("PS:"+);
						
						IVL_TS pTime = pobs.getEffectiveTime();
						System.out.println(pTime);
						HashMap ts = CDAParserUtil.getTS(pTime);
						cond.put("code", pobs.getCode().getCode());
						cond.put("name", problem_name);
						cond.put("status", problem_status);
						cond.put("ts", ts);
						condList.add(cond);
					}
					if(obs instanceof EpisodeObservation){
						EpisodeObservation eobs = (EpisodeObservation)obs;
						//E.g. clinical finding
						//System.out.println("EPISODE "+eobs.getValues());
					}
				}
		
		}
		}catch(Exception ex){
			System.out.println("Problem Parsing Exception:");
			ex.printStackTrace();
		}
		Collections.sort(condList,new DateComparator());
		return condList;
		
	}
}
