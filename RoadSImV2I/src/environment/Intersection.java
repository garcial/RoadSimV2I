package environment;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents the union of several {@link Segment}. A roundabout, 
 * a crossroad, a change of route...
 */
public class Intersection implements Serializable{
	

	private static final long serialVersionUID = 3885620456847709732L;

	//Unique id
	private String id;
	
	//In segments
	private ArrayList<Segment> in;
	
	//Out segments
	private ArrayList<Segment> out;
	
	//Coordinates
	private int x, y;
	
	/**
	 * Default constructor. 
	 */
	public Intersection(){
		
		this.id = "";
		this.in = new ArrayList<Segment>();
		this.out = new ArrayList<Segment>();
		this.x = 0;
		this.y = 0;
	}
	
	/**
	 * Constructor. 
	 *
	 * @param  in   A list of {@link Segment} that go into this
	 *              intersection.
	 * @param  out  A list of {@link Segment} that leave this
	 *              intersection.
	 * @param coordinates A array with the coordinates.
	 */
	public Intersection(String id, ArrayList<Segment> in, 
			            ArrayList<Segment> out, int x, int y) {
		
		this.id = id;
		this.in = in;
		this.out = out;
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Constructor. 
	 *
	 * @param  in   A list of {@link Segment} that go into this
	 *              intersection.
	 * @param  out  A list of {@link Segment} that leave this 
	 *              intersection.
	 * @param x A double with the x coordinate.
	 * @param y A double with the y coordinate.
	 */
	public Intersection(String id, int x, int y){
		
		this.id = id;
		this.in = new ArrayList<Segment>();
		this.out = new ArrayList<Segment>();
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Adds an in {@link Segment} to the Intersection. 
	 *
	 * @param  segment  {@link Segment} to be added.
	 * @return A boolean whether the collection has been modified 
	 *         or not.
	 */
	public boolean addInSegment(Segment segment){
		
		return this.in.add(segment);
	}
	
	/**
	 * Adds an out {@link Segment} to the Intersection. 
	 *
	 * @param  segment  {@link Segment} to be added.
	 * @return A boolean whether the {@link Segment} has been 
	 *         added or not.
	 */
	public boolean addOutSegment(Segment segment) {
		
		return this.out.add(segment);
	}

	//Setters and getters
	public String getId() {
		return id;
	}

	public ArrayList<Segment> getInSegments() {
		return in;
	}

	public ArrayList<Segment> getOutSegments() {
		return out;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}	
}
