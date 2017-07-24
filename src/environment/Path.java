package environment;

import java.io.Serializable;
import java.util.List;

public class Path implements Serializable {
	
	private static final long serialVersionUID =-5385591092178183277L;

	//The list of intersections of this path
	List<Intersection> intersectionPath;
	
	//The graphical lines of this path
	List<Step> graphicalPath;
	
	//The segments of this path
	List<Segment> segmentPath;
	
	/**
	 * Default constructor.
	 * 
	 * @param intersetcionPath
	 * @param graphicalPath
	 * @param segmentPath
	 */
	public Path(List<Intersection> intersetcionPath, 
			    List<Step> graphicalPath, 
			    List<Segment> segmentPath) {
		
		this.intersectionPath = intersetcionPath;
		this.graphicalPath = graphicalPath;
		this.segmentPath = segmentPath;
	}

	//Getters
	public List<Intersection> getIntersectionPath() {
		return intersectionPath;
	}

	public List<Step> getGraphicalPath() {
		return graphicalPath;
	}

	public List<Segment> getSegmentPath() {
		return segmentPath;
	}

}
