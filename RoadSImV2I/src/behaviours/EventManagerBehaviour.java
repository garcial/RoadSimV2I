package behaviours;

import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import agents.EventManagerAgent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class EventManagerBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = 4537023518719689317L;

	private EventManagerAgent agent;
	private boolean drawGUI;
	private AID topic;

	public EventManagerBehaviour(EventManagerAgent agent, 
			                     boolean drawGUI) {

		this.agent = agent;
		this.drawGUI = drawGUI;
		this.topic = null;		
		try {
			TopicManagementHelper topicHelper =(TopicManagementHelper) 
				 this.agent.getHelper(
						          TopicManagementHelper.SERVICE_NAME);
			topic = topicHelper.createTopic("tick");
			topicHelper.register(topic);			
		}
		catch (Exception e) {
			// Fatal error starting eventManagerBeh
			e.printStackTrace();
		}
	}

	@Override
	public void action() {

		//Block until tick is received(defect 1 tick=1 simu. second)

		ACLMessage msg = myAgent.receive(
				                 MessageTemplate.MatchTopic(topic));
		if (msg != null) {
			int totalMinutes = ((int) Long.parseLong(msg.getContent()))/60;
			int hours = (int) (totalMinutes / 60);
			int minutes = (int) (totalMinutes % 60);
			
			//If the minute has changed, notify the interface
			if (minutes != this.agent.getPreviousMinute() && 
				this.drawGUI) {
				
				this.agent.setPreviousMinute(minutes);
				
				ACLMessage timeMsg =new ACLMessage(ACLMessage.INFORM);
				timeMsg.setOntology("updateTimeOntology");
				timeMsg.addReceiver(this.agent.getInterfaceAgent().
						                       getName());
				timeMsg.setContent(new JSONObject(
					"{hora:" + String.format("%02d",hours) + 
					", minutos:" +String.format("%02d", minutes)+"}").
						toString());
				myAgent.send(timeMsg);
			}

			//Increment the elapsed time
			//this.agent.incrementeTimeElapsed();
			
			HashMap<Long, List<String>> events = 
					                           this.agent.getEvents();
			long currentTick = Long.parseLong(msg.getContent());
			int counter = 0;
			//Check for events that need to be fired at this tick
			if (events.containsKey(currentTick)) {
				
				//Execute all the actions
				List<String> actions = events.get(currentTick);
				StringBuilder str = new StringBuilder();
								
				for (String string : actions) {
					
					String parts[] = string.split(",");
					
					if (parts[0].equals("newCar")) {
						
						try {
							AgentController agent = this.agent.
									getCarContainer().
									createNewAgent("car" + 
									Long.toString(currentTick) + 
									Integer.toString(counter), 
									"agents.CarAgent", new Object[]{
										this.agent.getMap(), 
			/* IntOrigin, IntDest */	parts[2], parts[3], 
			/* maxSpeed car */			Integer.parseInt(parts[4]),
			/* alg type */				parts[5],
			/* drawGUI */				this.drawGUI,
			/* Initial time */          currentTick, 
			/* sensor ratio */          10});

							agent.start();
							
							//For the logs
							str.append(parts[1] + ": Car from " +
							           parts[2]  + " to " + 
									   parts[3] +"\n");

						} catch (StaleProxyException e) {
							System.out.println(
									"Error starting carAgent");
							e.printStackTrace();
						}
					} else if (parts[0].equals("segment")) {
						
						msg = new ACLMessage(ACLMessage.INFORM);
						msg.setOntology(
								  "eventManagerToSegmentOntology");
						msg.addReceiver(
								  new AID(parts[2], AID.ISLOCALNAME));
						msg.setContent(parts[3]);
						
						myAgent.send(msg);
						
						//For the logs
						str.append(parts[1] + ": Segment " + parts[2] + 
								 " service changed to " + parts[3] + 
								 "\n");
					}
					
					counter++;
				}
				
				if(this.drawGUI){
					msg = new ACLMessage(ACLMessage.INFORM);
					msg.setOntology("logOntology");
					msg.addReceiver(this.agent.getInterfaceAgent().
							                   getName());
					msg.setContent(str.toString());

					myAgent.send(msg);
				}
			}
		} else block();
	}
}
