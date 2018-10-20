package ar.uba.dc.lafhis.experiments.jung;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

public class ExperimentJUNGGameEdgeValue<ID, S, L> implements JSONAware{
	protected ID id;
	protected S from;
	protected S to;
	protected List<L> labels;
	
	protected boolean isControllable;
	
	public ExperimentJUNGGameEdgeValue(ID id, S from, S to, boolean isControllable, List<L> labels){
		this.id				= id;
		this.isControllable	= isControllable;
		this.from			= from;
		this.to				= to;
		this.labels			= new ArrayList<L>();
		for(L l : labels) {
			this.labels.add(l);
		}
	}
	
	public ID getID(){
		return id;
	}
	
	public S getOrigin(){
		return this.from;
	}
	
	public S getDestination(){
		return this.to;
	}
	
	public boolean getIsControllable(){
		return isControllable;
	}
	
	public List<L> getLabels(){
		return new ArrayList<L>(labels);
	}
	
	@Override
	public String toString() {
		String value = id.toString() +(isControllable? "!":"?") + "<";
		boolean firstValue = true;
		for(L l : labels) {
			if(firstValue) {
				firstValue = false;
			}else {
				value += ",";
			}
			value += l.toString();
		}
		value += ">";
		return value;
	}

	@Override
	public String toJSONString() {
		String jsonLabels	= ",\"labels:[";
		boolean firstValue = true;
		for(L l : labels) {
			if(firstValue) {
				firstValue = false;
			}else {
				jsonLabels += ",";
			}
			jsonLabels += l.toString();
		}
		jsonLabels += "]";
		return "{\"id\":\""+id.toString()+"\",\"from\":\""+ from +"\",\"to\":\""+ to +"\",\"isControllable\":"+ isControllable
				+ jsonLabels + "}";
	}
}
