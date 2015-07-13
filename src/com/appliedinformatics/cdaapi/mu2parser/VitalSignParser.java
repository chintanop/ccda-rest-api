package com.appliedinformatics.cdaapi.mu2parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.Component4;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.Organizer;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.consol.ResultOrganizer;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;

public class VitalSignParser {
	Section vitalSection  = null;
	
	public VitalSignParser(Section vitalSection) {
		this.vitalSection = vitalSection;
	}
	
	
	public ArrayList parse(){
			ArrayList vitalList = new ArrayList<HashMap>();
			
			try{
			for( Entry e:vitalSection.getEntries()){
				Organizer k = e.getOrganizer();
				HashMap vmap = new HashMap<String, String>();
				for (Component4 x : k.getComponents()){					
					Observation obs = x.getObservation();				
					CD co = obs.getCode();
					String vital_name = co.getDisplayName();
					IVL_TS pTime = obs.getEffectiveTime();
					String tr = pTime.getValue();
					HashMap ts = CDAParserUtil.getTS(pTime);
					vmap.put("date", tr);
//					System.out.println(ts);
					System.out.println(pTime);
					if (vital_name!=null && obs.getValues().size() > 0){
						PQ kl = (PQ) obs.getValues().get(0);
						String value = kl.getValue() + ";" +kl.getUnit();
						vmap.put(vital_name, value);

					}

				}
				if (vmap.size()>0){
					vitalList.add(vmap);
				}
			 }
			}catch(Exception ex){
				System.out.println("Vitals Parsing Error");
				ex.printStackTrace();
			}
			System.out.println(vitalList);
//			Collections.sort(vitalList,new DateComparator());
		return vitalList;
		
	}

}
