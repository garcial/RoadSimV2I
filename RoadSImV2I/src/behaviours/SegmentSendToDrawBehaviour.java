package behaviours;

import agents.SegmentAgent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This is the behaviour that sends the data to draw to the interface,
 * it has all the information about the cars that belong to a segment.
 *
 */
public class SegmentSendToDrawBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID =-6962886464372429902L;

	private SegmentAgent agent;
	private DFAgentDescription interfaceAgent;
	private AID topic;

	public SegmentSendToDrawBehaviour(SegmentAgent agent) {

		this.agent = agent;

		//Find the interface agent
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("interfaceAgent");
		dfd.addServices(sd);

		DFAgentDescription[] result = null;

		try {
			result = DFService.searchUntilFound(
					this.agent, this.agent.getDefaultDF(), 
					dfd, null, 5000L);
		} catch (FIPAException e) { e.printStackTrace(); }

		this.interfaceAgent = result[0];
		
		this.topic = null;
		
		try {
			TopicManagementHelper topicHelper = 
					(TopicManagementHelper) this.agent.
					    getHelper(TopicManagementHelper.SERVICE_NAME);
			topic = topicHelper.createTopic("tick");
			topicHelper.register(topic);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void action() {

		//Block until tick is received
		ACLMessage msg = myAgent.receive(
				                   MessageTemplate.MatchTopic(topic));

		if (msg != null) {
			
			//Log
			if (this.agent.getSegment().isSegmentLogging()) {
				
				this.agent.doLog(Long.parseLong(msg.getContent()));
			}

			if (this.agent.carsSize() > 0) {

				//Send the data to the InterfaceAgent
				msg = new ACLMessage(ACLMessage.INFORM);
				msg.setOntology("drawOntology");
				msg.addReceiver(this.interfaceAgent.getName());
				msg.setContent(this.agent.getDrawingInformation());

				myAgent.send(msg);
			}
		} else block();
	}
}
