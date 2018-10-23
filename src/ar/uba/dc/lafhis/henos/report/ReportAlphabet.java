package ar.uba.dc.lafhis.henos.report;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.List;

public class ReportAlphabet extends ReportObject {
	protected List<ReportSignal> signals;
	
	public List<ReportSignal> getSignals() {return signals;}
	//<count,[sig_1,..,sig_count]>
	public ReportAlphabet(PushbackInputStream fis) {
	    try {
			char current;
			current = (char) fis.read();
			if(current != ReportConstants.AUT_SER_OBJ_START) {
				isOK 	= false;
				return;
			}
			signals			= new ArrayList<ReportSignal>();
			int count		= readInt(fis, ReportConstants.AUT_SER_SEP);
			if(count == 0) {
				isOK	= false;
				return;
			}
			current = (char) fis.read();
			if(current != ReportConstants.AUT_SER_ARRAY_START) {
				isOK	= false;
				return;
			}			
			do {
				signals.add(new ReportSignal(fis));
				current = (char) fis.read();
				if(current != ReportConstants.AUT_SER_ARRAY_END && current
						!= ReportConstants.AUT_SER_SEP) {
					isOK	= false;
					return;
				}
					
			}while(current != ReportConstants.AUT_SER_ARRAY_END);
			current = (char) fis.read();
			if(current != ReportConstants.AUT_SER_OBJ_END) {
				isOK = false;
				return;
			}
	      } catch (IOException e) {
	    	  isOK = false;
	    	  e.printStackTrace();
	      }
	}
}