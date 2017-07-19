package behaviours;

import java.util.HashMap;

import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONObject;

import agents.InterfaceAgent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import view.CanvasWorld.MapPanel.Mobile;

public class InterfaceDrawBehaviour extends Behaviour {

	private static final long serialVersionUID = 5169881140236331658L;

	private InterfaceAgent agent;

	//Template to listen for drawing instructions
	private MessageTemplate mt = MessageTemplate.and(MessageTemplate.
			                    MatchPerformative(ACLMessage.INFORM),
			MessageTemplate.or(MessageTemplate.or(MessageTemplate.or(
			 MessageTemplate.or(
			 MessageTemplate.MatchOntology("drawOntology"),
			 MessageTemplate.MatchOntology("logOntology")),
			 MessageTemplate.MatchOntology("deleteCarOntology")),
			 MessageTemplate.MatchOntology("updateTimeOntology")),
			 MessageTemplate.MatchOntology("numberOfCarsOntology")));

	public InterfaceDrawBehaviour(InterfaceAgent agent) {

		this.agent = agent;
	}

	@Override
	public void action() {

		//Receive the drawing instructions
		ACLMessage msg = myAgent.receive(mt);

		if (msg != null) {
			
			if (msg.getOntology().equals("drawOntology")) {

				//Update the position in the canvas
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						HashMap<String, Mobile> cars = 
								         agent.getMap().getCars();					
						JSONObject cont = 
								    new JSONObject(msg.getContent());
						JSONArray list = cont.getJSONArray("cars");
						for(int i = 0; i < list.length(); i++){
							JSONObject obj = list.getJSONObject(i);
							Mobile m = cars.get(obj.get("id"));
							
							if (m != null) {

								m.setX((float) obj.getDouble("x"));
								m.setY((float) obj.getDouble("y"));
							}
						}

						agent.getMap().setCars(cars);
					}
				});
			} else if (msg.getOntology().equals("deleteCarOntology")){

				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						agent.getMap().deleteCar(msg.getContent());	
					}
				});

			} else if (msg.getOntology().
					             equals("updateTimeOntology")) {

				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {

						agent.getMap().setTime(msg.getContent());	
					}
				});
			} else if (msg.getOntology().equals("logOntology")) {

				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {

						agent.getMap().appendText(msg.getContent());	
					}
				});

			} else if (msg.getOntology().
					              equals("numberOfCarsOntology")) {
				
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						JSONObject numberOfCarsData = 
								  new JSONObject(msg.getContent());
						int numberOfCars = (int) 
								numberOfCarsData.get("numberOfCars");
						agent.getMap().setNumberOfCars(numberOfCars);	
					}
				}); 
			}
			
		} else block();
	}

	@Override
	public boolean done() {

		return false;
	}
}
