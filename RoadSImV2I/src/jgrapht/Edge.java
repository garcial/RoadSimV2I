package jgrapht;

import java.util.ArrayList;
import java.util.List;
import org.jgrapht.graph.DefaultWeightedEdge;
import environment.EdgeData;
import environment.Segment;

public class Edge extends DefaultWeightedEdge {

	/**
	 * The edge was composed by the segment and its current  
	 * density in a segment -- The segment that represent 
	 * this Edge weight -- Time to go to the 
	 * destinationedgeDataList -- List of data of others 
	 * cars about this segment
	 */
	private Segment segment;
	private float weight;
	private long tini;
	private long tfin;
	private int serviceLevel;
	private List<EdgeData> edgeDataList;
	private static final long serialVersionUID = 17455L;

	public Edge() {
		// TODO Auto-generated constructor stub
	}
	
	public Edge(Segment segment) {
		this.edgeDataList = new ArrayList<EdgeData>();
		this.segment = segment;
		this.weight = (float) 0.0;
	}

	public Edge(Segment segment, char serviceLevel, 
			    long initialDate, long finalDate) {
		super();
		this.edgeDataList = new ArrayList<EdgeData>();
		this.segment = segment;
		this.weight = 0.0f;
		this.serviceLevel = serviceLevel;
		this.tini = initialDate;
		this.tfin = finalDate;
		this.edgeDataList.add(
				new EdgeData(serviceLevel, initialDate, finalDate));
		
		
	}
	
	@Override
	/**
	 * The objective is return the time estimated to do the travel
	 * */
	public double getWeight(){
		return weight;
	}
	
	public void updateList(long n_k){
		for(EdgeData elem:this.edgeDataList){
			if(elem.getFinalDate() < n_k){
				this.edgeDataList.remove(elem);
			} else if (elem.getInitialDate() < n_k){
				this.tini = n_k;
			}
		}
	}

	public Segment getSegment() {
		return segment;
	}

	public void setSegment(Segment segment) {
		this.segment = segment;
	}
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public List<EdgeData> getEdgeDataList() {
		return edgeDataList;
	}

	public void setEdgeDataList(List<EdgeData> edgeDataList) {
		this.edgeDataList = edgeDataList;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	public long getTini() {
		return tini;
	}

	public void setTini(long tini) {
		this.tini = tini;
	}

	public long getTfin() {
		return tfin;
	}

	public void setTfin(long tfin) {
		this.tfin = tfin;
	}

	public int getServiceLevel() {
		return serviceLevel;
	}

	public void setServiceLevel(int serviceLevel) {
		this.serviceLevel = serviceLevel;
	}

	@Override
	public String toString() {
		return "Edge [segment=" + segment + ", weight=" + weight +
			   ", tini=" + tini + ", tfin=" + tfin + 
			   ", serviceLevel=" + serviceLevel + ", edgeDataList=" +
			   edgeDataList + "]";
	}

	

	

}
