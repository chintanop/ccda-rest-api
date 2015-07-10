package com.appliedinformatics.cdaapi.parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.openhealthtools.mdht.uml.cda.Consumable;
import org.openhealthtools.mdht.uml.cda.ManufacturedProduct;
import org.openhealthtools.mdht.uml.cda.Material;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.Precondition;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.SubstanceAdministration;
import org.openhealthtools.mdht.uml.cda.ccd.MedicationsSection;
import org.openhealthtools.mdht.uml.cda.ccd.ProblemObservation;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PIVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.SXCM_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.impl.ENImpl;

/**
 * 
 * @author Chintan Patel <chintan@trialx.com>
 * 
 * MedicationParser - Parses medications from a CDA into HashMap/Dictionary
 * 
 */
public class MedicationParser {

	MedicationsSection medSection = null;
	
	public MedicationParser(MedicationsSection medSection) {
		this.medSection = medSection;
	}
	
	public ArrayList parse(){
		ArrayList medList = new ArrayList<HashMap>();
		
		if(this.medSection == null){
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
			
			if (med_name == null){
				 med_name = CDAParserUtil.getTranslationDisplayName(sa.getCode());
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
					ts = CDAParserUtil.getTS(pTime);
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
					HashMap ots = CDAParserUtil.getTS(pobs.getEffectiveTime());
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
			
			//indx = indx+1;
			
		}	
		return medList;
	}
	
	
}
