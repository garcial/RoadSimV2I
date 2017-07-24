package environment;


public class EdgeData {
	
	/*
	 * weight -- Level of services of a Segment
	 * initialDate -- miliseconds of the car enter in the segment
	 * finalDate -- miliseconds of the car exit of the segment
	 * */
	private int serviceLevel;
	private double weight; //Utilizar double
	private long initialDate;
	private long finalDate;

	public EdgeData() {

	}

	public EdgeData(int serviceLevel, double weight, long initialDate, long finalDate) {
		super();
		this.setServiceLevel(serviceLevel);
		this.weight = weight;
		this.initialDate = initialDate;
		this.finalDate = finalDate;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(char weight) {
		this.weight = weight;
	}

	public long getInitialDate() {
		return initialDate;
	}

	public void setInitialDate(long initialDate) {
		this.initialDate = initialDate;
	}

	public long getFinalDate() {
		return finalDate;
	}

	public void setFinalDate(long finalDate) {
		this.finalDate = finalDate;
	}

	public int getServiceLevel() {
		return serviceLevel;
	}

	public void setServiceLevel(int serviceLevel) {
		this.serviceLevel = serviceLevel;
	}

}
