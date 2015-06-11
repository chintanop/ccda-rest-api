package com.appliedinformatics.cdaapi.mu2parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.openhealthtools.mdht.uml.cda.Procedure;
import org.openhealthtools.mdht.uml.cda.consol.ProcedureActivityProcedure;
import org.openhealthtools.mdht.uml.cda.consol.ProceduresSection;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;

public class ProcedureParser {
		/** Parse procedures **/
		
		ProceduresSection proceduresSection = null;

		public ProcedureParser(ProceduresSection proceduresSection){
			this.proceduresSection = proceduresSection;
		}
		
		public ArrayList parse(){
			ArrayList proc_parsed = new ArrayList<HashMap<String, String>>();
			//ArrayList proc_parsed = new ArrayList<String>();

			for (Procedure proc: this.proceduresSection.getProcedures()){
				HashMap proc_map = new HashMap<String, String>();
				proc_map.put("code",proc.getCode().getCode());
				proc_map.put("name", proc.getCode().getDisplayName());
				proc_map.put("ts",CDAParserUtil.getTS(proc.getEffectiveTime()));
				proc_map.put("status",proc.getStatusCode().getCode());
				proc_parsed.add(proc_map);
			}
			
			
			return proc_parsed;
		}

}
