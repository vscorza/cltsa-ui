package ar.uba.dc.lafhis.henos.report;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReportContext extends ReportObject {
	protected String name;
	protected ReportAlphabet alphabet;
	protected List<ReportFluent> fluents;
	protected List<String> livenessNames;
	
	
	public String getName() {return name;}
	public ReportAlphabet getAlphabet() { return alphabet;}
	public List<ReportFluent> getfluents(){return fluents;}
	public List<String> getLivenessNames(){return livenessNames;}
	//<name,alphabet,fluents_count,[f_1.name,..,f_n.name],liveness_count,[l_1.name,..,l_m.name]>
	public ReportContext(FileInputStream fis) {
		fluents			= new ArrayList<ReportFluent>();
		livenessNames	= new ArrayList<String>();
	    try {
			char current;
			current = (char) fis.read();
			if(current != ReportConstants.AUT_SER_OBJ_START) {
				isOK = false;
				return;
			}
			name			= readString(fis, ReportConstants.AUT_SER_SEP);
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
				do {
					fluents.add(new ReportFluent(fis));
					current = (char) fis.read();
					if(current != ReportConstants.AUT_SER_ARRAY_END && current
							!= ReportConstants.AUT_SER_SEP) {
						isOK	= false;
						return;
					}
						
				}while(current != ReportConstants.AUT_SER_ARRAY_END);
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
	      } catch (IOException e) {
	    	  isOK = false;
	    	  e.printStackTrace();
	      }		
	}
}
