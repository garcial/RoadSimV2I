package environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.json.JSONArray;
import org.json.JSONObject;
import jgrapht.Edge;

/**
 * Class that holds the representation of a map.
 * 
 * It also has al the logic to read the map files and creates the 
 * {@link SegmentAgent}.
 *
 */
public class Map implements Serializable {

	private static final long serialVersionUID = 6521810168990354805L;

	@SuppressWarnings("unused")
	private Intersection start;
	private Integer intersectionCount;
	private Integer segmentCount;
	private List<Intersection> intersections;
	// JGRAPHT
	private DefaultDirectedWeightedGraph<Intersection, Edge> jgrapht;

	//The container where the segment agents will be created
	private transient jade.wrapper.AgentContainer mainContainer;
	
	//Parameters for the segments
	private boolean segmentLogging;
	private String loggingDirectory;
	HashMap<String, Segment> segmentsAux;
	HashMap<String, Edge> edgesAux;
	
	// Draw th GUI
	private boolean drawGUI;

	/**
	 * Constructor that builds a Map from a folder.
	 * 
	 * @param folder Folder where the files are stored.
	 */
	public Map(String folder, 
			   jade.wrapper.AgentContainer mainContainer,
			   boolean segmentLogging, String loggingDirectory, 
			   boolean drawGUI) 
		   throws IOException{

		//For the agents
		this.mainContainer = mainContainer;		
		this.segmentLogging = segmentLogging;
		this.loggingDirectory = loggingDirectory;
		this.jgrapht = 
				new DefaultDirectedWeightedGraph<Intersection, Edge>
		                (Edge.class);

		//Read the files
		this.intersectionCount = 0;
		this.segmentCount = 0;
		
		this.drawGUI = drawGUI;

		//Get all files from the given folder
		String url = Map.class.getClassLoader().getResource(folder).
				                                getPath();
		
		File[] files = new File(url).listFiles();

		//Check correct files
		BufferedReader intersectionsReader = null, 
				       segmentsReader = null, 
				       stepsReader = null;

		for(int i=0; i<files.length; i++){
			
			if(files[i].getName().equals("intersections")){

				intersectionsReader = new 
						BufferedReader(
						new FileReader(files[i].getAbsolutePath()));

			}else if(files[i].getName().equals("segments")){

				segmentsReader = new BufferedReader(
						new FileReader(files[i].getAbsolutePath()));

			}else if(files[i].getName().equals("steps")){

				stepsReader = new BufferedReader(
						new FileReader(files[i].getAbsolutePath()));
			}
		}

		if(segmentsReader == null || intersectionsReader == null || 
				stepsReader == null) {

			throw new IOException("Couldn't find the files.");
		} else {

			try {

				//This will be used later to append the segments in an
				//     efficient way
				HashMap<String, Intersection> intersectionsAux = new 
						              HashMap<String, Intersection>();

				String line = intersectionsReader.readLine();

				//Auxiliar structure
				this.intersections = new ArrayList<Intersection>();

				//Read  all the Intersections
				while(line != null){

					JSONObject inter = new JSONObject(line);

					Intersection intersection = new 
							Intersection(inter.getString("id"), 
							    inter.getJSONObject("coordinates").
							          getInt("x"),
								inter.getJSONObject("coordinates").
								      getInt("y"));

					this.intersections.add(intersection);
					intersectionsAux.put(inter.getString("id"),
							             intersection);
					//JGRAPHT
					this.jgrapht.addVertex(intersection);
					System.out.println("Map.java-- Add Vertex " + 
					                  intersection.getId() +" : [ " +
							          intersection.toString() + " ]");
					line = intersectionsReader.readLine();
					this.intersectionCount++;
				}

				line = segmentsReader.readLine();

				//This will be used to add the steps later
				segmentsAux = new HashMap<String, Segment>();
				edgesAux = new HashMap<String, Edge>();

				//Read all the segments				
				while(line != null){

					JSONObject seg = new JSONObject(line);

					Intersection origin = null;
					Intersection destination = null;

					//Origin
					if(!seg.getString("origin").equals("null")) {

						origin = intersectionsAux.get(
								          seg.getString("origin"));
					}

					//Destination
					if(!seg.getString("destination").equals("null")) {

						destination = intersectionsAux.get(
								seg.getString("destination"));
					}

					//Populate the map
					JSONArray segTwinsJSON = seg.getJSONArray("twins");
					LinkedList<String> segTwinsList = new LinkedList<String>();
					for (int i = 0; i < segTwinsJSON.length(); i++){
						segTwinsList.add((String)segTwinsJSON.get(i));
					}
					Segment segment = new Segment(seg.getString("id"), 
					          origin, destination, seg.getDouble("length"),
					          seg.getInt("maxSpeed"), 
					          seg.getInt("capacity"),
					          seg.getInt("density"), 
					          seg.getInt("numberTracks"), 
					          this.mainContainer, this.segmentLogging, 
					          this.loggingDirectory, this.drawGUI,
					          seg.getString("direction"),
					          seg.getDouble("pkstart"), segTwinsList);


					if(origin != null){
						origin.addOutSegment(segment);
					}

					if(destination != null){
						destination.addInSegment(segment);
					}
					
					//Add an Edge to de Jgraph
					if(origin != null && destination != null){
						Edge e = new Edge(segment);
						System.out.println("Map.java-- Add Edge " + 
						                   segment.getId() +": [ " +
								           e.toString() + " ]");
						this.jgrapht.addEdge(origin, destination, e);
						// The weight is hours in double (0.xx)
						this.jgrapht.setEdgeWeight(e, 
								segment.getLength() /
								segment.getMaxSpeed());
						this.edgesAux.put(segment.getId(), e);
					}

					segmentsAux.put(segment.getId(), segment);

					line = segmentsReader.readLine();
					this.segmentCount++;
				}

				this.start = this.intersections.get(0);

				//Read all the steps
				line = stepsReader.readLine();

				//Read all the segments				
				while(line != null){

					JSONObject step = new JSONObject(line);

					//The segment the step belongs to
					String idSegment = step.getString("idSegment");

					//Create the step
					Step s = new Step(step.getString("id"), 
						segmentsAux.get(idSegment), 
						step.getJSONObject("originCoordinates").
						     getInt("x"),
						step.getJSONObject("originCoordinates").
						     getInt("y"),
						step.getJSONObject("destinationCoordinates").
						     getInt("x"),
						step.getJSONObject("destinationCoordinates").
						     getInt("y"));

					//Add the steps to the segment
					segmentsAux.get(idSegment).addStep(s);				

					line = stepsReader.readLine();
				}
				
				//Move the segments
				for (String string : segmentsAux.keySet()) {					
					this.move(segmentsAux.get(string), 4);
				}
				
				//Compute the length of the step according to the
				//   length of the segment
				for(Segment segment:segmentsAux.values()) {
					double length = 0.0;
					for(Step step:segment.getSteps()) {
						length += step.getStepGraphicalLength(); 
					}
					for(Step step:segment.getSteps()) {
						step.setStepLength((float) 
								(step.getStepGraphicalLength() *
								 segment.getLength() / length));
					}
				}

			}catch(Exception e){

				e.printStackTrace();
			}finally{

				intersectionsReader.close();
				segmentsReader.close();
				stepsReader.close();
			}
		}
	}

	/**
	 * Given the id of an intersection, it returns that intersection
	 * 
	 * @param id
	 * @return
	 */
	public Intersection getIntersectionByID(String id){

		Intersection ret = null;

		for(Intersection intersection: this.intersections){

			if(intersection.getId().equals(id)){

				ret = intersection;
				break;
			}
		}

		return ret;
	}

	/**
	 * Returns a random valid intersection id
	 * 
	 * @return
	 */
	public String getRandomIntersection(){

		Random rand = new Random();
		int randomNum = rand.nextInt(this.intersectionCount);

		return this.intersections.get(randomNum).getId();
	}
	
	/**
	 * Returns the jgraph with the structure of the map
	 * */
	public DefaultDirectedWeightedGraph<Intersection, Edge> 
	       getJgrapht() {
		return jgrapht;
	}

	/**
	 * Returns the list of intersections
	 * 
	 * @return The Intersection list
	 */
	public List<Intersection> getIntersections(){

		return this.intersections;
	}

	/**
	 * This method moves the segment a given quantity, so two  
	 * segments don't overlap.
	 * 
	 * @param seg
	 * @param quantity
	 */
	private void move(Segment seg, int quantity) {

		List<Step> steps = seg.getSteps();

		Step firstStep = steps.get(0);
		Step lastSetp = steps.get(steps.size()-1);

		//We will use this to check if the segment is more 
		//   horizontal than vertical
		int xIncrement = 
			lastSetp.getOriginX() - firstStep.getDestinationX();
		int yIncrement = 
			lastSetp.getOriginY() - firstStep.getDestinationY();

		if (xIncrement > yIncrement) { //The line is more horizontal

			if (firstStep.getOriginX() < lastSetp.getDestinationX()) { 
				//Left to right
				this.moveY(steps, quantity); //Move down
			} else {

				this.moveY(steps, -quantity); //Move up
			}
			
		} else { //The line is more vertical

			if (firstStep.getOriginY() > lastSetp.getDestinationY()) { 
				//Bottom to up
				this.moveX(steps, quantity); //Move right
			} else {

				this.moveX(steps, -quantity); //Move left
			}
		}
	}

	private void moveX(List<Step> stepList, int quantity){
		
		for (int i=0; i < stepList.size(); i++) {
			
			Step step = stepList.get(i);
			
			if (i == 0) { //First, we don't move its origin				
				step.setDestinationX(step.getDestinationX()+quantity);
			} else if (i == stepList.size()-1) { 
				//Last, we don't move its destination
				step.setOriginX(step.getOriginX() + quantity);
			} else {
				
				step.setDestinationX(step.getDestinationX()+quantity);
				step.setOriginX(step.getOriginX() + quantity);
			}
		}
	}

	private void moveY(List<Step> stepList, int quantity){
		
		for (int i=0; i < stepList.size(); i++) {
			
			Step step = stepList.get(i);
			
			if (i == 0) { //First, we don't move its origin
				
				step.setDestinationY(step.getDestinationY()+quantity);
				
			} else if (i == stepList.size()) { 
				//Last, we don't move its destination			
				step.setOriginY(step.getOriginY() + quantity);
			} else {
				
				step.setDestinationY(step.getDestinationY()+quantity);
				step.setOriginY(step.getOriginY() + quantity);
			}
		}
	}
	
	public Segment getSegmentByID(String id) {
		return segmentsAux.get(id);
	}
	
	public Edge getEdgeBySegmentID(String id) {
		return edgesAux.get(id);
	}

	public HashMap<String, Segment> getSegmentsAux() {
		return segmentsAux;
	}
	
	
}
