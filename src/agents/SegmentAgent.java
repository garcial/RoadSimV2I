package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONArray;
import org.json.JSONObject;
import behaviours.SegmentListenBehaviour;
import behaviours.SegmentSendToDrawBehaviour;
import environment.Intersection;
import environment.Segment;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jgrapht.Edge;
import vehicles.CarData;

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
	private DirectedWeightedMultigraph<Intersection, Edge> jgrapht;

	//The cars that are currently on this segment
	private HashMap<String, CarData> cars;
	//The initial time in which the currentServiceLevel begins
	private long tini;
	
	//The log agent if it is requested on main
	private boolean useLog;
	private DFAgentDescription logAgent;

	@SuppressWarnings("unchecked")
	protected void setup() {

		//Get the segment from parameter
		this.segment = (Segment) this.getArguments()[0];
		this.jgrapht = (DirectedWeightedMultigraph<Intersection, Edge>) this.getArguments()[1];
		this.drawGUI = (boolean) this.getArguments()[2];
		this.useLog = (boolean) this.getArguments()[3];
		this.segment.setSegmentAgent(this);
		this.cars = new HashMap<String, CarData>();
		this.setTini((long) this.getArguments()[4]);

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
		
		if (useLog) {
			dfd = new DFAgentDescription();
			sd = new ServiceDescription();
			sd.setType("logAgent");
			dfd.addServices(sd);
			DFAgentDescription[] result = null;
			try {
				result = DFService.searchUntilFound(
						this, getDefaultDF(), dfd, null, 5000);
			} catch (FIPAException e) { e.printStackTrace(); }
			while (result == null || result[0] == null) {
				try {
					result = DFService.searchUntilFound(
							this, getDefaultDF(), dfd, null, 5000);
				} catch (FIPAException e) { e.printStackTrace(); }
			}
			this.logAgent = result[0];
		}
		
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
	public void addCar(JSONObject car) {

		this.cars.put(car.getString("id"), 
				new CarData(car.getString("id"), 
						    (float) car.getDouble("x"),
						    (float) car.getDouble("y"),
						    (float) car.getDouble("speed"),
						    (int) car.getInt("type"),
						    (float) car.getDouble("segmentDistanceCovered"),
						    car.getLong("tick")));
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
	 * @param JSONObject of a CarData objetc
	 */
	public void updateCar(JSONObject car) {

		CarData aux = cars.get(car.getString("id"));
		aux.setX((float) car.getDouble("x"));
		aux.setY((float) car.getDouble("y"));
		aux.setCurrentSpeed((float) car.getDouble("speed"));
		aux.setSegmentDistanceCovered(
				(float) car.getDouble("segmentDistanceCovered"));
		aux.setCurrentTick(car.getLong("tick"));
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
			ret2.put("type", car.getTypeOfAlgorithm());
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

		if (currentTick % 15 == 0) {

			JSONObject data = new JSONObject();
			data.put("id", getLocalName());
			data.put("time", currentTick);
			data.put("currentSpeed", segment.getCurrentAllowedSpeed());
			data.put("maxSpeed", segment.getMaxSpeed());
			
			List<CarData> lista = 
					new ArrayList<CarData>(cars.values());
			java.util.Map<Integer, Long> counted = 
					lista.stream().
		            collect(Collectors.groupingBy(	            		
		            		(x->x.getTypeOfAlgorithm()), 
		            		Collectors.counting()));

			data.put("shortest", counted.get(0)==null?0:counted.get(0));
			data.put("fastest", counted.get(1)==null?0:counted.get(1));
			data.put("startSmart", counted.get(2)==null?0:counted.get(2));
			data.put("dynamicSmart", counted.get(3)==null?0:counted.get(3));
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setOntology("segmentToLog");
			msg.addReceiver(logAgent.getName());
//			System.out.println("msg: " + data.toString());
			msg.setContent(data.toString());
			send(msg);
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
	
	public DirectedWeightedMultigraph<Intersection, Edge> getJgrapht() {
		return jgrapht;
	}

	public long getTini() {
		return tini;
	}

	public void setTini(long tini) {
		this.tini = tini;
	}

}
