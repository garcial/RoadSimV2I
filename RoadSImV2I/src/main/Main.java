package main;

import java.io.IOException;
import environment.Map;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

/**
 * Main program, it creates everything.
 *
 */
public class Main {
	
	//Initial tick length, this value is ignored if the GUI is drawn
	private static final long tickLength = 1L;
	
	//Start at specific tick: 7:59 that in seconds is ..
	private static final long startingTick = 17*3600 + 59*60;
	
	//Finish the simulation at specific tick: 00:00
	private static final long finishingTick = 21*3600;
	
	//Draw the GUI
	private static final boolean drawGUI = true;
	
	//Start the RMA
	private static final boolean startRMA = false;
	
	//Activate segment logging
	private static final boolean segmentLogging = false;
	
	//Logging directory for the segments
	private static final String loggingDirectory = 
			"/home/usuario/Documents/SimulationResults";

	public static void main(String[] args) {
		
		Map map = null;

		//Get a hold on JADE runtime
		jade.core.Runtime rt = jade.core.Runtime.instance();

		//Exit the JVM when there are no more containers around
		rt.setCloseVM(true);

		//Create a profile for the main container
		Profile profile = new ProfileImpl(null, 1099, null);
		profile.setParameter(Profile.CONTAINER_NAME, 
				             "Main container");
		
		/*
		 * This should make the program go smoother
		 */
		//How many threads will be in charge of delivering the  
		//   messages, maximum 100, default 5
		profile.setParameter(
				"jade_core_messaging_MessageManager_poolsize",
				"100");
		
		/*
		 * This is needed because when the MessageManager fills up, it 
		 * slows down all the agents, so to achieve a good performance  
		 * we make the queue bigger.
		 */
		//Size of the message queue, default 100000000 (100Mb), now  
		//    the maximum size we can
		profile.setParameter(
				"jade_core_messaging_MessageManager_maxqueuesize", 
				Integer.toString(Integer.MAX_VALUE));
		
		/*
		 * This is just so that the program does not bother us with
		 *    warnings
		 */
		profile.setParameter(
				"jade_core_messaging_MessageManager_warningqueuesize",
				Integer.toString(Integer.MAX_VALUE));
		
		//Default 1000ms, now 5000ms
		profile.setParameter(
		  "jade_core_messaging_MessageManager_deliverytimethreshold",
		  "5000");

		/*
		 * This is needed because the TimeKeeperAgent has to search 
		 * for more than 100 agents
		 */
		//By default, the maximum number of returned matches by the DF
		//   is 100 this makes it larger
		profile.setParameter("jade_domain_df_maxresult", "10000");
		
		/*
		 * This activates the Topic service, which allows us to 
		 *  "broadcast" messages.
		 * It will be activated in all containers
		 */
		profile.setParameter(
				Profile.SERVICES, 
				"jade.core.messaging.TopicManagementService");
		
		//Container that will hold the agents
		jade.wrapper.AgentContainer mainContainer = 
				                     rt.createMainContainer(profile);

		//Start RMA
		if (startRMA) {
			try {
				AgentController agent = mainContainer.createNewAgent(
						           "rma",
						           "jade.tools.rma.rma", 
						           new Object[0]);

				agent.start();

			} catch (StaleProxyException e1) {

				System.out.println("Error starting the rma agent");
				e1.printStackTrace();
			}
		}

		//We will use a container only for the segments
		profile = new ProfileImpl(null, 1099, null);
		profile.setParameter(Profile.CONTAINER_NAME, 
				            "Segment container");
		
		/*
		 * This activates the Topic service, which allows us to 
		 *     "broadcast" messages
		 */
		profile.setParameter(Profile.SERVICES, 
				        "jade.core.messaging.TopicManagementService");

		//Container that will hold the agents
		jade.wrapper.AgentContainer segmentContainer = 
				                     rt.createAgentContainer(profile);

		//Load the map (including the related segmentAgents)
		try {
			map = new Map("staticFiles/map", segmentContainer,
					      segmentLogging, loggingDirectory, drawGUI);
		} catch (IOException e) {
			System.out.println("Error reading the maps file.");
			e.printStackTrace();
		}
		
		//Create a profile for the car container
		profile = new ProfileImpl(null, 1099, null);
		profile.setParameter(Profile.CONTAINER_NAME, "Car container");
		
		/*
		 * This activates the Topic service, which allows us to 
		 *    "broadcast" messages
		 */
		profile.setParameter(Profile.SERVICES,
				        "jade.core.messaging.TopicManagementService");

		//Container that will hold the agents
		jade.wrapper.AgentContainer carContainer = 
				                     rt.createAgentContainer(profile);
		
		// Wait for 1 second to finishing the starting all the 
		//    containers (by the flyes)
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e3) {
			e3.printStackTrace();
		}
		
		//Interface if is necesary
		if(drawGUI){
			try {

			AgentController agent = 
					mainContainer.createNewAgent("interfaceAgent",
							             "agents.InterfaceAgent", 
							             new Object[]{map, drawGUI});

				agent.start();

			} catch (StaleProxyException e) {

				System.out.println("Error starting the interface");
				e.printStackTrace();
			}
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
		
		//EventManager
		try {
			AgentController agent = 
					mainContainer.createNewAgent("eventManagerAgent",
							"agents.EventManagerAgent", 
							new Object[]{map, carContainer, 
									     segmentContainer,
									    "staticFiles/events", 
									    startingTick, drawGUI});
			agent.start();
		} catch (StaleProxyException e1) {
			System.out.println(
					        "Error starting the EventManager agent");
			e1.printStackTrace();
		}
		
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e2) {
//			e2.printStackTrace();
//		}
		
		//TimeKeeper
		try {
			AgentController agent = 
					mainContainer.createNewAgent("timeKeeperAgent",
							"agents.TimeKeeperAgent",
							new Object[]{drawGUI,tickLength,
									startingTick, finishingTick});
			agent.start();
		} catch (StaleProxyException e1) {
			System.out.println("Error starting the TimeKeeper agent");
			e1.printStackTrace();
		}

	}
}
