package ar.uba.dc.lafhis.henos.report;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.List;

public class ReportAutomaton extends ReportObject{
	public static int SOURCE_GAME	= 0x1;
	public static int SOURCE_LTL	= 0x2;
	public static int SOURCE_STRAT	= 0x4;
	public static int SOURCE_DIAG	= 0x8;
	
	protected String name;
	protected ReportContext context;
	protected List<Integer> localAlphabet;
	protected List<ReportTransition> transitions;
	protected List<Integer> initialStates;
	protected List<List<Boolean>> fluentValuations;
	protected List<List<Boolean>> livenessValuations;
	protected List<Boolean> reportedVStates;
	protected List<List<Boolean>> vStates;
	protected boolean isGame;
	protected boolean isLtl;
	protected boolean isStrat;
	protected boolean isDiag;
	
	public String getName() {return name;}
	public ReportContext getContext() {return context;}
	public List<Integer> getLocalAlphabet(){return localAlphabet;}
	public List<ReportTransition> getTransitions(){return transitions;}
	public List<Integer> getInitialStates(){return initialStates;}
	public List<List<Boolean>> getFluentValuations(){return fluentValuations;}
	public List<List<Boolean>> getLivenessValuations(){return livenessValuations;}
	public List<Boolean> getReportedVStates(){return reportedVStates;}
	public List<List<Boolean>> getVStates(){return vStates;}
	public boolean getIsGame() {return isGame; }
	public boolean getIsLTL() {return isLtl; }
	public boolean getIsStrategy() { return isStrat; }
	public boolean getIsDiagnosis() { return isDiag; }
	/*
	 * <name,ctx,local_alphabet_count,[sig_1.idx,..,sig_N.idx],vstates_monitored_count,[vstates_mon1,..,vstates_monN]
	,trans_count,[trans_1,..,trans_M],init_count,[init_i,..,init_K],[[val_s_0_f_0,..,val_s_0_f_L],..,[val_s_T_f_0,..,val_s_T_f_L]]
	,[[val_s_0_l_0,..,val_s_0_l_R],..,[val_s_T_l_0,..,val_s_T_l_R]]
	,[[vstate_s_0_f_0,..,vstate_s_0_f_V],..,[vstate_s_T_f_0,..,vstate_s_T_f_R]]>
	 */
	public ReportAutomaton(PushbackInputStream fis) {
		localAlphabet	= new ArrayList<Integer>();
		transitions		= new ArrayList<ReportTransition>();
		initialStates	= new ArrayList<Integer>();
		fluentValuations	= new ArrayList<List<Boolean>>();
		livenessValuations	= new ArrayList<List<Boolean>>();
		reportedVStates	= new ArrayList<Boolean>();
		vStates			= new ArrayList<List<Boolean>>();
	    try {
			char current;
			current = (char) fis.read();
			if(current != ReportConstants.AUT_SER_OBJ_START) {
				isOK = false;
				return;
			}
			name			= readString(fis, ReportConstants.AUT_SER_SEP);
			int source		= readInt(fis, ReportConstants.AUT_SER_SEP);
			isGame			= (source & ReportAutomaton.SOURCE_GAME) == ReportAutomaton.SOURCE_GAME;
			isLtl			= (source & ReportAutomaton.SOURCE_LTL) == ReportAutomaton.SOURCE_LTL;
			isStrat			= (source & ReportAutomaton.SOURCE_STRAT) == ReportAutomaton.SOURCE_STRAT;
			isDiag			= (source & ReportAutomaton.SOURCE_DIAG) == ReportAutomaton.SOURCE_DIAG;
			context			= new ReportContext(fis);
			current = (char) fis.read();
			if(current != ReportConstants.AUT_SER_SEP) {
				isOK = false;
				return;
			}
			int count	= readInt(fis, ReportConstants.AUT_SER_SEP);
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
				current = (char) fis.read();
				if(current != ReportConstants.AUT_SER_SEP) {
					isOK	= false;
					return;
				}								
			}else {
				localAlphabet	= readIntArray(fis, ReportConstants.AUT_SER_ARRAY_END);
			}
			count	= readInt(fis, ReportConstants.AUT_SER_SEP);
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
				current = (char) fis.read();
				if(current != ReportConstants.AUT_SER_SEP) {
					isOK	= false;
					return;
				}				
			}else {
				reportedVStates	= readBooleanArray(fis, ReportConstants.AUT_SER_ARRAY_END);
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
				current 	= (char) fis.read();
				if(current != ReportConstants.AUT_SER_ARRAY_START) {
					isOK	= false;
					return;
				}	
				do {
					transitions.add(new ReportTransition(fis));
					current = (char) fis.read();
					if(current != ReportConstants.AUT_SER_ARRAY_END && current
							!= ReportConstants.AUT_SER_SEP) {
						isOK	= false;
						return;
					}
						
				}while(current != ReportConstants.AUT_SER_ARRAY_END);
			}
			current = (char) fis.read();
			if(current != ReportConstants.AUT_SER_SEP) {
				isOK = false;
				return;
			}	
			count	= readInt(fis, ReportConstants.AUT_SER_SEP);
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
				initialStates	= readIntArray(fis, ReportConstants.AUT_SER_ARRAY_END);
			}	
			fluentValuations	= readBooleanArrayArray(fis, ReportConstants.AUT_SER_SEP);
			livenessValuations	= readBooleanArrayArray(fis, ReportConstants.AUT_SER_OBJ_END);
			vStates	= readBooleanArrayArray(fis, ReportConstants.AUT_SER_OBJ_END);			
	      } catch (IOException e) {
	    	  isOK = false;
	    	  e.printStackTrace();
	      }		
	}
	
	public String getLabel(int labelLocalIndex) {
		if(getContext().getAlphabet().getSignals().size() <= labelLocalIndex)
			return "__tau__";
		return getContext().getAlphabet().getSignals().get(labelLocalIndex).getName();
	}

	
	public String getAutomatonInfo() {
		String retValue		= "<html>Name:<b>[" + name + "]</b></br>Local Alphabet:[";
		boolean firstValue	= true;
		for(int i : getLocalAlphabet()) {
			if(firstValue)firstValue = false;
			else	retValue 	+= ",";
			retValue		+= getLabel(i);
		}
		retValue			+= "]</br>Fluents:[";
		firstValue	= true;
		for(String s : getContext().getfluents()) {
			if(firstValue)firstValue = false;
			else	retValue 	+= ",";
			retValue		+= s;
		}
		for(String s : getContext().getLivenessNames()) {
			if(firstValue)firstValue = false;
			else	retValue 	+= ",";
			retValue		+= s;
		}
		for(String s : getContext().getVStatesNames()) {
			if(firstValue)firstValue = false;
			else	retValue 	+= ",";
			retValue		+= s;
		}		
		retValue			+= "]</html>";
		return retValue;
	}
}

