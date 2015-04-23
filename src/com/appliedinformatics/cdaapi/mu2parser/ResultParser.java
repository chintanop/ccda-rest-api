package com.appliedinformatics.cdaapi.mu2parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.openhealthtools.mdht.uml.cda.Organizer;
import org.openhealthtools.mdht.uml.cda.ReferenceRange;
import org.openhealthtools.mdht.uml.cda.consol.ResultObservation;
import org.openhealthtools.mdht.uml.cda.consol.ResultOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.ResultsSection;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsSection;
import org.openhealthtools.mdht.uml.cda.hitsp.DiagnosticResultsSection;
import org.openhealthtools.mdht.uml.hl7.datatypes.ANY;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;

/**
 * 
 * @author Chintan Patel <chintan@trialx.com>
 * 
 * ResultParser - Parses results from a CDA into HashMap/Dictionary. It has 2 constuctors
 * each for CCD/HITSP-32 document and one for HITSP-83 document
 * 
 */

public class ResultParser {
	
	ResultsSection resultsSection;
	DiagnosticResultsSection diagnosticResultsSection;
	VitalSignsSection vitalsignSection;
	
	public ResultParser(ResultsSection resultsSection){
		this.resultsSection = resultsSection;
	}
	
	public ResultParser(DiagnosticResultsSection diagnosticResultsSection){
		this.diagnosticResultsSection = diagnosticResultsSection;
	}
	public ResultParser(VitalSignsSection vitalsignSection){
		this.vitalsignSection = vitalsignSection;
	}
	
	public ArrayList parse(){
		if(resultsSection != null){
			return parseH32(resultsSection);
		}
		if (diagnosticResultsSection!=null){
			return parseH83(diagnosticResultsSection);
		}
		return null;
	}
	
	private ArrayList parseH32(ResultsSection resultsSection){
		ArrayList resultsList = new ArrayList<HashMap>();
		
		if(resultsSection == null){
			return resultsList;
		}
		
		for(ResultOrganizer rorg: resultsSection.getResultOrganizers()){

			HashMap res_org = new HashMap();
			String organizer_name = rorg.getCode().getDisplayName();
			ArrayList res_items = new ArrayList<HashMap>();
			
			for(ResultObservation robs : rorg.getResultObservations()){
				
				HashMap res_item = new HashMap<String, String>();
				//result name
				String result_name = robs.getCode().getDisplayName();
				String code = robs.getCode().getCode();
				String code_system = robs.getCode().getCodeSystem();
				String result_value = "";
				String result_unit  = "";
				
				//result val
				ANY rval = robs.getValues().get(0);
				
				if(rval instanceof PQ){
					PQ rpq = (PQ)rval;
					result_value = ""+rpq.getValue();
					result_unit =  ""+rpq.getUnit();
				}
				
				//Get reference ranges
				String reference_range_text = "";
				String reference_range_low = "";
				String reference_range_high = "";
				
				for(ReferenceRange rr : robs.getReferenceRanges()){
					ANY rr_val = rr.getObservationRange().getValue();
					if (rr_val instanceof IVL_PQ){
						reference_range_low = ""+((IVL_PQ)rr_val).getLow().getValue();
						reference_range_high = ""+((IVL_PQ)rr_val).getHigh().getValue();
					}
				}
				
				//TimeStamp
				HashMap ts = CDAParserUtil.getTS(robs.getEffectiveTime());
				
				//Interpretation
				String interpretation = "";
				if (robs.getInterpretationCodes().size()>0){
					interpretation = ((CE)robs.getInterpretationCodes().get(0)).getDisplayName();
				}
				
				res_item.put("name", result_name);
				res_item.put("value", result_value);
				res_item.put("unit", result_unit);
				res_item.put("code", code);
				res_item.put("code_system", code_system);
				res_item.put("ref_low",reference_range_low);
				res_item.put("ref_high",reference_range_high);
				res_item.put("ts", ts);
				res_item.put("interpretation", interpretation);
				
				res_items.add(res_item);
				
			}
			res_org.put("category", organizer_name);
			res_org.put("results", res_items);
			
			resultsList.add(res_org);
		}
		
		return resultsList;
	}

	
	private ArrayList parseH83(DiagnosticResultsSection diagnosticResultsSection){
		ArrayList resultsList = new ArrayList<HashMap>();
		
		if(diagnosticResultsSection == null){
			return resultsList;
		}
		
	//	System.out.println(diagnosticResultsSection.getOrganizers());
	//	System.out.println(diagnosticResultsSection.getDiagnosticProcedures());
	
		for( Organizer org: diagnosticResultsSection.getOrganizers()){
			
			ResultOrganizer rorg = (ResultOrganizer)org;
			//System.out.println(rorg);
		
			HashMap res_org = new HashMap();
			String organizer_name = rorg.getCode().getDisplayName();
			//System.out.println("PATI ORG"+organizer_name);
			ArrayList res_items = new ArrayList<HashMap>();
			
			for(ResultObservation robs : rorg.getResultObservations()){
				
				HashMap res_item = new HashMap<String, String>();
				//result name
				String result_name = robs.getCode().getDisplayName();
				String code = robs.getCode().getCode();
				String code_system = robs.getCode().getCodeSystem();
			
				String result_value = "";
				String result_unit  = "";
				
				//result val
				ANY rval = robs.getValues().get(0);
				
				if(rval instanceof PQ){
					PQ rpq = (PQ)rval;
					result_value = rpq.getValue()+" ";
					result_unit  = rpq.getUnit();
				}
				
				//Get reference ranges
				String reference_range_text = "";
				String reference_range_low = "";
				String reference_range_high = "";
				
				for(ReferenceRange rr : robs.getReferenceRanges()){
					ANY rr_val = rr.getObservationRange().getValue();
					if (rr_val instanceof IVL_PQ){
						reference_range_low = ""+((IVL_PQ)rr_val).getLow().getValue();
						reference_range_high = ""+((IVL_PQ)rr_val).getHigh().getValue();
					}
				}
				
				//TimeStamp
				HashMap ts = CDAParserUtil.getTS(robs.getEffectiveTime());
				
				//Interpretation
				String interpretation = "";
				if (robs.getInterpretationCodes().size()>0){
					interpretation = ((CE)robs.getInterpretationCodes().get(0)).getDisplayName();
				}
				
				res_item.put("name", result_name);
				res_item.put("value", result_value);
				res_item.put("unit", result_unit);
				res_item.put("code", code);
				res_item.put("code_system", code_system);
				res_item.put("ref_low",reference_range_low);
				res_item.put("ref_high",reference_range_high);
				res_item.put("ts", ts);
				res_item.put("interpretation", interpretation);
				
				res_items.add(res_item);
				
			}
			res_org.put("category", organizer_name);
			res_org.put("results", res_items);
			
			resultsList.add(res_org);
		}
		
		return resultsList;
	}
}
