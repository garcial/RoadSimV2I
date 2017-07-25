package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.json.JSONObject;

import agents.InterfaceAgent;
import environment.Intersection;
import environment.Map;
import environment.Segment;
import environment.Step;
import searchAlgorithms.Method;
import view.CanvasWorld.MapPanel.Mobile;

/**
 * This is the graphical part of the application, it draws
 * all the elements, keeps a log and the time slider.
 *
 */
public class CanvasWorld extends JFrame 
       implements ActionListener, ChangeListener {

	private static final long serialVersionUID = 1L;
	private final int FPS = 40;

	private MapPanel contentPane;
	private Map map = null;
	
	private JLabel statisticsJL, timeJL;

	private InterfaceAgent interfaceAgent;
	public static int MAXWORLDX, MAXWORLDY;

	private Timer timer = new Timer(1000/this.FPS, this);

	/**
	 * Constructor
	 * 
	 * @param interfaceAgent
	 * @param map
	 */
	public CanvasWorld(InterfaceAgent interfaceAgent, Map map) {

		super();
		
		//Make it white
		this.getContentPane().setBackground(Color.WHITE);

		this.interfaceAgent = interfaceAgent;

		this.map = map;

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setTitle("V2I enviroment");
		setBounds(10, 10, InterfaceAgent.MAXWORLDX, 
				          InterfaceAgent.MAXWORLDY);

		//Create a layout
		this.getContentPane().setLayout(new GridBagLayout());

		//Fluid layout
		GridBagConstraints constraints = 
				                    new GridBagConstraints();

		//Relative sizes
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridwidth = 4; //How many columns to take
		constraints.gridheight = 1; //How many rows to take
//		constraints.weightx = 0.9; //Percentage of space this will take horizontally
		constraints.weighty = 1; //Percentage of space this will take vertically
		constraints.gridx = 0; //Select column
		constraints.gridy = 0; //Select row

		contentPane = new MapPanel();
		this.getContentPane().add(contentPane, constraints);

		//The statistics about types of vehicles and current time running
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridwidth = 1; //How many columns to take
		constraints.gridheight = 1; //How many rows to take
		constraints.weightx = 1; //Percentage of space this will take horizontally
		constraints.weighty = 0; //Percentage of space this will take vertically
		constraints.gridx = 0; //Select column
		constraints.gridy = 1; //Select row

		this.statisticsJL = 
				new JLabel("Shortest: 0% Fastest: 0% StartSmart: 0% DynamicSmart: 0% #cars: 0");
		this.getContentPane().add(this.statisticsJL, constraints);

		//The current time running
		constraints.gridx = 1; //Select column
		constraints.gridy = 1; //Select row

		this.timeJL = new JLabel("Current time: Not available");
		this.getContentPane().add(this.timeJL, constraints);
		
		//Time slider
		constraints.gridx = 2; //Select column
		constraints.gridy = 1; //Select row

		//Its label
		JLabel sliderLabel = new JLabel("Simulation speed", JLabel.CENTER);
		sliderLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

		this.getContentPane().add(sliderLabel, constraints);

		constraints.weightx = 1; //Percentage of space this will take horizontally
		constraints.weighty = 0; //Percentage of space this will take vertically
		constraints.gridx = 3; //Select column
		constraints.gridy = 1; //Select row

		JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, 100);
		speedSlider.setBackground(Color.WHITE);
		
		//Ignore the default tick value if the GUI is drawn
		this.interfaceAgent.setTick(100);
		
		//Labels
		Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
		labels.put(1, new JLabel("Faster"));
		labels.put(200, new JLabel("Slower"));
		speedSlider.setLabelTable(labels);
		speedSlider.setPaintLabels(true);

		this.getContentPane().add(speedSlider, constraints);

		//Listener
		speedSlider.addChangeListener(this);

		//Show the frame
		setVisible(true);

		this.timer.start();
	}

	/**
	 * Changes the time label
	 * 
	 * @param time New time
	 */
	public void setTime(String time) {
		
		JSONObject inter = new JSONObject(time);
		this.timeJL.setText("Time: " + 
		         String.format("%02d", inter.getInt("hora")) + ":" +
		         String.format("%02d", inter.getInt("minutos")));
	}

	/**
	 * Changes the number of cars in the GUI
	 * 
	 * @param cars Number of cars
	 */
	public void modifyStatistics() {
		List<Mobile> lista = 
				new ArrayList<Mobile>(
						this.interfaceAgent.getCanvasMap().getCars().values());
		java.util.Map<Integer, Long> counted = 
				lista.stream()
	            .collect(Collectors.groupingBy(	            		
	            		(x->x.getAlgorithmType()), 
	            		Collectors.counting()));
		int numberOfCars = lista.size();
		double percent = 100d / numberOfCars;
		this.statisticsJL.setText(
		    "Shortest:  " + porcentaje(counted, 0, percent) +"%  "+
			"Fastest:  " + porcentaje(counted, 1, percent) +"%  "+
			"StartSmart:  " + porcentaje(counted, 2, percent) +"%  "+
			"DynamicSmart:  " + porcentaje(counted, 3, percent) +"%  "+
			"#Cars:  " + numberOfCars);
	}
	
	private Long porcentaje(java.util.Map<Integer, Long> counted, int clave, double percent) {
		if (counted.containsKey(clave)) return Math.round(counted.get(clave) * percent);
		return 0l;
	}

	/**
	 * Adds a new car to the GUI
	 * 
	 * @param ag
	 * @param id
	 * @param algorithmColor
	 * @param x
	 * @param y
	 */
	public void addCar(String ag, String id, int algorithmType, float x, float y) {
		contentPane.addCar(ag, id, algorithmType, x, y);	
	}

	/**
	 * Moves an existing car
	 * 
	 * @param id
	 * @param x
	 * @param y
	 */
	public void moveCar(String id, float x, float y) {
		contentPane.moveCar(id, x, y);
	}

	/**
	 * Deletes a car from the GUI
	 * 
	 * @param id
	 */
	public void deleteCar(String id) {
		JSONObject objid = new JSONObject(id);
		contentPane.deleteCar(objid.getString("id"));
	}

	//Setters and getters
	public HashMap<String, Mobile> getCars() {
		return contentPane.getCars();
	}

	public void setCars(HashMap<String, Mobile> cars) {
		contentPane.setCars(cars);
	}

	public class MapPanel extends JPanel{

		private static final long serialVersionUID = 1L;
		private HashMap<String, Mobile> carPositions;
		private Image backGround;
		private ImageIcon mapImage = 
				new ImageIcon(CanvasWorld.class.getClassLoader().getResource("staticFiles/images/red.png"));

		/**
		 * Default constructor.
		 */
		public MapPanel() {
			this.carPositions =  new HashMap<String, Mobile>();
			backGround = mapImage.getImage();
			this.setBorder(new EmptyBorder(1, 1, 1, 1));
			this.setDoubleBuffered(true);
			this.setLayout(null);
		}

		/**
		 * Adds a car to the panel
		 * 
		 * @param ag
		 * @param id
		 * @param algorithmType
		 * @param x
		 * @param y
		 */
		public void addCar(String ag, String id, int algorithmType, float x, float y) {
			carPositions.put(id, new Mobile(id, algorithmType, x, y));
			modifyStatistics();
			repaint();
		}

		/**
		 * Moves a car on the panel
		 * 
		 * @param id
		 * @param x
		 * @param y
		 */
		public void moveCar(String id, float x, float y) {
			Mobile m = carPositions.get(id);
			if (m != null) {
				m.setX(x);
				m.setY(y);
			}
		}

		/**
		 * Deletes a car from the panel
		 * 
		 * @param id
		 */
		public void deleteCar(String id) {
			this.carPositions.remove(id);
			modifyStatistics();
		}

		/**
		 * This is the function that paints it all
		 */
		public void paint(Graphics gi) {

			Graphics2D g = (Graphics2D) gi;

			//Draw the background
			g.drawImage(backGround, 0, 0, this);

			//Classes that will be used to paint
			Line2D line = new Line2D.Float();
			Ellipse2D oval = new Ellipse2D.Float();

			//Set the stroke width
			g.setStroke(new BasicStroke(2));
			
			//Draw the intersections and segments
			for (Intersection in : map.getIntersections()) {

				for (Segment s: in.getOutSegments()){

					if (s.getCurrentServiceLevel() == 0) {

						g.setColor(Color.GREEN);

					} else if (s.getCurrentServiceLevel() == 1) {

						g.setColor(Color.YELLOW);

					} else if (s.getCurrentServiceLevel() == 2) {

						g.setColor(Color.ORANGE);

					} else if (s.getCurrentServiceLevel() == 3) {

						g.setColor(Color.RED);

					} else if (s.getCurrentServiceLevel() == 4) {

						g.setColor(Color.RED);

					} else if (s.getCurrentServiceLevel() == 5) {

						g.setColor(Color.BLACK);
					}

					for(Step st: s.getSteps()){

						line.setLine(st.getOriginX(), st.getOriginY(), st.getDestinationX(), st.getDestinationY());
						g.draw(line);
					}
				}
			}

			//Draw the intersections
			for (Intersection in : map.getIntersections()) {

				g.setColor(Color.RED);

				oval.setFrame(in.getX()-2, in.getY()-2, 4, 4);
				g.fill(oval);
			}

			//Draw the cars
			Color c = null;

			for (Mobile m : carPositions.values()) {

				float x = m.getX();
				float y = m.getY();
				if (m.getAlgorithmType() == Method.SHORTEST.value) {
						c = Color.WHITE;
				} else if (m.getAlgorithmType() == Method.FASTEST.value) {
						c = Color.CYAN;
  					  } else if (m.getAlgorithmType() == Method.DYNAMICSMART.value) {
  						  		c = Color.PINK;
					         } else if (m.getAlgorithmType() == Method.STARTSMART.value) 
					        	c = Color.ORANGE;
				g.setStroke(new BasicStroke(1));

				//Car draw as a circle
				oval.setFrame(x - 3, y - 3, 6, 6); 

				g.setColor(c);
				g.fill(oval);
			}
		}

		/**
		 * Auxiliary class
		 *
		 */
		public class Mobile {

			private String id;

			private int algorithmType;

			private float x;
			private float y;

			public Mobile(String id, int algorithmType, float x, float y) {

				this.setId(id);
				this.setAlgorithmType(algorithmType);
				this.setX(x);
				this.setY(y);
			}

			public float getX() {
				return x;
			}

			public void setX(float x) {
				this.x = x;
			}

			public float getY() {
				return y;
			}

			public void setY(float y) {
				this.y = y;
			}

			public String getId() {
				return id;
			}

			public void setId(String id) {
				this.id = id;
			}

			public int getAlgorithmType() {
				return algorithmType;
			}

			public void setAlgorithmType(int algorithmType) {
				this.algorithmType = algorithmType;
			}
		}

		//Setters and getters
		public void setCars(HashMap<String, Mobile> cars) {

			this.carPositions = cars;
		}

		public HashMap<String, Mobile> getCars() {

			return carPositions;
		}
	}

	/**
	 * This is the event for the timer, when called 
	 * it repaints the window.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == this.timer) {

			contentPane.repaint();
		}
	}

	/**
	 * This is the event for the slider, to change the speed of the
	 * simulation.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {

		JSlider source = (JSlider)e.getSource();

		if (!source.getValueIsAdjusting()) {

			int tickSpeed = (int)source.getValue();
			this.interfaceAgent.setTick(tickSpeed);
		}
	}
}