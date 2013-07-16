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
		
		System.out.println(cd);
		
	}
	
	
	
	/**
	 * Get Medications from the CDA
	 * @return ArrayList of parsed medications
	 */
	public ArrayList getMedications(){
		return (new MedicationParser(medicationsSection)).parse();
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
