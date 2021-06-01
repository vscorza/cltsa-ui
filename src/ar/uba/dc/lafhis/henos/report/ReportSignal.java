package ar.uba.dc.lafhis.henos.report;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;

public class ReportSignal extends ReportObject{
	protected String name;
	protected ReportSignalType type;
	
	public String getName() {
		if(name == null)
			return "";
		else
			return name.replace(".on", "↑").replace(".off","↓").replace("â", ""); 
	}
	public ReportSignalType getType() {return type;}
	//<name,type>
	public ReportSignal(PushbackInputStream fis) {
	    try {
			char current;
			current = (char) fis.read();
			if(current != ReportConstants.AUT_SER_OBJ_START) {
				isOK = false;
				return;
			}
			name			= readString(fis, sepChar);
			int typeValue	= readInt(fis, objEndChar);
			switch(typeValue) {
			case 0: type = ReportSignalType.INPUT; break;
			case 1: type = ReportSignalType.OUTPUT; break;
			case 2: type = ReportSignalType.INTERNAL; break;
			default: isOK = false; break;
			}

	      } catch (IOException e) {
	    	  isOK = false;
	    	  e.printStackTrace();
	      }
	}
}
