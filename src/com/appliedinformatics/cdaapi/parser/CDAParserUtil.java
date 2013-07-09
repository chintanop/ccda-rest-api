package com.appliedinformatics.cdaapi.parser;

import java.util.HashMap;

import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.hl7.datatypes.ANY;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;

/**
 * 
 * @author Chintan Patel <chintan@trialx.com>
 * 
 * Parsing utility Functions
 *
 */
public class CDAParserUtil {

	/**
	 * Takes a IVL_TS time object and returns a simple low,high and center values
	 * @param pTime
	 * @return A dictionary/HashMap of low, high and center values of time stamp
	 */
	public static HashMap getTS(IVL_TS pTime){
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
	
	/**
	 * Takes a ANY object and checks if its a CD object or CE object and 
	 * returns a corresponding string of displayName
	 * 
	 * TODO: Connect this to a terminlogy lookup service if no display name given by code given
	 * 
	 * @param dt
	 * @return String of the display name
	 */
	public static String getTranslationDisplayName(ANY dt){
		
		//check if its CD instance
		if(dt instanceof CD){
			CD cdt = (CD)dt;
			if(cdt.getTranslations().size()>0){
				return cdt.getTranslations().get(0).getDisplayName();
			}
		}
		
		//check if its CE instance
		if(dt instanceof CE){
			CE cdt = (CE)dt;
			if(cdt.getTranslations().size()>0){
				return cdt.getTranslations().get(0).getDisplayName();
			}
		}
		return null;
	}
	
	/**
	 * Gets a section with given template_id from a ClinicalDocument
	 * @param cd
	 * @param template_id
	 * @return Section 
	 */
	public static Section getSection(ClinicalDocument cd, String template_id){
		if(cd.hasSectionTemplate(template_id)){
			for(Section sec : cd.getSections()){
				if(sec.hasTemplateId(template_id)){		
					return sec;
				}
			}
		}
		return null;
	}
	
	
}
