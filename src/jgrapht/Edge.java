package jgrapht;

import java.util.ArrayList;
import java.util.List;
import org.jgrapht.graph.DefaultWeightedEdge;
import environment.EdgeData;

public class Edge extends DefaultWeightedEdge {

	/**
	 * The edge was composed by the segment and its current  
	 * density in a segment -- The segment that represent 
	 * this Edge weight -- Time to go to the 
	 * destinationedgeDataList -- List of data of others 
	 * cars about this segment
	 */
	private String idSegment;
	private double length;
	private int maxSpeed;
	private int serviceLevel;
	private List<EdgeData> edgeDataList;
	private static final long serialVersionUID = 17455L;

	
	public Edge(String idSegment, double length, int maxSpeed) {
		this.edgeDataList = new ArrayList<EdgeData>();
		this.idSegment = idSegment;
		this.length = length;
		this.maxSpeed = maxSpeed;
		this.edgeDataList = new ArrayList<EdgeData>();
	}

	public Edge(String idSegment, char serviceLevel, double weight,
			    long initialDate, long finalDate, double length, int maxSpeed) {
		super();
		this.edgeDataList = new ArrayList<EdgeData>();
		this.idSegment = idSegment;
		this.serviceLevel = serviceLevel;
		this.edgeDataList.add(
				new EdgeData(serviceLevel, weight, initialDate, finalDate));
		this.length = length;
		this.maxSpeed = maxSpeed;
		
	}
	
//	public void updateList(long n_k){
//		for(EdgeData elem:this.edgeDataList){
//			if(elem.getFinalDate() < n_k){
//				this.edgeDataList.remove(elem);
//			} else if (elem.getInitialDate() < n_k){
//				this.tini = n_k;
//			}
//		}
//	}

	public String getIdSegment() {
		return idSegment;
	}


	public List<EdgeData> getEdgeDataList() {
		return edgeDataList;
	}

	public void setEdgeDataList(List<EdgeData> edgeDataList) {
		this.edgeDataList = edgeDataList;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(int maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public void setIdSegment(String idSegment) {
		this.idSegment = idSegment;
	}

	public int getServiceLevel() {
		return serviceLevel;
	}

	public void setServiceLevel(int serviceLevel) {
		this.serviceLevel = serviceLevel;
	}

	@Override
	public String toString() {
		return "Edge [idSegment=" + idSegment + ", weight=" + getWeight() +
			   ", serviceLevel=" + serviceLevel + ", edgeDataList=" +
			   edgeDataList + "]";
	}

	

	

}
