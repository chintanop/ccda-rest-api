package com.appliedinformatics.cdaapi.mu2parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.openhealthtools.mdht.uml.cda.Act;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.Component4;
import org.openhealthtools.mdht.uml.cda.Component5;
import org.openhealthtools.mdht.uml.cda.Consumable;
import org.openhealthtools.mdht.uml.cda.Entry;
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
import org.openhealthtools.mdht.uml.cda.consol.AllergiesSection;
import org.openhealthtools.mdht.uml.cda.consol.ProceduresSection;
import org.openhealthtools.mdht.uml.cda.consol.MedicationsSection;
import org.openhealthtools.mdht.uml.cda.consol.MedicationActivity;
import org.openhealthtools.mdht.uml.cda.consol.ProblemObservation;
import org.openhealthtools.mdht.uml.cda.consol.ProblemSection;
import org.openhealthtools.mdht.uml.cda.consol.ResultObservation;
import org.openhealthtools.mdht.uml.cda.consol.ResultOrganizer;
import org.openhealthtools.mdht.uml.cda.consol.ResultsSection;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsSectionEntriesOptional;
import org.openhealthtools.mdht.uml.cda.hitsp.HITSPPackage;
import org.openhealthtools.mdht.uml.cda.hitsp.PatientSummary;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.cda.util.ValidationResult;
import org.openhealthtools.mdht.uml.hl7.datatypes.AD;
import org.openhealthtools.mdht.uml.hl7.datatypes.ANY;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.ENXP;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PIVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.SXCM_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.impl.ENImpl;
import org.openhealthtools.mdht.uml.hl7.vocab.NullFlavor;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;
import org.openhealthtools.mdht.uml.cda.mu2consol.Mu2consolPackage;
import org.openhealthtools.mdht.uml.cda.mu2consol.Mu2consolFactory;
import org.openhealthtools.mdht.uml.cda.mu2consol.ClinicalOfficeVisitSummary;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
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
	AllergiesSection allergySection 					= null;
	ResultsSection resultsSection 			= null;
	DiagnosticResultsSection diagnosticResultsSection = null;
	ProceduresSection proceduresSection		= null;
	PatientSummary ps 						= null; //for HITSP 83
	ContinuityOfCareDocument ccd 			= null; //for HITSP 32
	Section VitalSection = null;
	public ClinicalOfficeVisitSummary covs			= null; //for Mu2 C-CDA
	
	/**
	 * Constructor for the CDA Parser. Accepts an InputStream of a CDA document in either
	 * HITSP 32 (CCD) or HITSP 83 (CCDA) document
	 * @param cda_input
	 */
	public CDAParser(InputStream cda_input){
//		HITSPPackage.eINSTANCE.eClass();
//		ContinuityOfCareDocument doc = CCDFactory.eINSTANCE.createContinuityOfCareDocument().init();

		Mu2consolPackage.eINSTANCE.eClass();
		
		ValidationResult result = new ValidationResult();
		
		try {
			//cd = CDAUtil.load(cda_input, result); //Load CDA document and validate
			
			cd = CDAUtil.loadAs(
					cda_input,  
					Mu2consolPackage.eINSTANCE.getClinicalOfficeVisitSummary(),
					result);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Aamir");
		System.out.println("Aamir");
		//Output diagnostic error messages
		//TODO: return any errors to the client
		for (Diagnostic diagnostic : result.getErrorDiagnostics()) {
			System.out.println("ERROR: " + diagnostic.getMessage());
		}
		for (Diagnostic diagnostic : result.getWarningDiagnostics()) {
			System.out.println("WARNING: " + diagnostic.getMessage());
		}
		
		 covs = (ClinicalOfficeVisitSummary)cd;
		System.out.println("NOT A CCDA or PATIENT SUMMARY!");
		System.out.println(cd);
		System.out.println(covs);
		for(Section sec: covs.getAllSections()){
			String r = sec.getClass().getName();
			if (r == "org.openhealthtools.mdht.uml.cda.consol.impl.VitalSignsSectionEntriesOptionalImpl"){
				VitalSection = sec ;		
			}
		}
		
		
		medicationsSection 	= covs.getMedicationsSection();
		problemSection = covs.getProblemSection();
		allergySection = covs.getAllergiesSection();
		//resultsSection = covs.getResultsSection();
		proceduresSection = covs.getProceduresSection();
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

	public ArrayList getVitals(){
		return (new VitalSignParser(VitalSection)).parse();
	}
	
	public ArrayList getProcedures(){
		return (new ProcedureParser(proceduresSection)).parse();
	}
	
	/**
	 * Get demographics from the CDA
	 * @return HashMap/Dictionary of Demographics
	 */
	public HashMap getDemographics(){
		return (new DemographicParser(cd)).parse();
	}
	
	public static void main(String[] args) {
		InputStream is;
		try {
			is = new FileInputStream(new File("ccd_samples/CCDATest16.xml"));
			CDAParser cdaParser = new CDAParser(is);
			System.out.println("\n\n******* MEDICATIONS ************\n\n");
			System.out.println(cdaParser.getMedications());
			
			System.out.println(("\n\n****** RESULTS ***************\n\n"));
			System.out.println(cdaParser.getResults());
			
			System.out.println(("\n\n****** ALLERGIES ***************\n\n"));
			System.out.println(cdaParser.getAllergies());
			
			System.out.println(("\n\n****** PROBLEMS ***************\n\n"));
			System.out.println(cdaParser.getProblems());
			
			System.out.println(("\n\n****** DEMOGRAPHICS ***************\n\n"));
			System.out.println(cdaParser.getDemographics());

			System.out.println(("\n\n****** VITALS ***************\n\n"));
			System.out.println(cdaParser.getVitals());		
			
			System.out.println(("\n\n****** PROCEDURES ***************\n\n"));
			System.out.println(cdaParser.getProcedures());		
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
