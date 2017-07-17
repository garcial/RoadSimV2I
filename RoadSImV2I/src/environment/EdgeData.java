package environment;

import java.util.Date;

public class EdgeData {
	
	/*
	 * weight -- Level of services of a Segment
	 * initialDate -- miliseconds of the car enter in the segment
	 * finalDate -- miliseconds of the car exit of the segment
	 * */
	private char weight; //Utilizar double
	private long initialDate;
	private long finalDate;

	public EdgeData() {

	}

	public EdgeData(char weight, long initialDate, long finalDate) {
		super();
		this.weight = weight;
		this.initialDate = initialDate;
		this.finalDate = finalDate;
	}

	public char getWeight() {
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

}
