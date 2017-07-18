package searchAlgorithms;

/**
 * Enumeration for the different types of algorithms
 *
 */
public enum Method {

	SHORTEST(0), FASTEST(1), STARTSMART(2), DYNAMICSMART(3),
    A_STAR(4), DIJKSTRA(5), KSHORTEST(6);
	
    public final int value;

    private Method(int value) {
        this.value = value;
    }
}