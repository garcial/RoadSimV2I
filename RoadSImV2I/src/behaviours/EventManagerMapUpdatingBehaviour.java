package behaviours;

import org.json.JSONObject;

import agents.EventManagerAgent;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class EventManagerMapUpdatingBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = 1L;
	EventManagerAgent myEventAgent;
	
	MessageTemplate mt = MessageTemplate.
			                  MatchOntology("mapUpdateOntology");


	public EventManagerMapUpdatingBehaviour(EventManagerAgent a) {
		super(a);
		myEventAgent = a;
	}

	@Override
	public void action() {
		ACLMessage msg = myAgent.receive(mt);
		if (msg!= null) {
			JSONObject mapData = new JSONObject(msg.getContent());
			myEventAgent.updateMap(mapData);
		} else block();
	}

}
