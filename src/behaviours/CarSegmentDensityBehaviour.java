package behaviours;

import org.json.JSONObject;

import agents.CarAgentV2I;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CarSegmentDensityBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = 7963143335893634038L;
	
	MessageTemplate mt = MessageTemplate.
			                  MatchOntology("trafficDensityOntology");
	
	private CarAgentV2I myCarAgent;
	public CarSegmentDensityBehaviour(CarAgentV2I myCarAgent) {
		super();
		this.myCarAgent = myCarAgent;
	}
	@Override
	public void action() {
		ACLMessage msg = myAgent.receive(mt);
		if (msg!= null) {
			JSONObject densityData = new JSONObject(msg.getContent());
			myCarAgent.setCurrentTrafficDensity(
					                densityData.getDouble("density"));
		} else block();
	}

}
