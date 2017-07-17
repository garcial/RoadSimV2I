package behaviours;

import javax.swing.SwingUtilities;

import org.json.JSONObject;

import agents.InterfaceAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This behaviour is used by the InterfaceAgent and adds a new car to 
 *  the GUI and executes a behaviour to update the speed of the car.
 *
 */
public class InterfaceAddCarBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = 1L;

	//Template to listen for the new communications from cars
	private MessageTemplate mtNewCar = 
			MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchOntology("newCarOntology"));
	
	private InterfaceAgent agent;
	
	public InterfaceAddCarBehaviour(InterfaceAgent a) {
		
		this.agent = a;
	}

	@Override
	public void action() {

		ACLMessage msg = myAgent.receive(mtNewCar);
		
		if (msg != null) {

			JSONObject cont = new JSONObject(msg.getContent());

			final String id = cont.getString("id");
			final float x = (float) cont.getInt("x");
			final float y = (float) cont.getInt("y");
			final int algorithmType = cont.getInt("algorithmType");
			
			//Add the car to the scene
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					agent.getMap().addCar(myAgent.getLocalName(), id,
							              algorithmType, x, y, false);
				}
			});
			
		} else block();
	}
}