package trafficData;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;


public class TrafficDataInStore {

	private HashMap<String, ArrayList<TrafficData>> data;
	
	public TrafficDataInStore() {
		data = new HashMap<String, ArrayList<TrafficData>>();
	}

	public HashMap<String, ArrayList<TrafficData>> getData() {
		return data;
	}

	public void setData(HashMap<String, ArrayList<TrafficData>> data){
		this.data = data;
	}
	public void put(String key, JSONObject json) {
		if (data.containsKey(key)) 
			data.get(key).add(new TrafficData(json));
		else {
			ArrayList<TrafficData> al = new ArrayList<TrafficData>();
			al.add(new TrafficData(json));
			data.put(key, al);
		}
	}
	
	public void delete(String key) {
		data.remove(key);
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		for(String key:data.keySet()) {
			json.put(key, data.get(key));
		}
		return json;
	}
	
}
