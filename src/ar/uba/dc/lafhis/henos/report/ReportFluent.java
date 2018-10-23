package ar.uba.dc.lafhis.henos.report;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.List;
//
public class ReportFluent extends ReportObject{
	protected String name;
	protected List<Integer> startingSignals;
	protected List<Integer> endingSignals;
	protected boolean initially;

	public String getName() {return name;}
	public List<Integer> getStartingSignals() {return startingSignals;}
	public List<Integer> getEndingSignals() {return endingSignals;}
	public boolean getInitialValue() {return initially;}
	//<name,starting_count,[f_1,..,f_n],ending_count,[f'_1,..,f'_m],initially>
	public ReportFluent(PushbackInputStream fis) {
	    try {
			char current;
			current = (char) fis.read();
			if(current != ReportConstants.AUT_SER_OBJ_START) {
				isOK = false;
				return;
			}
			int startCount	= readInt(fis, sepChar);
			startingSignals	= readIntArray(fis, sepChar);
			int endCount	= readInt(fis, sepChar);
			endingSignals	= readIntArray(fis, sepChar);
			initially		= readBoolean(fis, objEndChar);
	      } catch (IOException e) {
	    	  isOK = false;
	    	  e.printStackTrace();
	      }
	}
}