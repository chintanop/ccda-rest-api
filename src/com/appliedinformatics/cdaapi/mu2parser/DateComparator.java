package com.appliedinformatics.cdaapi.mu2parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

public class DateComparator  implements Comparator<HashMap<String, String>> {
	    @Override
	    public int compare(HashMap o1, HashMap o2) {
	    	
	    	HashMap st1 = (HashMap)o1.get("ts");
	    	HashMap st2 = (HashMap)o2.get("ts");
	    	
	    	String ts1 = getValidTS(st1);
	    	String ts2 = getValidTS(st2);
	    	
	    	
	    	Date d1 = getDate(ts1);
	    	Date d2 = getDate(ts2);
	    	
	    	if (d1 !=null && d2!=null){
	    		return d2.compareTo(d1);
	    	}else{
	    		return 0;
	    	}
	    }
	    
	    public String getValidTS(HashMap st){
	    	if (st.get("ts_center")!=null && st.get("ts_center")!=""){
	    		return (String)st.get("ts_center");
	    	}else if  (st.get("ts_high")!=null && st.get("ts_high")!=""){
	    		return (String)st.get("ts_high");
	    	}else {
	    		return (String)st.get("ts_low");
	    	}
	    }
	    
	    public Date getDate(String ts){
	    	try {
	    		if (ts==null){
	    			return null;
	    		}
		    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				return sdf.parse(ts);
			} catch (ParseException e) {
				e.printStackTrace();
			}
	    	return null;
	    }

}
