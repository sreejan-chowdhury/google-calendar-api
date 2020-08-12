package com.synacor.google.calendar.constants;

import java.util.HashMap;
import java.util.Map;

import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.parameter.PartStat;

public enum PartStatConstants {
	
	needsAction(PartStat.NEEDS_ACTION),//
	declined(PartStat.DECLINED),
	tentative(PartStat.TENTATIVE),
	accepted(PartStat.ACCEPTED);

	
	public final Parameter val;
	 
    private PartStatConstants(Parameter val) {
        this.val = val;
    }
    
    public Parameter getVal() {
    	return val;
    }
    
    public static Parameter getPartStat(String partStatName) {
        return PartStatConstants.valueOf(partStatName).getVal();
     }
}
