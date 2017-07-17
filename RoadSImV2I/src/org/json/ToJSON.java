package org.json;

public class ToJSON {

	
	public static String toJSon(Object...objects) {
        /*StringBuilder cadena = new StringBuilder("{");
        for (int i = 0; i < objects.length; i+=2) {
            cadena.append(objects[i].toString()).
                   append(":").
                   append(objects[i+1]).
                   append(",");
        }
        return cadena.append("}").toString();*/
		
		JSONObject res = new JSONObject();
		for (int i = 0; i < objects.length; i+=2) {
			res.put(objects[i].toString(), objects[i+1]);
        }
		return res.toString();
    }
    
    public static String toJSonList(Object...objects) {
        StringBuilder build = new StringBuilder("[");
        for(int i = 0; i< objects.length; i++) 
            build.append(objects[i]).append(",");
        return build.append("]").toString();
    }
}
