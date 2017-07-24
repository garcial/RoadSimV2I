package vehicles;

import org.json.JSONObject;

public class CarData {
	private String id; // The getName() of the carAgent
	private float x, y;
	private float currentSpeed;
	private int typeOfAlgorithm;
	private float segmentDistanceCovered;
	private long currentTick;
	private float tripDistanceCovered;

	public CarData(String id, float x, float y, float currentSpeed,
			       int typeOfAlgorithm, float segmentDistanceCovered,
			       long currentTick) {
		super();
		this.id = id;
		this.x = x;
		this.y = y;
		this.currentSpeed = currentSpeed;
		this.typeOfAlgorithm = typeOfAlgorithm;
		this.segmentDistanceCovered = segmentDistanceCovered;
		this.currentTick = currentTick;
		this.tripDistanceCovered = 0f;
	}

	public String getId() {
		return id;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getCurrentSpeed() {
		return currentSpeed;
	}

	public void setCurrentSpeed(float currentSpeed) {
		this.currentSpeed = currentSpeed;
	}

	public int getTypeOfAlgorithm() {
		return typeOfAlgorithm;
	}

	public float getSegmentDistanceCovered() {
		return segmentDistanceCovered;
	}

	public void setSegmentDistanceCovered(float segmentDistanceCovered) {
		this.segmentDistanceCovered = segmentDistanceCovered;
	}

	public long getCurrentTick() {
		return currentTick;
	}

	public void setCurrentTick(long currentTick) {
		this.currentTick = currentTick;
	}
	public JSONObject toJSON() {
		JSONObject resultado =  new JSONObject();
		resultado.put("id", getId());
		resultado.put("x", getX());
		resultado.put("y", getY());
		resultado.put("speed", getCurrentSpeed());
		resultado.put("type", getTypeOfAlgorithm());
		resultado.put("segmentDistanceCovered", getSegmentDistanceCovered());
		resultado.put("tick", getCurrentTick());
		resultado.put("tripDistanceCovered", getTripDistanceCovered());
		return resultado;
	}

	public void incSegmentDistanceCovered(float deltaPk) {
		segmentDistanceCovered += deltaPk;
		
	}

	public float getTripDistanceCovered() {
		return tripDistanceCovered;
	}

	public void incTripDistanceCovered(float increment) {
		this.tripDistanceCovered += increment;
	}

}

