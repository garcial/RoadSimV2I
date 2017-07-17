package trafficData;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class TrafficData {
	private long tini;
	private long tfin;
	private int numCars;
	private ArrayList<Double> carsPositions;
	private ArrayList<Double> carsSpeeds;
	private ArrayList<String> ids;
	private JSONObject json;
	
	public TrafficData() {
		this.tini = 0;
		this.tfin = 0;
		this.numCars = 0;
		this.carsPositions = new ArrayList<Double>();
		this.carsSpeeds = new ArrayList<Double>();
		this.ids = new ArrayList<String>();
		json = null;
	}
	
	public TrafficData(JSONObject json) {
		this.tini = json.getInt("tini");
		this.tfin = json.getInt("tfin");
		this.setNumCars(json.getInt("numCars"));
		JSONArray carPos = json.getJSONArray("positions");
		this.carsPositions = new ArrayList<Double>();
		for(int i = 0; i<carPos.length(); i++) 
			carsPositions.add(carPos.getDouble(i));
		JSONArray speeds = json.getJSONArray("speeds");
		this.carsSpeeds = new ArrayList<Double>();
		for(int i = 0; i<speeds.length(); i++)
			carsSpeeds.add(speeds.getDouble(i));	
		JSONArray idsReceived = json.getJSONArray("ids");
		for(int i = 0; i< idsReceived.length(); i++)
			ids.add(idsReceived.getString(i));
		this.json = json;
	}
	
	public long getTini() {
		return tini;
	}
	public void setTini(long tini) {
		this.tini = tini;
	}
	public long getTfin() {
		return tfin;
	}
	public void setTfin(long tfin) {
		this.tfin = tfin;
	}
	public ArrayList<Double> getCarsPositions() {
		return carsPositions;
	}
	public void setCarsPositions(ArrayList<Double> carsPositions) {
		this.carsPositions = carsPositions;
	}
	public ArrayList<Double> getCarsSpeeds() {
		return carsSpeeds;
	}
	public void setCarsSpeeds(ArrayList<Double> carsSpeeds) {
		this.carsSpeeds = carsSpeeds;
	}

	public int getNumCars() {
		return numCars;
	}

	public void setNumCars(int numCars) {
		this.numCars = numCars;
	}
	

	public ArrayList<String> getIds() {
		return ids;
	}

	public void setIds(ArrayList<String> ids) {
		this.ids = ids;
	}
	
	public JSONObject toJSON(){
		if (json!= null) return json;
		JSONObject json = new JSONObject();
		json.put("tini", getTini());
		json.put("tfin", getTfin());
		json.put("numCars", getNumCars());
		json.put("positions", new JSONArray(getCarsPositions().toArray(new Double[0])));
		json.put("speeds", new JSONArray(getCarsSpeeds().toArray(new Double[0])));
		json.put("ids", new JSONArray(getIds().toArray(new String[0])));
		return json;
	}

	@Override
	public String toString() {
		return toJSON().toString();
	}

}

