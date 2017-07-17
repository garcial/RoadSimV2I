package agents;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This agent is in charge of sending a Tick message to the rest of
 *  the agents.
 * 
 * EvenManagerAgent, CarAgents and SegmentAgents need a tick to
 *  start its behaviour, otherwise they won't do anything.
 *
 */
public class TimeKeeperAgent extends Agent {

	private static final long serialVersionUID = 4546329963020795810L;
	private long tickLength, currentTick, finishingTick;
	private boolean drawGUI;
	private List<AID> agents = new ArrayList<AID>();
	private DFAgentDescription interfaceAgent;

	protected void setup() {

		//Register the service
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("timeKeeperAgent");
		sd.setName(getLocalName());

		dfd.addServices(sd);
		try {
			DFService.register(this,  dfd);
		} catch (FIPAException fe) { 
			fe.printStackTrace(); 
		}

		// Decide is is necessary draw the interface 
		this.drawGUI = (boolean) this.getArguments()[0];
		
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

			this.interfaceAgent = result[0];
		}

		//Get the ticklength
		this.tickLength = (long) this.getArguments()[1];

		//Start at specific tick
		this.currentTick = (long) this.getArguments()[2];

		//End the simulation at specific tick
		this.finishingTick = (long) this.getArguments()[3];

		//A reference to myself
		TimeKeeperAgent timeKeeperAgent = this;

		//Create the tick topic, but do not register to it
		AID topic = null;
		try {
			TopicManagementHelper topicHelper = (TopicManagementHelper) 
					     getHelper(TopicManagementHelper.SERVICE_NAME);
			topic = topicHelper.createTopic("tick");

		} catch (Exception e) {
			System.err.println("Agent " + getLocalName() + 
					          ": ERROR creating topic \"tick\"");
			e.printStackTrace();
		}
		// It is a spetial AID with the topic "tick". There is no 
		//    concern about which agent holds it.
		final AID finalTopic = topic;

		// add forever behaviour
		addBehaviour(new Behaviour() {

			private static final long serialVersionUID = 1L;

			@Override
			public void action() {

				if (currentTick == finishingTick) {

					// Stops the entire app, not just this agent. 
					// The value 0 states right finalization
					System.exit(0);
				}

				try {

					// Sleeps for TickLength miliseconds
					Thread.sleep(timeKeeperAgent.getTickLength());
				} catch (InterruptedException e) { 
					System.out.println("Bye"); 
				}

				timeKeeperAgent.currentTick++;

				//Send the topic "tick"
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				// if the delivery of a message fails, no failure
				//     handling action must be performed.
				msg.addUserDefinedParameter(
						ACLMessage.IGNORE_FAILURE, "true");
				// if the delivery of a message fails, no FAILURE 
				//    notification has to be sent back to the sender.				
				msg.addUserDefinedParameter(
						ACLMessage.DONT_NOTIFY_FAILURE, "true");
				// This message must be stored for a given timeout 
				//     (in ms) in case it is sent to/from a  
				//     temporarily disconnected split container. After
				//      that timeout a FAILURE message will be sent 
				//      back to the sender.
				// 0 means store and forward disabled -1 is infinite 
				//     timeout
				msg.addUserDefinedParameter(ACLMessage.SF_TIMEOUT,
						                    "-1");
				msg.addReceiver(finalTopic);
				System.out.println("TimeKeeperAgent: " + timeKeeperAgent.currentTick);
				msg.setContent(Long.toString(timeKeeperAgent.
						                               currentTick));
				myAgent.send(msg);

				//If is necesary draw the GUI
				if (timeKeeperAgent.getDrawGUI()) {
					//Send the number of cars to the interface agent
					//Search for cars that are currently in the DF
					DFAgentDescription[] cars = null;

					DFAgentDescription dfd = new DFAgentDescription();
					ServiceDescription sd  = new ServiceDescription();
					sd.setType("carAgent");
					dfd.addServices(sd);

					try {
						cars = DFService.search(
						  timeKeeperAgent, getDefaultDF(), dfd, null);
					} catch (FIPAException e) { e.printStackTrace(); }


					if (cars != null) {
						msg = new ACLMessage(ACLMessage.INFORM);
						msg.addReceiver(interfaceAgent.getName());
						msg.setOntology("numberOfCarsOntology");
						JSONObject numberOfCars = new JSONObject();
						numberOfCars.put("numberOfCars", cars.length);					
						msg.setContent(numberOfCars.toString());
						myAgent.send(msg);

					}
				}
			}

			@Override
			public boolean done() {
				return false;
			}
		});

		/**
		 * End of test code
		 */

		if(this.drawGUI){
			//Check for tickLeght changes
			addBehaviour(new Behaviour() {

				private static final long serialVersionUID = 
						8455875589611369392L;

				MessageTemplate mt = MessageTemplate.and(
						MessageTemplate.MatchPerformative(
								ACLMessage.INFORM), 
						MessageTemplate.MatchOntology(
								"changeTickLengthOntology"));


				@Override
				public void action() {
					ACLMessage msg = myAgent.receive(mt);


					if (msg != null) {
						JSONObject messageData = 
								    new JSONObject(msg.getContent()); 
						Long tickLength = 
							   (long) (messageData.getInt("idTick"));
						System.out.println(tickLength);
						((TimeKeeperAgent)this.myAgent).
						setTickLength(tickLength);

					} else block();
				}

				@Override
				public boolean done() {
					return false;
				}

			});
		}

	}

	//Setters and getters
	public List<AID> getAgents() {
		return agents;
	}

	public long getTickLength() {

		return this.tickLength;
	}

	public void setTickLength(long newTick) {

		this.tickLength = newTick;
	}
	
	public boolean getDrawGUI(){
		return this.drawGUI;
	}
}