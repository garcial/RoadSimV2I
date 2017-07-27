package behaviours;

import org.json.JSONObject;
import agents.SegmentAgent;
import environment.EdgeData;
import environment.Segment;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jgrapht.Edge;
import vehicles.CarData;

/**
 * This behaviour is used by the SegmentAgentV2I and listens to 
 * messages either by cars to register, deregister or update 
 * themselves from it or from the EventManagerAgent to tell them
 *  updates on its status.
 */
public class SegmentListenBehaviour extends Behaviour {

	private static final long serialVersionUID =-2533061568306629976L;

	//Template to listen for the new communications from cars
	private MessageTemplate mtCarControl = 
			MessageTemplate.and(
			  MessageTemplate.MatchPerformative(ACLMessage.INFORM),
			  MessageTemplate.MatchOntology("carToSegmentOntology"));

	private MessageTemplate mtEventManagerControl = 
		 MessageTemplate.and(
		   MessageTemplate.MatchPerformative(ACLMessage.INFORM),
		   MessageTemplate.MatchOntology(
				                   "eventManagerToSegmentOntology"));

	private MessageTemplate mt =
             MessageTemplate.or(mtCarControl, mtEventManagerControl);

	private SegmentAgent agent;
	
	private int previousServiceLevel = 0; // 'A';
	
	//Constructor
	public SegmentListenBehaviour(SegmentAgent agent) {
		previousServiceLevel = 0; // 'A';
		this.agent = agent;
	}

	@Override
	public void action() {

		ACLMessage msg = myAgent.receive(mt);

		if (msg != null) { //There is a message
			
			if (msg.getOntology().equals("carToSegmentOntology")) {

				JSONObject car = new JSONObject(msg.getContent());
				
				//Register
				if (msg.getConversationId().equals("update")) { 
					//Update position

					this.agent.updateCar(car);
				} else {
					if (msg.getConversationId().equals("register")) {
						// Register
						this.agent.addCar(car);						
					} else             
						this.agent.removeCar(car.getString("id"));
					
					Segment segment = this.agent.getSegment();
					int numCars = this.agent.getCars().size();
					
					//Set the density
					double density = numCars/segment.getLength();
					segment.setDensity(density);
					
					//Set the service level
					int currentSL;
					if (density < 11) {						
						currentSL = 0; // 'A';
					} else if (density < 18) {
						currentSL = 1; // 'B';
					} else if (density < 26) {
						currentSL = 2; // 'C';
					} else if (density < 35) {
						currentSL = 3; // 'D';
					} else if (density < 43) {
						currentSL = 4; // 'E';
					} else 
						currentSL = 5; // 'F';
					
					ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
					msg2.setOntology("trafficDensityOntology");
					
					JSONObject densityData = new JSONObject();
					densityData.put("density", density);
					msg2.setContent(densityData.toString());
					
					for (String id:agent.getCars().keySet()) {
						msg2.addReceiver(new AID(id, true));;			
					}
					agent.send(msg2);	
					
					if (currentSL != previousServiceLevel) {
						//Store current data of the edge in the list of 
						// previous data
//						Edge myEdge = agent.getSegment().getMyEdge();
//						myEdge.getEdgeDataList().add(
//								new EdgeData(previousServiceLevel,
//										agent.getJgrapht().getEdgeWeight(myEdge),
//										agent.getTini(),
//										car.getLong("tick")	
//								));
						agent.setTini(car.getLong("tick"));
						//Change the weight of this edge in the 
						//  jgrapht
						//Start computing the average speed
						double averageSpeed = 0.0;
						for(CarData cd:agent.getCars().values()) {
							averageSpeed += cd.getCurrentSpeed();
						}
						averageSpeed = averageSpeed / numCars;
						//Update the weight in the jgraph of the map
						agent.getJgrapht().setEdgeWeight(
								agent.getSegment().getMyEdge(),
								segment.getLength() /  averageSpeed );

						previousServiceLevel = currentSL;
						agent.getSegment().setCurrentServiceLevel(currentSL);
					}
				}
				
			} else if (msg.getOntology().
					        equals("eventManagerToSegmentOntology")) {				
				Segment segment = this.agent.getSegment();				
				char serviceLevel = msg.getContent().charAt(0);
				segment.setCurrentServiceLevel(serviceLevel);
			}
			
		} else block();
	}


	@Override
	public boolean done() {

		return false;
	}
}
