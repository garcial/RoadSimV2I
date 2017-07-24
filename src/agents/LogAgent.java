package agents;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.json.JSONObject;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class LogAgent extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6553357997493246057L;
	private File dataAgentsFile;
	private File dataSegmentsFile;
	
	protected void setup(){
		//Register the agent
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("logAgent");
		sd.setName(getLocalName());

		dfd.addServices(sd);
		try {
			DFService.register(this,  dfd);
		} catch (FIPAException fe) {
			this.takeDown();
		}
		
		setDataAgentsFile(new File((String) 
				          this.getArguments()[0] + "dataAgents.csv"));
		setDataSegmentsFile(new File((String) 
		          this.getArguments()[0] + "dataSegments.csv"));
		try {
			Files.write(Paths.get(getDataAgentsFile().getName()),
				   "Name\tTime\tType\tDistance\n".getBytes());
			Files.write(Paths.get(getDataSegmentsFile().getName()),
				   ("Name\tTime\tMaxSpeed\tCurrentSpeed\t" +
			        "Shortest\tFastest\tStartSmart\tDynamicSmart\n").getBytes());
		}catch (IOException e) {
			e.printStackTrace();
		}
		addBehaviour(new CyclicBehaviour() {
			
			/**
			 * Receive three types of messages: 
			 * One from whatever carAgent,
			 * other from whatever segmentAgent, and
			 * the message to close files from the tickAgent
			 */
			private static final long serialVersionUID = 1L;
			private MessageTemplate mtCar = MessageTemplate.and(
					  MessageTemplate.MatchOntology("carToLog"),
					  MessageTemplate.MatchPerformative(ACLMessage.INFORM));
			private MessageTemplate mtSegment = MessageTemplate.and(
					  MessageTemplate.MatchOntology("segmentToLog"),
					  MessageTemplate.MatchPerformative(ACLMessage.INFORM));
			private MessageTemplate mt = MessageTemplate.or(
		            		 mtCar, 
		            		 mtSegment);

			@Override
			public void action() {
				ACLMessage msg = myAgent.receive(mt);

				if (msg != null) { //There is a message
					
					if (msg.getOntology().equals("carToLog")) {
						JSONObject car = new JSONObject(msg.getContent());
						try {
							StringBuilder data = new StringBuilder(car.getString("id"));
							data.append("\t");
							data.append(car.getLong("time"));
							data.append("\t");
							data.append(car.getInt("type"));
							data.append("\t");
							data.append((float)car.getDouble("distance"));
							data.append("\n");
							Files.write(Paths.get(getDataAgentsFile().getName()),
									   data.toString().getBytes(),
									   StandardOpenOption.APPEND);
						} catch (IOException e) {
							System.out.println("Error en log " + car.toString());
							e.printStackTrace();
						}
					} else if (msg.getOntology().equals("segmentToLog")) {
						JSONObject segment = new JSONObject(msg.getContent());
						try {
							StringBuilder data = new StringBuilder(segment.getString("id"));
							data.append("\t");
							data.append(segment.getLong("time"));
							data.append("\t");
							data.append(segment.getInt("maxSpeed"));
							data.append("\t");
							data.append(segment.getInt("currentSpeed"));
							data.append("\t");
							data.append(segment.getLong("shortest"));
							data.append("\t");
							data.append(segment.getLong("fastest"));
							data.append("\t");
							data.append(segment.getLong("startSmart"));
							data.append("\t");
							data.append(segment.getLong("dynamicSmart"));
							data.append("\n");
							Files.write(Paths.get(getDataSegmentsFile().getName()),
									data.toString().getBytes(),
									StandardOpenOption.APPEND);
						} catch (IOException e) {
							System.out.println("Error en log " + segment.toString());
							e.printStackTrace();
						}
					}
				}
			}
		});
	}

	public File getDataAgentsFile() {
		return dataAgentsFile;
	}

	public void setDataAgentsFile(File dataAgentsFile) {
		this.dataAgentsFile = dataAgentsFile;
	}

	public File getDataSegmentsFile() {
		return dataSegmentsFile;
	}

	public void setDataSegmentsFile(File dataSegmentsFile) {
		this.dataSegmentsFile = dataSegmentsFile;
	}
	

}

