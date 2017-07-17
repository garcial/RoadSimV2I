package searchAlgorithms;

/**
 * Enumeration for the different types of algorithms
 *
 */
public enum Method {

	SHORTEST(0), FASTEST(1), SMARTEST(2), A_STAR(3), DIJKSTRA(4), KSHORTEST(5);
	
    public final int value;

    private Method(int value) {
        this.value = value;
    }
}