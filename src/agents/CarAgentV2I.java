package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import searchAlgorithms.Method;
import vehicles.CarData;

import java.util.ArrayList;
import java.util.List;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONObject;
import behaviours.CarBehaviourV2I;
import behaviours.CarSegmentDensityBehaviour;
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

	private CarData carData;
	private float currentPk;
	private int direction;
	private int maxSpeed;
	private double currentTrafficDensity; //TODO: Where it is used?
	private long tini; // For measuring temporal intervals of traffic
	private DFAgentDescription interfaceAgent;
	private boolean drawGUI;
	private Map map;
	private Path path;
	private Segment currentSegment;
	private String initialIntersection, finalIntersection;
	private boolean smart = false;
	private DirectedWeightedMultigraph<Intersection, Edge> jgrapht;
	private int type;
	private boolean useLog;
	private DFAgentDescription logAgent;

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
		//Get the jgraphT from the map
		this.jgrapht = this.map.getJgrapht();
		//Get the starting and final points of my trip
		this.initialIntersection = (String) this.getArguments()[1];
		this.finalIntersection = (String) this.getArguments()[2];
		
		//Get the speeds
		this.maxSpeed = (int) this.getArguments()[3];

		String routeType = (String) this.getArguments()[4];
		
		//Get the initial time tick from eventManager
		tini = (long) this.getArguments()[6];
		
		//It is requested to do Logs?
		useLog = (boolean) this.getArguments()[7];
	
		
	    if (routeType.equals("fastest")) 
	    	type = Method.FASTEST.value;
	    else if (routeType.equals("shortest"))
	    	type = Method.SHORTEST.value;
	    else if (routeType.equals("dynamicSmart")) {
	    	type = Method.DYNAMICSMART.value;
//	    	System.out.println(getLocalName() + " soy inteligente");
	    	smart = true;
	    } else type = Method.STARTSMART.value;
	    
		//Get the desired Path from the origin to the destination
		// using the jgrapht
		path = getPathOnMethod(initialIntersection, 
						       finalIntersection);
		Step current = path.getGraphicalPath().get(0);
	    setCurrentSegment(current.getSegment());
	    if (routeType.equals("fastest")) 
	    	type = Method.FASTEST.value;
	    else if (routeType.equals("shortest"))
	    	type = Method.SHORTEST.value;
	    else if (routeType.equals("dynamicSmart")) {
	    	type = Method.DYNAMICSMART.value;
//	    	System.out.println(getLocalName() + " soy inteligente");
	    	smart = true;
	    } else type = Method.STARTSMART.value;
		//Create new CarData object
		carData = new CarData(
				getName().toString(),  // Id
				current.getOriginX(),  // X
				current.getOriginY(),  // Y
				Math.min(getMaxSpeed()/4, // CurrentSpeed at the beginning
		    			getCurrentSegment().getCurrentAllowedSpeed()), 
				type,
				0,   // Segment distance covered
				tini // Current tick
				);
		
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
			logAgent = result[0];
		}
		
		if(this.drawGUI){
			//We notify the interface about the new car
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(interfaceAgent.getName());
			msg.setContent(carData.toJSON().toString());
			msg.setOntology("newCarOntology");
			send(msg);
		}

		//Register
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setOntology("carToSegmentOntology");
		msg.setConversationId("register");
		msg.addReceiver(current.getSegment().getSegmentAgent().getAID());
		msg.setContent(carData.toJSON().toString());
		
		send(msg);
		// Receive the current traffic density from the current 
		//    segment
		msg = blockingReceive(MessageTemplate.
				             MatchOntology("trafficDensityOntology"));
		JSONObject densityData = new JSONObject(msg.getContent());
		setCurrentTrafficDensity(densityData.getDouble("density"));

		//Runs the agent
		addBehaviour(new CarBehaviourV2I(this, 50, this.drawGUI));
		addBehaviour(new CarSegmentDensityBehaviour(this));

	}
	
	/**
	 * Recalculate the route, this will be called from the behaviour 
	 *     if we are smart.
	 * 
	 * @param origin ID of the intersection where the car is
	 */
	public void recalculate(String origin) {
		
		path = getPathOnMethod(origin, finalIntersection);
	}

	public CarData getCarData() {
		return carData;
	}

	public void setCarData(CarData carData) {
		this.carData = carData;
	}

	public int getTypeOfAlgorithm() {
		return type;
	}
	//Setters and getters
	public int getDirection() {
		return direction;
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
	
	public DirectedWeightedMultigraph<Intersection, Edge> 
	                                                   getJgrapht() {
		return jgrapht;
	}

	public void setJgrapht(
			  DirectedWeightedMultigraph<Intersection,Edge> jgraht){
		this.jgrapht = jgraht;
	}
	
	public boolean isSmart() {
		
		return this.smart;
	}	
	
	public double getCurrentTrafficDensity() {
		return currentTrafficDensity;
	}

	public void setCurrentTrafficDensity(double currentTD) {
		this.currentTrafficDensity = currentTD;
	}

	public long getTini() {
		return tini;
	}

	public void setTini(long tini) {
		this.tini = tini;
	}
	
	public DFAgentDescription getLogAgent() {
		return logAgent;
	}
	
	public boolean getUseLog(){
		return useLog;
	}

	public Path getPathOnMethod(String initialInterseccion,
			                    String finalIntersection) {
		
		GraphPath<Intersection, Edge> pathJGrapht = null;
		if (type == Method.DYNAMICSMART.value || 
			type == Method.STARTSMART.value) {
			pathJGrapht = DijkstraShortestPath.findPathBetween(jgrapht, 
					map.getIntersectionByID(initialInterseccion),
					map.getIntersectionByID(finalIntersection));
		} else if (type == Method.SHORTEST.value) {
			@SuppressWarnings("unchecked")
			DirectedWeightedMultigraph<Intersection, Edge> jgraphtClone = 
					(DirectedWeightedMultigraph<Intersection, Edge>) jgrapht.clone();
			putWeightsAsDistancesOnGraph(jgraphtClone);
			pathJGrapht = DijkstraShortestPath.findPathBetween(jgraphtClone, 
					map.getIntersectionByID(initialInterseccion),
					map.getIntersectionByID(finalIntersection));
		} else if (type == Method.FASTEST.value) {
			@SuppressWarnings("unchecked")
			DirectedWeightedMultigraph<Intersection, Edge> jgraphtClone = 
					(DirectedWeightedMultigraph<Intersection, Edge>) jgrapht.clone();
			putWeightAsTripMaxSpeedOnGraph(jgraphtClone);
			pathJGrapht = DijkstraShortestPath.findPathBetween(jgraphtClone, 
					map.getIntersectionByID(initialInterseccion),
					map.getIntersectionByID(finalIntersection));
		}
		
		List<Step> steps = new ArrayList<Step>();
		List<Segment> segments = new ArrayList<Segment>();
		for(Edge e: pathJGrapht.getEdgeList()){
			steps.addAll(map.getSegmentByID(e.getIdSegment()).getSteps());
			segments.add(map.getSegmentByID(e.getIdSegment()));
		}
		return new Path(pathJGrapht.getVertexList(),
				        steps, segments);
	}

	private void putWeightsAsDistancesOnGraph(
			DirectedWeightedMultigraph<Intersection, Edge> jgraht2) {
		for(Edge e: jgraht2.edgeSet()) {
			jgraht2.setEdgeWeight(e, e.getLength());
		}
	}
	
	private void putWeightAsTripMaxSpeedOnGraph(
			DirectedWeightedMultigraph<Intersection, Edge> jgraphtClone) {
		for(Edge e: jgraphtClone.edgeSet()) {
			jgraphtClone.setEdgeWeight(e, e.getLength() /
					                 e.getMaxSpeed());
		}
	}
}


//private AStarAdmissibleHeuristic<Intersection> admissibleHeuristic;
//private AStarShortestPath<Intersection, Edge> astar;


// En el setup():
//else if (routeType.equals("a_star")) {
//	this.algorithmType = Method.A_STAR.value;
//	admissibleHeuristic = 
//		new AStarAdmissibleHeuristic<Intersection>() {
//		  @Override
//		  public double getCostEstimate(Intersection vs, 
//			 	                        Intersection ve) {
//			  Segment s = jgrapht.getEdge(vs, ve).getSegment();
//			  return s.getLength()/s.getMaxSpeed();
//		}
//	};
//	astar = new AStarShortestPath<>(jgrapht, 
//			                        admissibleHeuristic);
//	this.smart = true;
//} 
//else if(routeType.equals("dijsktra")) {
//	this.algorithmType = Method.DIJKSTRA.value;
//	this.smart = true;
//} 
//else if (routeType.equals("kshortest")) {
//	this.algorithmType = Method.KSHORTEST.value;
//	this.smart = true;
//}

// En el getPathOnMethod:
//else if (algorithmType == Method.A_STAR.value) {
//pathJGrapht = astar.getPath(
//		map.getIntersectionByID(initialInterseccion), 
//		map.getIntersectionByID(finalIntersection));
//} else if(algorithmType == Method.DIJKSTRA.value) {
//pathJGrapht = DijkstraShortestPath.findPathBetween(jgrapht, 
//		map.getIntersectionByID(initialInterseccion),
//		map.getIntersectionByID(finalIntersection));
//} 
//else if (algorithmType == Method.KSHORTEST.value) {
//this.algorithmType = Method.KSHORTEST.value;
//}//}