package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import javax.swing.SwingUtilities;

import org.json.JSONObject;

import behaviours.InterfaceAddCarBehaviour;
import behaviours.InterfaceDrawBehaviour;
import environment.Map;
import view.CanvasWorld;


/**
 * This agent has the GUI and receives all the mesages to draw to it
 *
 */
public class InterfaceAgent extends Agent{

	private static final long serialVersionUID = 1L;

	public static final int MAXWORLDX = 1100;
	public static final int MAXWORLDY = 720;

	private CanvasWorld map;
	
	private DFAgentDescription timeKeeperAgent;

	protected void setup() {

		//Register the service
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("interfaceAgent");
		sd.setName(getLocalName());

		dfd.addServices(sd);
		try {
			DFService.register(this,  dfd);
		} catch (FIPAException fe) { 
			fe.printStackTrace(); 
		}

		//Find the TimeKeeperAgent agent
		dfd = new DFAgentDescription();
		sd = new ServiceDescription();
		sd.setType("timeKeeperAgent");
		dfd.addServices(sd);

		DFAgentDescription[] result = null;

		try {
			result = DFService.searchUntilFound(
					this, getDefaultDF(), dfd, null, 5000);
		} catch (FIPAException e) { e.printStackTrace(); }

		this.timeKeeperAgent = result[0];

		//Check if I need to create the GUI, for testing purposes
		boolean drawGUI = (boolean) this.getArguments()[1];

		if (drawGUI) {

			//Get the map from an argument
			Map graphicalMap = (Map) this.getArguments()[0];
			
			InterfaceAgent me = this;

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					map = new CanvasWorld(me, MAXWORLDX, MAXWORLDY,
							              graphicalMap);	
				}
			});

			//Launch the behaviour that will add cars
			addBehaviour(new InterfaceAddCarBehaviour(this));

			//This will listen for drawing instructions
			addBehaviour(new InterfaceDrawBehaviour(this));
		}
	}

	/**
	 * Send a message to the TimeKeeperAgent to change its tickLength
	 * 
	 * @param newTick New value of the tick
	 */
	public void setTick(int newTick) {

		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setOntology("changeTickLengthOntology");
		msg.addReceiver(this.timeKeeperAgent.getName());
		JSONObject jsonmessage = new JSONObject();
		jsonmessage.put("idTick", newTick);
		System.out.println("idTick: " + jsonmessage.toString());
		//msg.setContent(Integer.toString(newTick));
		msg.setContent(jsonmessage.toString());
		this.send(msg);
	}

	//Setters and getters
	public CanvasWorld getMap() {
		return map;
	}
}
