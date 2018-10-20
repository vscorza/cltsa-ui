package ar.uba.dc.lafhis.henos.report;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class ReportTransition extends ReportObject{
	protected int fromState;
	protected int toState;
	protected boolean isInput;
	protected List<Integer> labels;
	
	public int getFromState() {return fromState;}
	public int getToState() {return toState;}
	public boolean getIsInput() {return isInput;}
	public List<Integer> getLabels() {return labels;}
	//<from,to,sig_count,[s_1.idx,..,s_N.idx],is_input>
	public ReportTransition(FileInputStream fis) {
	    try {
			char current;
			current = (char) fis.read();
			if(current != ReportConstants.AUT_SER_OBJ_START) {
				isOK = false;
				return;
			}
			fromState		= readInt(fis, sepChar);
			toState			= readInt(fis, sepChar);
			int sigCount	= readInt(fis, sepChar);
			labels			= readIntArray(fis, sepChar);
			isInput			= readBoolean(fis, objEndChar);
	      } catch (IOException e) {
	    	  isOK = false;
	    	  e.printStackTrace();
	      }
	}
}
