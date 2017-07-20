package behaviours;

import org.json.JSONObject;
import org.json.ToJSON;

import agents.CarAgentV2I;
import environment.Segment;
import environment.Step;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This behaviour is used by the CarAgentV2I and calculates the next 
 * graphical position of the car. It also registers and deregisters
 * the car from the segments.
 * 
 * The car is registered when it enters a new segment and deregistered
 * when it leaves a segment. Updating its position when moving on the
 * road.
 *
 */
public class CarBehaviourV2I extends CyclicBehaviour {

	private CarAgentV2I carAgentV2I;
	private AID topic;
	private boolean done = false;
	private boolean drawGUI;
	private long previousTick;
	//For recording routing total time
	private long tstart;
	//For recording how many km has been covered yet of the current
	// Step
	private float stepDistanceCovered;
	private float proportion;

	public CarBehaviourV2I(CarAgentV2I a, long timeout, boolean drawGUI) {

		this.carAgentV2I = a;
		this.drawGUI = drawGUI;
		this.topic = null;
		tstart = carAgentV2I.getTini();
		previousTick = tstart - 1;
		stepDistanceCovered = 0f;
		
		try {
			TopicManagementHelper topicHelper =(TopicManagementHelper) 
				this.carAgentV2I.getHelper(TopicManagementHelper.
						                                SERVICE_NAME);
			topic = topicHelper.createTopic("tick");
			topicHelper.register(topic);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void action() {

		//Block until tick is received
		ACLMessage msg = 
				myAgent.receive(MessageTemplate.MatchTopic(topic));

		if (msg != null) {
			
			long currentTick = Long.parseLong(msg.getContent());

			//If I still have to move somewhere
			if(this.carAgentV2I.getPath().getGraphicalPath().size()>0){
				//Get the path
				Step currentStep = this.carAgentV2I.getPath().
						                     getGraphicalPath().get(0);
				// First calculate the currentSpeed,Greenshield model
				int currentSpeed = (int) Math.min(
				     carAgentV2I.getMaxSpeed(),
				     (carAgentV2I.getCurrentTrafficDensity() >= 28.2)? 5:
				     carAgentV2I.getCurrentSegment().getMaxSpeed() *
	                 (1-carAgentV2I.getCurrentTrafficDensity()/28.2));
				
				carAgentV2I.setCurrentSpeed(currentSpeed);
				
				float currentPk = this.carAgentV2I.getCurrentPk();

				//TODO: Revise formula to compute pkIncrement
				float deltaTime = (currentTick - previousTick) / 3600f;
				float deltaPk = (float) currentSpeed * deltaTime;
//				System.out.println("PKCurrent: " + currentPk);
				//Compute distance covered
				float graphCovered = deltaPk * 
						        currentStep.getStepGraphicalLength() /
						        currentStep.getStepLength();
				//Virtual position
				float currentX = this.carAgentV2I.getX();
				float currentY = this.carAgentV2I.getY();
				//Update step distance covered
				stepDistanceCovered += deltaPk;

				//Check if we need to go to the next step
				while (stepDistanceCovered > 
				       currentStep.getStepLength()) {
					stepDistanceCovered -= currentStep.getStepLength();
					//If there is still a node to go
					if (this.carAgentV2I.getPath().
							getGraphicalPath().size() > 1) {
						//Remove the already run path
						this.carAgentV2I.getPath().getGraphicalPath().
						                     remove(0);
						currentStep = this.carAgentV2I.getPath().
								getGraphicalPath().get(0);
						currentX = currentStep.getOriginX();
						currentY = currentStep.getOriginY();				
					} else {
						this.kill();
						break;
					}
				}

				if (!this.done) {
					//Proportion inside the segment
//					float proportion = stepDistanceCovered / 
//							           currentStep.getStepLength();
					
					//Update the current pk when updating the x and y
					if("up".compareTo(this.carAgentV2I.getCurrentSegment().
							getDirection()) == 0){
						this.carAgentV2I.setCurrentPk(currentPk + stepDistanceCovered);
					} else {
						this.carAgentV2I.setCurrentPk(currentPk - stepDistanceCovered);
					}
					proportion = graphCovered / currentStep.getStepGraphicalLength();
					this.carAgentV2I.setX(currentX + proportion * 
							(currentStep.getDestinationX() - currentStep.getOriginX()));
					this.carAgentV2I.setY(currentY + proportion * 
							(currentStep.getDestinationY() - currentStep.getOriginY()));
//					this.carAgentV2I.setX(((1 - proportion) * currentX + 
//							proportion * currentStep.getDestinationX()));
//					this.carAgentV2I.setY(((1 - proportion) * currentY + 
//							proportion * currentStep.getDestinationY()));

					//If I am in a new segment
					if (!this.carAgentV2I.getCurrentSegment().
							equals(currentStep.getSegment())) {

						//Deregister from previous segment
						this.informSegment(
							this.carAgentV2I.getCurrentSegment(), 
							"deregister");
						//Set the new current segment
						this.carAgentV2I.
						     setCurrentSegment(currentStep.getSegment());
						//Register in the new segmentAgent
						this.informSegment(currentStep.getSegment(),
								           "register");

						carAgentV2I.setCurrentPk(currentStep.getSegment().getPkIni());
						if("up".compareTo(this.carAgentV2I.getCurrentSegment().
								getDirection()) == 0){
							this.carAgentV2I.setCurrentPk(currentPk + stepDistanceCovered);
						} else {
							this.carAgentV2I.setCurrentPk(currentPk - stepDistanceCovered);
						}

						if (this.carAgentV2I.isSmart()) {
						    this.carAgentV2I.recalculate(
								this.carAgentV2I.getCurrentSegment().
								           getOrigin().getId());
						}
					}

					this.informSegment(currentStep.getSegment(), "update");
					previousTick = currentTick;
				}
			}
		} else block();
	}

	//This method will send a message to a given segment
	private void informSegment(Segment segment, String type) {

		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setOntology("carToSegmentOntology");
		msg.setConversationId(type);
		msg.addReceiver(segment.getSegmentAgent().getAID());
		JSONObject carDataRegister = new JSONObject();
		carDataRegister.put("id", this.carAgentV2I.getId());
		carDataRegister.put("x", this.carAgentV2I.getX());
		carDataRegister.put("y", this.carAgentV2I.getY());
		carDataRegister.put("currentSpeed",
				this.carAgentV2I.getCurrentSpeed()); 
		msg.setContent(carDataRegister.toString());
		myAgent.send(msg);
	}

	public void kill() {

		//Done flag
		this.done = true;
		//Deregister from previous segment
		this.informSegment(this.carAgentV2I.getCurrentSegment(),
				           "deregister");

		//Delete the car from the canvas
		if (this.carAgentV2I.getInterfaceAgent() != null && this.drawGUI) {

			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setOntology("deleteCarOntology");
			msg.addReceiver(this.carAgentV2I.getInterfaceAgent().getName());
			msg.setContent(ToJSON.toJSon("id",this.carAgentV2I.getId()));

			myAgent.send(msg);
		}
		
		//Deregister the carAgentV2I
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(this.carAgentV2I.getAID());
		
		try {
			DFService.deregister(this.carAgentV2I,  dfd);
		} catch (Exception e) { 
		}
		//Send information about this car to the AgentLog
		//TODO:currentTick-tstart: total routing time,
		//     TypeOfAlgorithm,
		//     InitialIntersection, finalIntersection
		//     Path performed
		//     #routing changes
		//     ...
		this.carAgentV2I.doDelete();
	}
}