package com.appliedinformatics.cdaapi.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.emf.common.util.Diagnostic;
import org.openhealthtools.mdht.uml.cda.Act;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.Consumable;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.Material;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.Organizer;
import org.openhealthtools.mdht.uml.cda.Participant2;
import org.openhealthtools.mdht.uml.cda.Precondition;
import org.openhealthtools.mdht.uml.cda.ReferenceRange;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.SubstanceAdministration;
import org.openhealthtools.mdht.uml.cda.ccd.CCDFactory;
import org.openhealthtools.mdht.uml.cda.hitsp.DiagnosticResultsSection;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.ccd.EpisodeObservation;
import org.openhealthtools.mdht.uml.cda.ccd.MedicationsSection;
import org.openhealthtools.mdht.uml.cda.ccd.ProblemAct;
import org.openhealthtools.mdht.uml.cda.ccd.ProblemObservation;
import org.openhealthtools.mdht.uml.cda.ccd.ProblemSection;
import org.openhealthtools.mdht.uml.cda.ccd.ResultObservation;
import org.openhealthtools.mdht.uml.cda.ccd.ResultOrganizer;
import org.openhealthtools.mdht.uml.cda.ccd.ResultsSection;
import org.openhealthtools.mdht.uml.cda.hitsp.HITSPPackage;
import org.openhealthtools.mdht.uml.cda.hitsp.PatientSummary;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.cda.util.ValidationResult;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.ANY;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.ENXP;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PIVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.SXCM_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.impl.ENImpl;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;

/**
 * 
 * @author Chintan Patel <chintan@trialx.com>
 * 
 * CDAParser - The core CDAParser that parses an input stream to parse a HITSP-32 or HITSP-83 document
 *
 */
public class CDAParser {

	//Initialize the document variables
	ClinicalDocument cd 					= null;
	MedicationsSection medicationsSection 	= null;
	ProblemSection  problemSection 			= null;
	Section allergySection 					= null;
	ResultsSection resultsSection 			= null;
	DiagnosticResultsSection diagnosticResultsSection = null;
	
	PatientSummary ps 						= null; //for HITSP 83
	ContinuityOfCareDocument ccd 			= null; //for HITSP 32
	
	/**
	 * Constructor for the CDA Parser. Accepts an InputStream of a CDA document in either
	 * HITSP 32 (CCD) or HITSP 83 (CCDA) document
	 * @param cda_input
	 */
	public CDAParser(InputStream cda_input){
		HITSPPackage.eINSTANCE.eClass();
		ContinuityOfCareDocument doc = CCDFactory.eINSTANCE.createContinuityOfCareDocument().init();

		ValidationResult result = new ValidationResult();
		
		try {
			cd = CDAUtil.load(cda_input, result); //Load CDA document and validate
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Output diagnostic error messages
		//TODO: return any errors to the client
		for (Diagnostic diagnostic : result.getErrorDiagnostics()) {
			System.out.println("ERROR: " + diagnostic.getMessage());
		}
		for (Diagnostic diagnostic : result.getWarningDiagnostics()) {
			System.out.println("WARNING: " + diagnostic.getMessage());
		}
		
		String allergy_template_id = "2.16.840.1.113883.10.20.1.2";
		
		if (cd instanceof ContinuityOfCareDocument){
			ContinuityOfCareDocument ccd = (ContinuityOfCareDocument) cd;
			System.out.println("CCD");
			medicationsSection 			= ccd.getMedicationsSection();
			problemSection				= ccd.getProblemSection();
			allergySection 				= CDAParserUtil.getSection(cd, allergy_template_id);	
			resultsSection 				= ccd.getResultsSection();
		}
		
		if (cd instanceof PatientSummary){
			PatientSummary ps 	= (PatientSummary)cd;
			medicationsSection 	= ps.getMedicationsSection();
			problemSection 		= ps.getProblemSection();
			System.out.println("Patient Summary");
			allergySection 			= ps.getAllergiesReactionsSection();
			diagnosticResultsSection = ps.getDiagnosticResultsSection();
		}
	}
	
	/*
	public  String getTranslationDisplayName(ANY dt){
		if(dt instanceof CD){
			CD cdt = (CD)dt;
			if(cdt.getTranslations().size()>0){
				return cdt.getTranslations().get(0).getDisplayName();
			}
		}
		
		if(dt instanceof CE){
			CE cdt = (CE)dt;
			if(cdt.getTranslations().size()>0){
				return cdt.getTranslations().get(0).getDisplayName();
			}
		}
		return null;
	}
	
	public  HashMap getTS(IVL_TS pTime){
		HashMap <String, String>ts = new HashMap();
		ts.put("ts_low", "");
		ts.put("ts_high", "");
		ts.put("ts_center", "");
		if (pTime!=null){
			if (pTime.getLow()!=null){
				//System.out.println("LOW:"+pTime.getLow().getValue());
				ts.put("ts_low",pTime.getLow().getValue());
			}
			if (pTime.getHigh()!=null){
				//System.out.println("HIGH:"+pTime.getHigh().getValue());
				ts.put("ts_high",pTime.getHigh().getValue());
			}
			if (pTime.getCenter()!=null){
				//System.out.println("CENTER:"+pTime.getCenter().getValue());
				ts.put("ts_center",pTime.getCenter().getValue());
			}
		}
		return ts;
	}
	
	public Section getSection(ClinicalDocument cd, String template_id){
		if(cd.hasSectionTemplate(template_id)){
			for(Section sec : cd.getSections()){
				if(sec.hasTemplateId(template_id)){		
					return sec;
				}
			}
		}
		return null;
}

	public ArrayList parseProblems(ProblemSection problemSection){
			
			ArrayList condList = new ArrayList<HashMap>();
			
			if(problemSection == null){
				return condList;
			}
			for(ProblemAct problemAct: problemSection.getProblemActs()){
			
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
							HashMap ts = getTS(pTime);
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
			
			return condList;
			
		}
		
	public ArrayList parseMedications(MedicationsSection medSection){
			
			ArrayList medList = new ArrayList<HashMap>();
			
			if(medSection == null){
				return medList;
			}
			
			int indx = 0;
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
				String period_unit = "";
				HashMap ts = null;
				
				for(SXCM_TS effectiveTime :sa.getEffectiveTimes()){
					System.out.println(effectiveTime);
					if (effectiveTime instanceof PIVL_TS){
						PIVL_TS periodTS = (PIVL_TS)effectiveTime;
						period_unit += ""+periodTS.getPeriod().getValue()+" "+periodTS.getPeriod().getUnit();
					}
					if (effectiveTime instanceof IVL_TS){
						IVL_TS pTime = (IVL_TS)effectiveTime;
						ts = getTS(pTime);
					}
				}	
				
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
				
				//administration unit
				String administration_unit = "";
				CE aunit = sa.getAdministrationUnitCode();
				if(aunit!=null){
					administration_unit = aunit.getDisplayName();
				}
				
				//status
				String status_code = sa.getStatusCode().getCode();
				
				
				//related conditions
				ArrayList related_conditions = new ArrayList<HashMap>();
				for(Precondition prec : sa.getPreconditions()){
					//System.out.println();
					
					HashMap rcond = new HashMap();
					rcond.put("name", ((CE)prec.getCriterion().getValue()).getDisplayName());				
					related_conditions.add(rcond);
				}
				
				for(Observation os : sa.getObservations()){
					
					if(os instanceof ProblemObservation){
						ProblemObservation pobs = (ProblemObservation)os;
						String problem_name = ((CD)pobs.getValues().get(0)).getDisplayName();
						//String problem_status = ((CD)pobs.getProblemStatus().getValues().get(0)).getDisplayName();
						HashMap rcond = new HashMap();
						rcond.put("name", problem_name);	
						HashMap ots = getTS(pobs.getEffectiveTime());
						rcond.put("ts", ots);
						//rcond.put("status", problem_status);
						related_conditions.add(rcond);
					}
				}
				
				med.put("name", med_name);
				med.put("dose", dosage);
				med.put("route", route);
				med.put("status", status_code);
				med.put("administration_unit",administration_unit); //e.g puff, tablet etc
				med.put("period_unit", period_unit);
				med.put("ts",ts);
				med.put("related_conditions", related_conditions);
				medList.add(med);
				
				indx = indx+1;
				if(indx>10){
					break;
				}
			}	
			return medList;
		}
		
	public ArrayList parseAllergies(Section allergySection){

			ArrayList allergyList = new ArrayList<HashMap>();

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
							allergy_agent = getTranslationDisplayName(p2.getParticipantRole().getPlayingEntity().getCode());
						}
						//System.out.println(((CS)p2.getRealmCodes().get(0)).getDisplayName());
					}

					for(EntryRelationship oer : obs.getEntryRelationships()){
						Observation oer_obs = oer.getObservation();
						if (oer.getTypeCode() == x_ActRelationshipEntryRelationship.MFST){ //
							allergy_reaction = ((CD)oer_obs.getValues().get(0)).getDisplayName();
							
							if(allergy_reaction == null){
								allergy_reaction = getTranslationDisplayName(oer_obs.getValues().get(0));
							}
							
						}

						if (oer.getTypeCode() == x_ActRelationshipEntryRelationship.REFR){ //
							allergy_status = ((CE)oer_obs.getValues().get(0)).getDisplayName();
							if(allergy_status == null){
								allergy_status = getTranslationDisplayName(oer_obs.getValues().get(0));
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
		
	public ArrayList parseResults(ResultsSection resultsSection){
			
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
					HashMap ts = getTS(robs.getEffectiveTime());
					
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
		
	public ArrayList parseResults(org.openhealthtools.mdht.uml.cda.hitsp.DiagnosticResultsSection diagnosticResultsSection){
			
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
				System.out.println("PATI ORG"+organizer_name);
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
					HashMap ts = getTS(robs.getEffectiveTime());
					
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

	public HashMap parseDemographics(ClinicalDocument cd){
		
		org.openhealthtools.mdht.uml.cda.RecordTarget recordTarget = cd.getRecordTargets().get(0);
		
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
	*/
	
	/**
	 * Get Medications from the CDA
	 * @return ArrayList of parsed medications
	 */
	public ArrayList getMedications(){
		return (new ProblemParser(problemSection)).parse();
	}
	
	/**
	 * Get Allergies from the CDA
	 * @return ArrayList of parsed allergies
	 */
	public ArrayList getAllergies(){
		return (new AllergyParser(allergySection)).parse();
	}
	
	/**
	 * Get results section from the CDA
	 * @return ArrayList of parsed results
	 */
	public ArrayList getResults(){
		//Decide between HITSP 32 versus 83
		if (ps!=null){
			return (new ResultParser(diagnosticResultsSection)).parse();
		}else{
			return (new ResultParser(resultsSection)).parse();
		}
	}

	/**
	 * Get problems from the CDA
	 * @return ArrayList of parsed problems
	 */
	public ArrayList getProblems(){
		return (new ProblemParser(problemSection)).parse();
	}

	/**
	 * Get demographics from the CDA
	 * @return HashMap/Dictionary of Demographics
	 */
	public HashMap getDemographics(){
		return (new DemographicParser(cd)).parse();
	}
}
