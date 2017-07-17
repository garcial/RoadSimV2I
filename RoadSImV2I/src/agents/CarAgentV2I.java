package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import searchAlgorithms.Method;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AStarShortestPath;
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.json.JSONObject;

import behaviours.CarBehaviourV2I;
import environment.Intersection;
import environment.Map;
import environment.Path;
import environment.Segment;
import environment.Step;
import jgrapht.Edge;

/**
 * This code represents a mobile car, it will have an origin an a 
 * destination and will get there using either the shortest, 
 * fastest or smartest path.
 *
 */
public class CarAgentV2I extends Agent {


	private static final long serialVersionUID = 1L;

	private float x, y;
	private float currentPk;
	private int direction;
	private int currentSpeed, maxSpeed;
	private double currentTrafficDensity;
	private long tini; // For measuring temporal intervals of traffic
	private String id; 
	private DFAgentDescription interfaceAgent;
	private boolean drawGUI;
	private Map map;
	private Path path;
	private Segment currentSegment;
	private String initialIntersection, finalIntersection;
	private boolean smart = false;
	private DefaultDirectedWeightedGraph<Intersection, Edge> jgrapht;
	private int algorithmType;
	
//	private AStarAdmissibleHeuristic<Intersection> admissibleHeuristic;
//	private AStarShortestPath<Intersection, Edge> astar;
   

	protected void setup() {

		//Register the agent
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("carAgentV2I");
		sd.setName(getLocalName());

		dfd.addServices(sd);
		try {
			DFService.register(this,  dfd);
		} catch (FIPAException fe) {
			this.takeDown();
		}
		
		//Is necessary draw the gui
		this.drawGUI = (boolean) this.getArguments()[5];

		//Get the map from an argument
		this.map = (Map) this.getArguments()[0];
		//Get the jgraph from the map
		this.jgrapht = this.map.getJgrapht();
		System.out.println("CarAgent.java-- Get JgraphT: " + 
		                   this.jgrapht.toString());
		//Get the starting and final points of my trip
		this.initialIntersection = (String) this.getArguments()[1];
		this.finalIntersection = (String) this.getArguments()[2];
		
		//Get the speeds
		this.maxSpeed = (int) this.getArguments()[3];
		this.currentSpeed = 0; //Se gestiona en el comportamiento 
		                       // (int) this.getArguments()[4];

		String routeType = (String) this.getArguments()[4];
		
		if (routeType.equals("fastest")) {
			this.algorithmType = Method.FASTEST.value;
		} else if (routeType.equals("shortest")) {
			this.algorithmType = Method.SHORTEST.value;
		} else if(routeType.equals(Method.DYNAMIC.value)) {
			this.algorithmType = Method.DYNAMIC.value;
		}
//		else if (routeType.equals("a_star")) {
//			this.algorithmType = Method.A_STAR.value;
//			admissibleHeuristic = 
//				new AStarAdmissibleHeuristic<Intersection>() {
//				  @Override
//				  public double getCostEstimate(Intersection vs, 
//					 	                        Intersection ve) {
//					  Segment s = jgrapht.getEdge(vs, ve).getSegment();
//					  return s.getLength()/s.getMaxSpeed();
//				}
//			};
//			astar = new AStarShortestPath<>(jgrapht, 
//					                        admissibleHeuristic);
//			this.smart = true;
//		} 
//		else if(routeType.equals("dijsktra")) {
//			this.algorithmType = Method.DIJKSTRA.value;
//			this.smart = true;
//		} 
//		else if (routeType.equals("kshortest")) {
//			this.algorithmType = Method.KSHORTEST.value;
//			this.smart = true;
//		}
		
		//Get the initial time tick from eventManager
		tini = (long) this.getArguments()[6];
		
		//An unique identifier for the car
		this.id = getName().toString();
		
		//Get the desired Path from the origin to the destination
		// using the jgrapht
		path = getPathOnMethod(initialIntersection, 
						       finalIntersection);
		//Starting point
		setX(path.getGraphicalPath().get(0).getOriginX());
		setY(path.getGraphicalPath().get(0).getOriginY());
		
		if(this.drawGUI){
			//Find the interface agent
			dfd = new DFAgentDescription();
			sd = new ServiceDescription();
			sd.setType("interfaceAgent");
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
			
			this.interfaceAgent = result[0];
		}

		if(this.drawGUI){
			//We notify the interface about the new car
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(interfaceAgent.getName());
			JSONObject carData = new JSONObject();
			carData.put("x", this.x);
			carData.put("y", this.y);
			carData.put("id", this.id);
			carData.put("algorithmType", this.algorithmType);
			msg.setContent(carData.toString());
			msg.setOntology("newCarOntology");
			send(msg);
		}

		// Set the initial values for the carAgent on the road
		Step current = path.getGraphicalPath().get(0);
	    setCurrentSegment(current.getSegment());

		//Register
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setOntology("carToSegmentOntology");
		msg.setConversationId("register");
		msg.addReceiver(current.getSegment().getSegmentAgent().getAID());
		JSONObject carDataRegister = new JSONObject();
		carDataRegister.put("id", getId());
		carDataRegister.put("x", getX());
		carDataRegister.put("y", getY());
		carDataRegister.put("currentSpeed", maxSpeed/4);
		// When starting my route I begin with 120/4 in the first second
		msg.setContent(carDataRegister.toString());
		
		send(msg);
		// Receive the current traffic density from the current 
		//    segment
		msg = blockingReceive(MessageTemplate.
				             MatchOntology("trafficDensityOntology"));
		JSONObject densityData = new JSONObject(msg.getContent());
		setCurrentTrafficDensity(densityData.getDouble("density"));

		//Change my speed according to the maximum allowed speed
	    setCurrentSpeed(Math.min(getMaxSpeed(), 
	    			getCurrentSegment().getCurrentAllowedSpeed()));
		
	    //The special color is useless without the interfaceAgent
		//Runs the agent
		addBehaviour(new CarBehaviourV2I(this, 50, this.drawGUI));

	}
	
	/**
	 * Recalculate the route, this will be called from the behaviour 
	 *     if we are smart.
	 * 
	 * @param origin ID of the intersection where the car is
	 */
	public void recalculate(String origin) {
		
		// A JGraph envision structure must be obtained from jgraphsT 
		//     received by other cars in the twin segment of the 
		//     current segment where the car is going.
		// TODO:
		path = getPathOnMethod(origin, finalIntersection);
	}

	//Setters and getters
	public int getDirection() {
		return direction;
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

	public float getCurrentPk() {
		return currentPk;
	}

	public void setCurrentPk(float currentPk) {
		this.currentPk = currentPk;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public int getCurrentSpeed() {
		return currentSpeed;
	}

	public void setCurrentSpeed(int currentSpeed) {
		this.currentSpeed = currentSpeed;
	}

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(int maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public DFAgentDescription getInterfaceAgent() {
		return interfaceAgent;
	}

	public Map getMap() {
		return map;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public String getId() {
		return id;
	}

	public Segment getCurrentSegment() {
		return currentSegment;
	}

	public void setCurrentSegment(Segment previousSegment) {
		this.currentSegment = previousSegment;
	}

	public String getInitialIntersection() {
		return initialIntersection;
	}

	public String getFinalIntersection() {
		return finalIntersection;
	}
	
	public DefaultDirectedWeightedGraph<Intersection, Edge> 
	                                                   getJgrapht() {
		return jgrapht;
	}

	public void setJgrapht(
			  DefaultDirectedWeightedGraph<Intersection,Edge> jgraht){
		this.jgrapht = jgraht;
	}
	
	public boolean isSmart() {
		
		return this.smart;
	}

	public int getAlgorithmType() {
		return algorithmType;
	}	
	
	public double getCurrentTrafficDensity() {
		return currentTrafficDensity;
	}

	public void setCurrentTrafficDensity(double currentTD) {
		this.currentTrafficDensity = currentTD;
	}

//	public long getElapsedtime() {
//		return elapsedtime;
//	}
//
//	public void increaseElapsedtime() {
//		this.elapsedtime++;
//	}

	public long getTini() {
		return tini;
	}

	public void setTini(long tini) {
		this.tini = tini;
	}
	
	public Path getPathOnMethod(String initialInterseccion,
			                    String finalIntersection) {
		
		GraphPath<Intersection, Edge> pathJGrapht = null;
		if (algorithmType == Method.FASTEST.value || 
			algorithmType == Method.DYNAMIC.value) {
			pathJGrapht = DijkstraShortestPath.findPathBetween(jgrapht, 
					map.getIntersectionByID(initialInterseccion),
					map.getIntersectionByID(finalIntersection));
		} else if (algorithmType == Method.SHORTEST.value) {
			putWeightsAsDistancesOnGraph(jgrapht);
			pathJGrapht = DijkstraShortestPath.findPathBetween(jgrapht, 
					map.getIntersectionByID(initialInterseccion),
					map.getIntersectionByID(finalIntersection));
		} 
//		else if (algorithmType == Method.A_STAR.value) {
//			pathJGrapht = astar.getPath(
//					map.getIntersectionByID(initialInterseccion), 
//					map.getIntersectionByID(finalIntersection));
//		} else if(algorithmType == Method.DIJKSTRA.value) {
//			pathJGrapht = DijkstraShortestPath.findPathBetween(jgrapht, 
//					map.getIntersectionByID(initialInterseccion),
//					map.getIntersectionByID(finalIntersection));
//		} 
//		else if (algorithmType == Method.KSHORTEST.value) {
//			this.algorithmType = Method.KSHORTEST.value;
//		}
		
		List<Step> steps = new ArrayList<Step>();
		List<Segment> segments = new ArrayList<Segment>();
		for(Edge e: pathJGrapht.getEdgeList()){
			steps.addAll(e.getSegment().getSteps());
			segments.add(e.getSegment());
		}
		return new Path(pathJGrapht.getVertexList(),
				        steps, segments);
	}

	private void putWeightsAsDistancesOnGraph(
			DefaultDirectedWeightedGraph<Intersection, Edge> jgraht2){
		for(Edge e: jgraht2.edgeSet()) {
			jgraht2.setEdgeWeight(e, e.getSegment().getLength());
		}
	}

}

