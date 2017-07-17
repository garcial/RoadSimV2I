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
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultCaret;

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

	private JLabel time, numberOfCars;
	private JTextArea logPanel;

	private InterfaceAgent interfaceAgent;
	public static int MAXWORLDX, MAXWORLDY;

	private Timer timer = new Timer(1000/this.FPS, this);

	/**
	 * Constructor
	 * 
	 * @param interfaceAgent
	 * @param maxX
	 * @param maxY
	 * @param map
	 */
	public CanvasWorld(InterfaceAgent interfaceAgent, int maxX, 
			           int maxY, Map map) {

		super();

		//Make it white
		this.getContentPane().setBackground(Color.WHITE);

		MAXWORLDX = maxX;
		MAXWORLDY = maxY;

		this.interfaceAgent = interfaceAgent;

		this.map = map;

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setTitle("Car enviroment");
		setBounds(10, 10, MAXWORLDX, MAXWORLDY);
		
		//Set the icon
		ImageIcon icon = new ImageIcon(CanvasWorld.class.
				  getClassLoader().
				  getResource("staticFiles/images/icon.png"));
		this.setIconImage(icon.getImage());

		//Create a layout
		this.getContentPane().setLayout(new GridBagLayout());

		//Fluid layout
		GridBagConstraints canvasConstraints = 
				                    new GridBagConstraints();

		//Relative sizes
		canvasConstraints.fill = GridBagConstraints.BOTH;
		canvasConstraints.gridwidth = 1; //How many columns to take
		canvasConstraints.gridheight = 6; //How many rows to take
		canvasConstraints.weightx = 0.9; //Percentage of space this will take horizontally
		canvasConstraints.weighty = 1; //Percentage of space this will take vertically
		canvasConstraints.gridx = 0; //Select column
		canvasConstraints.gridy = 0; //Select row

		contentPane = new MapPanel();
		this.add(contentPane, canvasConstraints);

		//Add a toolbar part, where 
		GridBagConstraints toolbarConstraints = new GridBagConstraints();

		//The time
		toolbarConstraints.fill = GridBagConstraints.BOTH;
		toolbarConstraints.weightx = 0.1; //Percentage of space this will take horizontally
		toolbarConstraints.weighty = 0.1; //Percentage of space this will take vertically
		toolbarConstraints.gridx = 1; //Select column
		toolbarConstraints.gridy = 0; //Select row

		this.time = new JLabel("Time: Not available");

		this.add(this.time, toolbarConstraints);

		//Agents count
		toolbarConstraints.fill = GridBagConstraints.BOTH;
		toolbarConstraints.weightx = 0.1; //Percentage of space this will take horizontally
		toolbarConstraints.weighty = 0.1; //Percentage of space this will take vertically
		toolbarConstraints.gridx = 1; //Select column
		toolbarConstraints.gridy = 1; //Select row

		this.numberOfCars = new JLabel("There are no cars");

		this.add(numberOfCars, toolbarConstraints);

		//Log panel
		this.logPanel = new JTextArea(2, 20);
		this.logPanel.setEditable(false);

		toolbarConstraints.weightx = 0.1; //Percentage of space this will take horizontally
		toolbarConstraints.weighty = 0.4; //Percentage of space this will take vertically
		toolbarConstraints.gridx = 1; //Select column
		toolbarConstraints.gridy = 2; //Select row

		//This will make the scroll not move
		DefaultCaret caret = (DefaultCaret) logPanel.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

		this.add(new JScrollPane(logPanel), toolbarConstraints);

		//Legend
		BufferedImage legend = null;
		try {
			legend = ImageIO.read(new File(CanvasWorld.class.getClassLoader().getResource("staticFiles/images/legend.png").getPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		JLabel legendLabel = new JLabel(new ImageIcon(legend), JLabel.LEFT);

		toolbarConstraints.weightx = 0.1; //Percentage of space this will take horizontally
		toolbarConstraints.weighty = 0.2; //Percentage of space this will take vertically
		toolbarConstraints.gridx = 1; //Select column
		toolbarConstraints.gridy = 3; //Select row

		this.add(legendLabel, toolbarConstraints);

		//Time slider
		toolbarConstraints.weightx = 0.1; //Percentage of space this will take horizontally
		toolbarConstraints.weighty = 0.1; //Percentage of space this will take vertically
		toolbarConstraints.gridx = 1; //Select column
		toolbarConstraints.gridy = 4; //Select row

		//Its label
		JLabel sliderLabel = new JLabel("Simulation speed", JLabel.CENTER);
		sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		this.add(sliderLabel, toolbarConstraints);

		toolbarConstraints.weightx = 0.1; //Percentage of space this will take horizontally
		toolbarConstraints.weighty = 0.1; //Percentage of space this will take vertically
		toolbarConstraints.gridx = 1; //Select column
		toolbarConstraints.gridy = 5; //Select row

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

		this.add(speedSlider, toolbarConstraints);

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
		this.time.setText("Time: " + inter.getInt("hora") + ":" + inter.getInt("minutos"));
	}

	/**
	 * Changes the number of cars in the GUI
	 * 
	 * @param cars Number of cars
	 */
	public void setNumberOfCars(int cars) {

		this.numberOfCars.setText("There are " + cars + " cars");
	}

	/**
	 * Adds text to the log area
	 * 
	 * @param text Text to append
	 */
	public void appendText(String text) {

		String aux = this.logPanel.getText();

		//Just so it doesn't grow too big
		if (aux.length() > 5000) {

			aux = "";
		}

		this.logPanel.setText(text + aux);
	}

	/**
	 * Adds a new car to the GUI
	 * 
	 * @param ag
	 * @param id
	 * @param algorithmColor
	 * @param x
	 * @param y
	 * @param specialColor
	 */
	public void addCar(String ag, String id, int algorithmColor, float x, float y, boolean specialColor) {

		contentPane.addCar(ag, id, algorithmColor, x, y, specialColor);	
	}

	/**
	 * Moves an existing car
	 * 
	 * @param id
	 * @param x
	 * @param y
	 * @param specialColor
	 */
	public void moveCar(String id, float x, float y, boolean specialColor) {
		contentPane.moveCar(id, x, y, specialColor);
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
		private ImageIcon mapImage = new ImageIcon(CanvasWorld.class.getClassLoader().getResource("staticFiles/images/red.png"));

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
		 * @param specialColor
		 */
		public void addCar(String ag, String id, int algorithmType, float x, float y, boolean specialColor) {

			carPositions.put(id, new Mobile(id, algorithmType, x, y, specialColor));
			repaint();
		}

		/**
		 * Moves a car on the panel
		 * 
		 * @param id
		 * @param x
		 * @param y
		 * @param specialColor
		 */
		public void moveCar(String id, float x, float y, boolean specialColor) {

			Mobile m = carPositions.get(id);

			if (m != null) {
				m.setX(x);
				m.setY(y);
				m.setSpecialColor(specialColor);
			}
		}

		/**
		 * Deletes a car from the panel
		 * 
		 * @param id
		 */
		public void deleteCar(String id) {

			this.carPositions.remove(id);
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
			Rectangle2D rect = new Rectangle2D.Float();

			//Set the stroke width
			g.setStroke(new BasicStroke(2));
			
			//Draw the intersections and segments
			for (Intersection in : map.getIntersections()) {

				for (Segment s: in.getOutSegments()){

					if (s.getCurrentServiceLevel().equals('A')) {

						g.setColor(Color.GREEN);

					} else if (s.getCurrentServiceLevel().equals('B')) {

						g.setColor(Color.YELLOW);

					} else if (s.getCurrentServiceLevel().equals('C')) {

						g.setColor(Color.ORANGE);

					} else if (s.getCurrentServiceLevel().equals('D')) {

						g.setColor(Color.RED);

					} else if (s.getCurrentServiceLevel().equals('E')) {

						g.setColor(Color.RED);

					} else if (s.getCurrentServiceLevel().equals('F')) {

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

				if (m.specialColor) {

					c = Color.RED;
				} else {

					if (m.getAlgorithmType() == Method.SHORTEST.value) {

						c = Color.WHITE;

					} else if (m.getAlgorithmType() == Method.FASTEST.value) {

						c = Color.CYAN;

					}else if (m.getAlgorithmType() == Method.SMARTEST.value) {

						c = Color.PINK;
					}
				}

				g.setStroke(new BasicStroke(1));

				//Windows
				rect.setFrame(x - 2, y - 2, 4, 2); 

				g.setColor(c);
				g.fill(rect);

				g.setColor(Color.BLACK);
				g.draw(rect);

				//Chasis
				rect.setFrame(x - 4, y, 8, 3);

				g.setColor(c);
				g.fill(rect);

				g.setColor(Color.BLACK);
				g.draw(rect);
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

			private boolean specialColor;

			public Mobile(String id, int algorithmType, float x, float y, boolean specialColor) {

				this.setId(id);
				this.setAlgorithmType(algorithmType);
				this.setX(x);
				this.setY(y);
				this.setSpecialColor(specialColor);

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

			public boolean isSpecialColor() {
				return specialColor;
			}

			public void setSpecialColor(boolean specialColor) {
				this.specialColor = specialColor;
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