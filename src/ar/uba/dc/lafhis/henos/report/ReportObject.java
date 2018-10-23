package ar.uba.dc.lafhis.henos.report;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class ReportObject {
	protected boolean isOK;
	
	protected Character lastFinalizer;
	
	public boolean getStatus() {return isOK;}
	
	protected static Character sepChar		= null;
	protected static Character arrayEndChar	= null;
	protected static Character objEndChar	= null;
	
	protected static List<Character> arrayFinalizers	= null;
	
	static {
		sepChar			= ReportConstants.AUT_SER_SEP;
		arrayEndChar	= ReportConstants.AUT_SER_ARRAY_END;
		objEndChar		= ReportConstants.AUT_SER_OBJ_END;
		arrayFinalizers	= new ArrayList<Character>();
		arrayFinalizers.add(sepChar); arrayFinalizers.add(arrayEndChar);
	}

	protected String readString(PushbackInputStream fis, Character finalizer) {
		List<Character> chars	= new ArrayList<Character>();
		chars.add(finalizer);
		return readString(fis, chars);
	}
	protected String readString(PushbackInputStream fis, List<Character> finalizers) {
		String value 	= "";
		lastFinalizer	= null;
		isOK			= true;
	    try {
			char current;
			while (fis.available() > 0) {
				current = (char) fis.read();
				if(!finalizers.contains(current)) {
					value += current;
				}else {
					lastFinalizer	= current;
					break;
				}
			}
		} catch (IOException e) {
			isOK = false;
			e.printStackTrace();
		}		
		return value;
	}
	
	protected int readInt(PushbackInputStream fis, Character finalizer) {
		List<Character> chars	= new ArrayList<Character>();
		chars.add(finalizer);
		return readInt(fis, chars);
	}
	protected int readInt(PushbackInputStream fis, List<Character> finalizers) {
		String value	= "";
		lastFinalizer	= null;
		isOK			= true;
	    try {
			char current;
			while (fis.available() > 0) {
				current = (char) fis.read();
				if(!finalizers.contains(current)) {
					value += current;
				}else {
					lastFinalizer	= current;
					break;
				}
			}
		} catch (IOException e) {
			isOK = false;
			e.printStackTrace();
		}		
		return Integer.parseInt(value);
	}
	
	protected boolean readBoolean(PushbackInputStream fis, Character finalizer) {
		List<Character> chars	= new ArrayList<Character>();
		chars.add(finalizer);
		return readBoolean(fis, chars);
	}
	protected boolean readBoolean(PushbackInputStream fis, List<Character> finalizers) {
		String value	= "";
		lastFinalizer	= null;
		isOK			= true;
	    try {
			char current;
			while (fis.available() > 0) {
				current = (char) fis.read();
				if(!finalizers.contains(current)) {
					value += current;
				}else {
					lastFinalizer	= current;
					break;
				}
			}
		} catch (IOException e) {
			isOK = false;
			e.printStackTrace();
		}		
	    value	= value.trim();
	    if(value.equals("1"))return true;
	    if(value.equals("0"))return false;
	    isOK	= false;
	    return false;
	} 
	protected List<Boolean> readBooleanArray(PushbackInputStream fis, Character finalizer){
		List<Character> chars	= new ArrayList<Character>();
		chars.add(finalizer);
		return readBooleanArray(fis, chars);
	}	
	protected List<Boolean> readBooleanArray(PushbackInputStream fis, List<Character> finalizers){
		List<Boolean> values	= new ArrayList<Boolean>();
		lastFinalizer			= null;
		isOK					= true;
		int currentInt;
	    try {
			char current 	= (char) fis.read();
			if(current != ReportConstants.AUT_SER_ARRAY_START) {
				isOK	= false;
				return values;
			}	
			do {
				current		= (char)fis.read();
				if(current == ReportConstants.AUT_SER_ARRAY_END) {
					break;
				}	
				fis.unread(current);
				currentInt	= readInt(fis, arrayFinalizers);
				if(!isOK)
					break;
				values.add(currentInt == 1);
			}while(lastFinalizer != null && !(lastFinalizer.charValue() == (ReportConstants.AUT_SER_ARRAY_END)));

			if (fis.available() > 0) {
				current = (char) fis.read();
				if(!finalizers.contains(current)) {
					isOK	= false;
				}else {
					lastFinalizer	= current;
				}
			}
		} catch (IOException e) {
			isOK = false;
			e.printStackTrace();
		}		
		return values;
	}	
	protected List<List<Boolean>> readBooleanArrayArray(PushbackInputStream fis, Character finalizer){
		List<Character> chars	= new ArrayList<Character>();
		chars.add(finalizer);
		return readBooleanArrayArray(fis, chars);
	}	
	protected List<List<Boolean>> readBooleanArrayArray(PushbackInputStream fis, List<Character> finalizers){
		List<List<Boolean>> values	= new ArrayList<List<Boolean>>();
		lastFinalizer			= null;
		isOK					= true;
	    try {
			char current 	= (char) fis.read();
			if(current != ReportConstants.AUT_SER_ARRAY_START) {
				isOK	= false;
				return values;
			}			
			do {
				current		= (char)fis.read();
				if(current == ReportConstants.AUT_SER_ARRAY_START) {
					fis.unread(current);
					values.add(readBooleanArray(fis, arrayFinalizers));
				}
			}while((lastFinalizer.charValue() != (ReportConstants.AUT_SER_ARRAY_END)));
			if (fis.available() > 0) {
				current = (char) fis.read();
				if(!finalizers.contains(current)) {
					isOK	= false;
				}else {
					lastFinalizer	= current;
				}
			}
		} catch (IOException e) {
			isOK = false;
			e.printStackTrace();
		}		
		return values;
	}		
	protected List<Integer> readIntArray(PushbackInputStream fis, Character finalizer){
		List<Character> chars	= new ArrayList<Character>();
		chars.add(finalizer);
		return readIntArray(fis, chars);
	}	
	protected List<Integer> readIntArray(PushbackInputStream fis, List<Character> finalizers){
		List<Integer> values	= new ArrayList<Integer>();
		lastFinalizer			= null;
		isOK					= true;
		int currentInt;
	    try {
			char current 	= (char) fis.read();
			if(current != ReportConstants.AUT_SER_ARRAY_START) {
				isOK	= false;
				return values;
			}	
			do {
				current		= (char)fis.read();
				if(current == ReportConstants.AUT_SER_ARRAY_END) {
					break;
				}	
				fis.unread(current);
				currentInt	= readInt(fis, arrayFinalizers);
				if(!isOK)
					break;
				values.add(currentInt);
			}while(lastFinalizer != null && !(lastFinalizer.charValue() == (ReportConstants.AUT_SER_ARRAY_END)));

			if (fis.available() > 0) {
				current = (char) fis.read();
				if(!finalizers.contains(current)) {
					isOK	= false;
				}else {
					lastFinalizer	= current;
				}
			}
		} catch (IOException e) {
			isOK = false;
			e.printStackTrace();
		}		
		return values;
	}
	protected List<List<Integer>> readIntArrayArray(PushbackInputStream fis, Character finalizer){
		List<Character> chars	= new ArrayList<Character>();
		chars.add(finalizer);
		return readIntArrayArray(fis, chars);
	}	
	protected List<List<Integer>> readIntArrayArray(PushbackInputStream fis, List<Character> finalizers){
		List<List<Integer>> values	= new ArrayList<List<Integer>>();
		lastFinalizer			= null;
		isOK					= true;
	    try {
			char current 	= (char) fis.read();
			if(current != ReportConstants.AUT_SER_ARRAY_START) {
				isOK	= false;
				return values;
			}			
			do {
				current		= (char)fis.read();
				if(current == ReportConstants.AUT_SER_ARRAY_START) {
					fis.unread(current);
					values.add(readIntArray(fis, arrayFinalizers));
				}
			}while((lastFinalizer.charValue() != (ReportConstants.AUT_SER_ARRAY_END)));
			if (fis.available() > 0) {
				current = (char) fis.read();
				if(!finalizers.contains(current)) {
					isOK	= false;
				}else {
					lastFinalizer	= current;
				}
			}
		} catch (IOException e) {
			isOK = false;
			e.printStackTrace();
		}		
		return values;
	}	
	protected List<String> readStringArray(PushbackInputStream fis, Character finalizer){
		List<Character> chars	= new ArrayList<Character>();
		chars.add(finalizer);
		return readStringArray(fis, chars);
	}	
	protected List<String> readStringArray(PushbackInputStream fis, List<Character> finalizers){
		List<String> values	= new ArrayList<String>();
		lastFinalizer			= null;
		isOK					= true;
		String currentString;
	    try {
			char current 	= (char) fis.read();
			if(current != ReportConstants.AUT_SER_ARRAY_START) {
				isOK	= false;
				return values;
			}	
			do {
				currentString	= readString(fis, arrayFinalizers);
				if(!isOK)
					break;
				values.add(currentString);
			}while(lastFinalizer != null && !(lastFinalizer.charValue() == (ReportConstants.AUT_SER_ARRAY_END)));
			if (fis.available() > 0) {
				current = (char) fis.read();
				if(!finalizers.contains(current)) {
					isOK	= false;
				}else {
					lastFinalizer	= current;
				}
			}
		} catch (IOException e) {
			isOK = false;
			e.printStackTrace();
		}		
		return values;
	}	
}
