package ar.uba.dc.lafhis.experiments.exchange;

import org.json.simple.JSONObject;

public class JSONCompatibleInt implements JSONCompatible {
	public static String VALUE_PARAM	= "value";
	
	protected int value;
	protected JSONObject jsonObject;
	
	public JSONCompatibleInt(int value) {
		this.value		= value;
		this.jsonObject	= new JSONObject();
		jsonObject.put(VALUE_PARAM, this.value);
	}
	
	public int getValue(){
		return value;
	}
	
	@Override
	public String toJSONString() {
		return jsonObject.toJSONString();
	}

	@Override
	public JSONObject toJSONObject() {
		return jsonObject;
	}

	@Override
	public void initializeFromJSONObject(JSONObject jsonObject) throws NullPointerException{
		if(!jsonObject.containsKey(VALUE_PARAM)){
			throw new NullPointerException("JSONCompatibleString::initializeFromJSONObject input object not properly structured");
		}
		this.jsonObject.put(VALUE_PARAM, jsonObject.get(VALUE_PARAM));
	}

}
