package environment;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import agents.SegmentAgent;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import jgrapht.Edge;

/**
 * Represents a section of a road in a single direction.
 * This section is only accessible from its origin and can only be 
 * left by its destination.
 */
public class Segment implements Serializable{

	private static final long serialVersionUID = -6853406084306746147L;
	
	//Unique id
	private String id;

	//Where the segment is accessed from
	private Intersection origin;

	//Where the segment is left
	private Intersection destination;

	//Length in kilometers of the segment
	private double length;

	//Capacity
	private int capacity;
	
	//Density
	private double density;

	//Number of tracks
	private int numberTracks;

	//The steps that form the segment
	private List<Step> steps;

	//Max speed
	private int maxSpeed;
	
	//Current allowed speed
	private int currentAllowedSpeed;

	//Kilometric points
	private float pkIni;
	
	//Direction
	private String direction;

	//Variable to draw the GUI
	private boolean drawGUI;
	
	//My edge of the jgrapht
	Edge myEdge;

	//Segment agent
	private SegmentAgent segmentAgent;

	//The container where the agents will be created
	@SuppressWarnings("unused")
	private transient jade.wrapper.AgentContainer mainContainer;
	
	//This dictionary contains the different service levels
	private HashMap<Integer, Float> serviceLevels;
	
	//The current service level
	private int currentServiceLevel;
	
	//Logging info
	private boolean segmentLogging;
	

	/**
	 * Constructor. 
	 *
	 * @param  origin {@link Intersection} where this {@link Segment} starts.
	 * @param  destination {@link Intersection} where this {@link Segment} ends.
	 * @param  length The length of this {@link Segment} in Km.
	 */
	public Segment(DirectedWeightedMultigraph<Intersection, Edge> jgrapht, 
			       String id, Intersection origin, Intersection destination, 
			       double length, int maxSpeed, int capacity, int density, 
			       int numberTracks, jade.wrapper.AgentContainer mainContainer, 
			       boolean segmentLogging, boolean drawGUI,
			       String direction, double pkstart, long tick){
		
		this.id = id;
		this.origin = origin;
		this.destination = destination;
		this.length = length;
		this.maxSpeed = maxSpeed;
		this.capacity = capacity;
		this.density = density;
		this.numberTracks = numberTracks;
		this.steps = new LinkedList<Step>();
		this.mainContainer = mainContainer;
		this.currentAllowedSpeed = this.maxSpeed;
		this.serviceLevels = new HashMap<Integer, Float>();
		this.currentServiceLevel = 0; // 'A';
		this.segmentLogging = segmentLogging;
		this.drawGUI = drawGUI;
		this.direction = direction;
		this.pkIni = (float) pkstart;
		
		//Put the service levels
		this.serviceLevels.put(0, 1.00f);
		this.serviceLevels.put(1, 0.95f);
		this.serviceLevels.put(2, 0.80f);
		this.serviceLevels.put(3, 0.65f);
		this.serviceLevels.put(4, 0.50f);
		this.serviceLevels.put(5, 0.10f);

		//Create the agents
		try {

			//Agent Controller to segments with Interface
			AgentController agent = mainContainer.createNewAgent(
					this.id, "agents.SegmentAgent", new Object[]{this, jgrapht, 
							 this.drawGUI, this.segmentLogging, tick});

			agent.start();
			
		} catch (StaleProxyException e) {

			System.out.println("Error starting " + this.id);
			e.printStackTrace();
		}
	}

	public void addStep(Step step) {
		this.steps.add(step);
	}

	//Setters and getters		
	public String getId() {
		return id;
	}

	public Intersection getOrigin() {
		return origin;
	}

	public Intersection getDestination() {
		return destination;
	}

	public double getLength() {
		return length;
	}

	public int getCapacity() {
		return capacity;
	}

	public int getNumberTracks() {
		return numberTracks;
	}

	public List<Step> getSteps() {
		return steps;
	}
	
	public Step getStep(int posicion) {
		if (posicion >= getSteps().size()) return null;
		return steps.get(posicion);
	}

	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public float getPkIni() {
		return pkIni;
	}

	public SegmentAgent getSegmentAgent() {
		return segmentAgent;
	}

	public void setSegmentAgent(SegmentAgent segmentAgent) {
		this.segmentAgent = segmentAgent;
		this.segmentAgent.setSegment(this);
	}

	public double getDensity() {
		return density;
	}

	public void setDensity(double density) {
		this.density = density;
	}

	public int getCurrentAllowedSpeed() {
		return currentAllowedSpeed;
	}

	public void setCurrentAllowedSpeed(int currentAllowedSpeed) {
		this.currentAllowedSpeed = currentAllowedSpeed;
	}

	public int getCurrentServiceLevel() {
		return currentServiceLevel;
	}

	public void setCurrentServiceLevel(int currentServiceLevel) {
		this.currentServiceLevel = currentServiceLevel;
		this.currentAllowedSpeed = (int) 
				(this.maxSpeed * this.serviceLevels.get(currentServiceLevel));
	}

	public boolean isSegmentLogging() {
		return segmentLogging;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public void setMyEdge(Edge e) {
		myEdge = e;		
	}
	
	public Edge getMyEdge() {
		return myEdge;
	}
		
}