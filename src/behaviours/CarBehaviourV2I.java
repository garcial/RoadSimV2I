package behaviours;

import org.json.JSONObject;

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
	private long currentTick;
	// Step
	private float stepDistanceCovered;
	private float proportion;

	public CarBehaviourV2I(CarAgentV2I a, long timeout, boolean drawGUI) {

		this.carAgentV2I = a;
		this.drawGUI = drawGUI;
		this.topic = null;
		previousTick = carAgentV2I.getTini() - 1;
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
			
			currentTick = Long.parseLong(msg.getContent());
			carAgentV2I.getCarData().setCurrentTick(currentTick);

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
				
				carAgentV2I.getCarData().setCurrentSpeed(currentSpeed);
				
				float currentPk = this.carAgentV2I.getCurrentPk();

				//Compute distance covered
				float deltaTime = (currentTick - previousTick) / 3600f;
				float deltaPk = (float) currentSpeed * deltaTime; // real
				float graphCovered = deltaPk * 					  // graphical
						        currentStep.getStepGraphicalLength() /
						        currentStep.getStepLength();
				//Virtual position
				float currentX = this.carAgentV2I.getCarData().getX();
				float currentY = this.carAgentV2I.getCarData().getY();
				//Update step distance covered
				stepDistanceCovered += deltaPk;
				carAgentV2I.getCarData().incSegmentDistanceCovered(deltaPk);
				carAgentV2I.getCarData().incTripDistanceCovered(deltaPk);

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
					
					//Update the current pk when updating the x and y
					if("up".compareTo(this.carAgentV2I.getCurrentSegment().
							getDirection()) == 0){
						this.carAgentV2I.setCurrentPk(currentPk + stepDistanceCovered);
					} else {
						this.carAgentV2I.setCurrentPk(currentPk - stepDistanceCovered);
					}
					proportion = graphCovered / currentStep.getStepGraphicalLength();
					this.carAgentV2I.getCarData().setX(currentX + proportion * 
							(currentStep.getDestinationX() - currentStep.getOriginX()));
					this.carAgentV2I.getCarData().setY(currentY + proportion * 
							(currentStep.getDestinationY() - currentStep.getOriginY()));

					//If I am in a new segment
					if (!this.carAgentV2I.getCurrentSegment().
							equals(currentStep.getSegment())) {

						//delete the surplus of km added to the previous segment 
						carAgentV2I.getCarData().
						        incSegmentDistanceCovered(-stepDistanceCovered);
						System.out.println(carAgentV2I.getLocalName() +
								";" + carAgentV2I.getCarData().getSegmentDistanceCovered() +
								";" + carAgentV2I.getCurrentSegment().getLength() +";");
						//Deregister from previous segment
						this.informSegment(
							this.carAgentV2I.getCurrentSegment(), 
							"deregister");
						//Set the new current segment
						this.carAgentV2I.
						     setCurrentSegment(currentStep.getSegment());
						this.carAgentV2I.getCarData().
						     setSegmentDistanceCovered(stepDistanceCovered);
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
		msg.setContent(carAgentV2I.getCarData().toJSON().toString());
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
			msg.setContent(carAgentV2I.getCarData().toJSON().toString());

			myAgent.send(msg);
		}
		System.out.println(carAgentV2I.getLocalName() +
				";" + carAgentV2I.getCarData().getSegmentDistanceCovered() +
				";" + carAgentV2I.getCurrentSegment().getLength() +";");
		System.out.println(carAgentV2I.getLocalName() + ";;;" +
		                  carAgentV2I.getCarData().getTripDistanceCovered());
		if (carAgentV2I.getUseLog()) {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setOntology("carToLog");
			msg.addReceiver(carAgentV2I.getLogAgent().getName());
			JSONObject data = new JSONObject();
			data.put("id", carAgentV2I.getLocalName());
			data.put("time", currentTick - carAgentV2I.getTini());
			data.put("type", carAgentV2I.getTypeOfAlgorithm());
			data.put("distance", carAgentV2I.getCarData().getTripDistanceCovered());
			data.put("tini", carAgentV2I.getTini());
			data.put("tfin", currentTick);
			msg.setContent(data.toString());
			carAgentV2I.send(msg);
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