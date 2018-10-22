package ar.uba.dc.lafhis.henos.report;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReportAutomaton extends ReportObject{
	protected String name;
	protected List<Integer> localAlphabet;
	protected List<ReportTransition> transitions;
	protected List<Integer> initialStates;
	protected List<List<Integer>> fluentValuations;
	protected List<List<Integer>> livenessValuations;
	
	public String getName() {return name;}
	public List<Integer> getLocalAlphabet(){return localAlphabet;}
	public List<ReportTransition> getTransitions(){return transitions;}
	public List<Integer> getInitialStates(){return initialStates;}
	public List<List<Integer>> getFluentValuations(){return fluentValuations;}
	public List<List<Integer>> getLivenessValuations(){return livenessValuations;}
	/*
	 * <name,ctx,local_alphabet_count,[sig_1.idx,..,sig_N.idx],trans_count,[trans_1,..,trans_M],init_count,[init_i,..,init_K],[[val_s_0_f_0,..,val_s_0_f_L],..,[val_s_T_f_0,..,val_s_T_f_L]]
	,[[val_s_0_l_0,..,val_s_0_l_R],..,[val_s_T_l_0,..,val_s_T_l_R]]>
	 */
	public ReportAutomaton(FileInputStream fis) {
		localAlphabet	= new ArrayList<Integer>();
		transitions		= new ArrayList<ReportTransition>();
		initialStates	= new ArrayList<Integer>();
		fluentValuations	= new ArrayList<List<Integer>>();
		livenessValuations	= new ArrayList<List<Integer>>();
	    try {
			char current;
			current = (char) fis.read();
			if(current != ReportConstants.AUT_SER_OBJ_START) {
				isOK = false;
				return;
			}
			name			= readString(fis, ReportConstants.AUT_SER_SEP);
			/*
			alphabet		= new ReportAlphabet(fis);
			current = (char) fis.read();
			if(current != ReportConstants.AUT_SER_SEP) {
				isOK = false;
				return;
			}
			int count	= readInt(fis, ReportConstants.AUT_SER_SEP);
			current 	= (char) fis.read();
			if(current != ReportConstants.AUT_SER_ARRAY_START) {
				isOK	= false;
				return;
			}			
			if(count == 0) {
				current = (char) fis.read();
				if(current != ReportConstants.AUT_SER_ARRAY_END) {
					isOK	= false;
					return;
				}		
			}else {
				fluents	= readStringArray(fis, ReportConstants.AUT_SER_OBJ_END);
			}
			count		= readInt(fis, ReportConstants.AUT_SER_SEP);
			if(count == 0) {
				current 	= (char) fis.read();
				if(current != ReportConstants.AUT_SER_ARRAY_START) {
					isOK	= false;
					return;
				}			
				current = (char) fis.read();
				if(current != ReportConstants.AUT_SER_ARRAY_END) {
					isOK	= false;
					return;
				}
			}else {
				livenessNames	= readStringArray(fis, ReportConstants.AUT_SER_OBJ_END);
			}		
			*/	
	      } catch (IOException e) {
	    	  isOK = false;
	    	  e.printStackTrace();
	      }		
	}
}
