package agents;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import behaviours.SegmentListenBehaviour;
import behaviours.SegmentSendToDrawBehaviour;
import environment.Segment;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * This agent will keep track of the cars that are inside between two
 * intersections and will update the data accordingly.
 *
 */
public class SegmentAgent extends Agent {

	private static final long serialVersionUID = 5681975046764849101L;

	//The segment this agent belongs to
	private Segment segment;
	private boolean drawGUI;
	private DFAgentDescription eventManagerAgent;

	//The cars that are currently on this segment
	private HashMap<String, CarData> cars;

	//Store the dynamic changes of service levels in all the segments
	private HashMap<String, Double> serviceLevelChanges;

	protected void setup() {

		//Get the segment from parameter
		this.segment = (Segment) this.getArguments()[0];
		this.drawGUI = (boolean) this.getArguments()[1];
		this.segment.setSegmentAgent(this);

		this.cars = new HashMap<String, CarData>();
		serviceLevelChanges = new HashMap<String, Double>();

		//Register the service
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());

		ServiceDescription sd = new ServiceDescription();
		sd.setType("segmentAgent");

		sd.setName(this.getSegment().getId());

		dfd.addServices(sd);

		try {
			DFService.register(this,  dfd);
		} catch (FIPAException fe) { 
			fe.printStackTrace(); 
		}
		
		dfd = new DFAgentDescription();
		sd = new ServiceDescription();
		sd.setType("eventManagerAgent");
		dfd.addServices(sd);
		DFAgentDescription[] result = null;
		try {
			result = DFService.searchUntilFound(
					this, getDefaultDF(), dfd, null, 5000);
		} catch (FIPAException e) { e.printStackTrace(); }
		eventManagerAgent = result[0];
		
		//This behaviour will keep the cars updated	
		addBehaviour(new SegmentListenBehaviour(this));

		//This behaviour will send the data to the GUI
		if(this.drawGUI){
			addBehaviour(new SegmentSendToDrawBehaviour(this));
		}
	}

	/**
	 * Add a car to this segment
	 * 
	 * @param id ID of the car (getName() of the carAgent
	 * @param x X coordinate of the car
	 * @param y Y coordinate of the car
	 * @param specialColor If we have to paint it specially
	 * @param radio is the radio of its sensor
	 */
	public void addCar(String id, float x, float y, float speed) {

		this.cars.put(id, new CarData(id, x, y, speed));
	}

	/**
	 * Remove a car from this segment
	 * 
	 * @param id ID of the car to remove
	 */
	public void removeCar(String id) {

		this.cars.remove(id);
	}

	/**
	 * Check if the car is contained in this segment
	 * 
	 * @param id ID of the car to check
	 * @return True if found, false otherwise
	 */
	public boolean containsCar(String id) {

		return this.cars.containsKey(id);
	}

	/**
	 * Updates the information of a car
	 * 
	 * @param id ID of the car to update
	 * @param x New x coordinate
	 * @param y New y coordinate
	 * @param specialColor New specialcolor
	 */
	public void updateCar(String id, float x, float y, float speed) {

		CarData aux = cars.get(id);
		aux.setX(x);
		aux.setY(y);
		aux.setCurrentSpeed(speed);
	}

	/**
	 * Creates the string with the information about this segment to
	 * notify the InterfaceAgent
	 * 
	 * @return String with the information of this segment
	 */
	public String getDrawingInformation() {

		// Como queremos esta estructura hemos preparado un JSONObject
		//    y metido una lista
		JSONObject resp = new JSONObject();
		JSONArray ret = new JSONArray();

		for(CarData car: cars.values()) {
			JSONObject ret2 = new JSONObject();
			ret2.put("id", car.getId());
			ret2.put("x", car.getX());
			ret2.put("y", car.getY());
			ret.put(ret2);
		}
		
		resp.put("cars", ret);
		return resp.toString();
	}

	/**
	 * This method logs the information of the segment.
	 * 
	 * @return
	 */
	public void doLog(long currentTick) {

		if (currentTick % 60 == 0) {

			int totalMinutes = (int)currentTick / 60;
			int hours = (int)(totalMinutes / 60);
			int minutes = (int)(totalMinutes % 60);

			//It is far more efficient to use this rather than a 
			//   simple String
			StringBuilder ret = new StringBuilder();

			//The time properly formated
			String time = String.format("%02d", hours) + ":" + 
			              String.format("%02d", minutes);

			ret.append(time + "," + this.getSegment().getMaxSpeed() +
					   "," + this.segment.getCurrentAllowedSpeed() + 
					   "," + this.segment.getCurrentServiceLevel() + 
					   "," + cars.size() + '\n');

			//Check if file exists
			File f = new File(Paths.get(
					this.segment.getLoggingDirectory() + "/" + 
			        this.getLocalName() + ".csv").toString());

			if (!f.exists()) {
				
				try {
					Files.write(Paths.get(
							this.segment.getLoggingDirectory() + "/" +
					        this.getLocalName() + ".csv"), 
							("Time,Vmax,Vcurrent,Service,Num cars\n" +
					        ret.toString()).getBytes());
				}catch (IOException e) {

					e.printStackTrace();
				}

			} else 

				try {
					Files.write(Paths.get(
							this.segment.getLoggingDirectory() + "/" +
					        this.getLocalName() + ".csv"), 
							ret.toString().getBytes(), 
							StandardOpenOption.APPEND);
				}catch (IOException e) {

					e.printStackTrace();
				}
		}
	}

	/**
	 * Number of cars in this segment
	 * 
	 * @return Number of cars in this segment
	 */
	public int carsSize() {

		return this.getCars().size();
	}

	//Getters and setters
	public Segment getSegment() {
		return segment;
	}

	public void setSegment(Segment segment) {
		this.segment = segment;
	}

	public HashMap<String, CarData> getCars() {
		return cars;
	}
	
	public void addServiceLevelChange(String segmentID, double time) {
		serviceLevelChanges.put(segmentID, time);
	}

	public HashMap<String, Double> getServiceLevelChanges() {
		return serviceLevelChanges;
	}

	public DFAgentDescription getEventManagerAgent() {
		return eventManagerAgent;
	}

	/**
	 * Auxiliary structure to keep track of the cars
	 *
	 */
	public class CarData {

		private String id; // The getName() of the carAgent
		private float x, y;
		private float currentSpeed;

		public CarData(String id, float x, float y, float speed) {

			this.id = id;
			this.x = x;
			this.y = y;
			this.currentSpeed = speed;
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

	}
}
