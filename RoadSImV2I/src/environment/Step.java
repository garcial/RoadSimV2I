package environment;

import java.io.Serializable;
import java.util.UUID;

/**
 * This class represents a step in a polyline of a segment.
 *
 */
public class Step implements Serializable {

	private static final long serialVersionUID = 111995236759060625L;

	//Unique id
	private String id;
		
	//Segment it belongs to
	private Segment segment;
	
	//Coordinates of the line
	private int originX, originY, destinationX, destinationY;
	private float stepLength;
	private float stepGraphicalLength;
	
	//Default constructor
	public Step(){
		
		this.id = UUID.randomUUID().toString();
		this.segment = null;
		this.originX = 0;
		this.originY = 0;
		this.destinationX = 0;
		this.destinationY = 0;
	}
	
	//Constructor
	public Step(String id, Segment segment, int originX, int originY, 
			    int destinationX, int destinationY){
		
		this.id = id;
		this.segment = segment;
		this.originX = originX;
		this.originY = originY;
		this.destinationX = destinationX;
		this.destinationY = destinationY;
		this.stepGraphicalLength = (float) Math.sqrt(
				(originX - destinationX) *
				(originX - destinationX) + 
				(originY - destinationY) *
				(originY - destinationY));
	}
	
	//Setters and getters
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Segment getSegment() {
		return segment;
	}

	public void setSegment(Segment segment) {
		this.segment = segment;
	}

	public int getOriginX() {
		return originX;
	}

	public void setOriginX(int originX) {
		this.originX = originX;
	}

	public int getOriginY() {
		return originY;
	}

	public void setOriginY(int originY) {
		this.originY = originY;
	}

	public int getDestinationX() {
		return destinationX;
	}

	public void setDestinationX(int destinationX) {
		this.destinationX = destinationX;
	}

	public int getDestinationY() {
		return destinationY;
	}

	public void setDestinationY(int destinationY) {
		this.destinationY = destinationY;
	}

	public float getStepLength() {
		return stepLength;
	}

	public void setStepLength(float stepLength) {
		this.stepLength = stepLength;
	}

	public float getStepGraphicalLength() {
		return stepGraphicalLength;
	}

	public void setStepGraphicalLength(float stepGraphicalLength) {
		this.stepGraphicalLength = stepGraphicalLength;
	}	
	
}
