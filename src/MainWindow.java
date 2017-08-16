import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;


public class MainWindow extends JFrame {
	
	private static final int DEAD = 0;
	private static final int ALIVE = 1;
	private static final int THRESHOLD = 250;
	private static final int DEFAULT_GRID_SIZE = 50;
	
	private static final Color DEFAULT_BACKGROUND = Color.BLACK;
	private static final Color DEFAULT_CELL_COLOR = Color.GREEN;
	private static final Color DEFAULT_DEAD_COLOR = Color.DARK_GRAY;
	private static final Color DEFAULT_GUI_BACKGROUND = Color.BLACK;
	private static final Color DEFAULT_GUI_FOREGROUND = Color.GREEN;
	private static final Border DEFAULT_BORDER = BorderFactory.createLineBorder(Color.DARK_GRAY);
	private static final Font DEFAULT_GUI_FONT = new Font("Unifont", Font.PLAIN, 12);
	private static final Font INFO_FONT = new Font("Monaco", Font.BOLD, 14);
	private static final Font STATUS_FONT = new Font("Monaco", Font.BOLD, 12);
	
	// Unicode characters
	private static final String PLAY = "START \u23F5";
	private static final String PAUSE = "PAUSE \u23F8";
	private static final String STOP = "STOP \u23F9";
	private static final String UP_ARROW = "\u2191";
	private static final String DOWN_ARROW = "\u2193"; 
	
	private static final int TICK_INTERVAL = 100; //milliseconds
	
	private boolean inProgress;
	private int currentGridSize;
	private int curGeneration, curPopulation, maxPopulation, minPopulation, avgPopulation, totPopulation;
	private JPanel[][] grid;
	private JButton init, start, stop, plusOne, reset;
	private int[][] cells;
	private JPanel gridPanel, settingsPanel;
	private JLabel cgLabel, cpLabel, mxpLabel, mnpLabel, agpLabel, pdLabel;
	private JLabel statusLabel;
	private Timer tickTimer;
	private ArrayList<Integer> population;
	private boolean mousePressed;
	private Point lastMouseOverPoint;
	
	public MainWindow() {
		super("Game of life");
		
		currentGridSize = DEFAULT_GRID_SIZE;
		grid = new JPanel[currentGridSize][currentGridSize];
		cells = new int[currentGridSize][currentGridSize];
		
		gridPanel = new JPanel();
		settingsPanel = new JPanel();
		population = new ArrayList<Integer>();
		
		initValues();
		initGrid();
		initSettings();
		
		JPanel temp = new JPanel();
		temp.setLayout(new GridLayout(1, 2, 0, 0));
		temp.add(gridPanel);
		temp.add(settingsPanel);
		
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BorderLayout());
		statusPanel.setBackground(DEFAULT_GUI_BACKGROUND);
		statusLabel = getStylizedLabel(STATUS_FONT, DEFAULT_GUI_FOREGROUND);
		clearStatusLabel();
		statusPanel.add(statusLabel);

		this.getContentPane().setBackground(DEFAULT_GUI_BACKGROUND);
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(temp, BorderLayout.CENTER);
		this.getContentPane().add(statusPanel, BorderLayout.SOUTH);
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if(confirmExit()) {
					MainWindow.this.dispose();
					System.exit(0);
				}
			}
		});
		
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setSize(900, 500);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	
	private void initValues() {
		curPopulation = 0;
		curGeneration = 0;
		inProgress = false;
		mousePressed = false;
		population.clear();
		initLabels();
		updateLabels();
	}
	
	private void initLabels() {
		cgLabel = getStylizedLabel("Current Generation: " + curGeneration);
		cpLabel = getStylizedLabel("Current Population: " + curPopulation);
		mxpLabel = getStylizedLabel("Maximum Population: " + maxPopulation);
		mnpLabel = getStylizedLabel("Minimum Population: " + minPopulation);
		pdLabel = getStylizedLabel("Percentage Change : " + percentToString(getPercentageDifference()));
		agpLabel = getStylizedLabel("Average Population: " + avgPopulation);
	}
	
	public void initGrid() {
		gridPanel.setBackground(DEFAULT_GUI_BACKGROUND);
		gridPanel.setLayout(new GridLayout(grid.length, grid.length, 0, 0));
		for(int i=0; i<grid.length; i++) {
			for(int j=0; j<grid[i].length; j++) {
				final int x =i , y = j;
				grid[i][j] = new JPanel();
				grid[i][j].setBackground(DEFAULT_BACKGROUND);
				grid[i][j].setBorder(DEFAULT_BORDER);
				grid[i][j].addMouseListener(new MouseListener(){

					@Override
					public void mouseClicked(MouseEvent e) {
						// TODO Auto-generated method stub
						if(!inProgress) {
							flipCell(x ,y);
							if(!start.isEnabled()) {
								start.setEnabled(true);
							}
						}
					}

					@Override
					public void mousePressed(MouseEvent e) {
						mousePressed = true;
					}

					@Override
					public void mouseReleased(MouseEvent e) {
						mousePressed = false;
						if(!start.isEnabled()) {
							start.setEnabled(true);
						}
					}

					@Override
					public void mouseEntered(MouseEvent e) {
						setLastPoint(new Point(x, y));
						if(mousePressed && !inProgress) {
							flipCell(x, y);
						}
					}

					@Override
					public void mouseExited(MouseEvent e) {
						clearStatusLabel();
					}
				});
				gridPanel.add(grid[i][j]);
			}
		}
	}
	
	public void initSettings() {
		JPanel topPanel, centerPanel, bottomPanel;
		
		topPanel = new JPanel();
		centerPanel = new JPanel();
		bottomPanel = new JPanel();
		
		topPanel.setBackground(DEFAULT_GUI_BACKGROUND);
		centerPanel.setBackground(DEFAULT_GUI_BACKGROUND);
		bottomPanel.setBackground(DEFAULT_GUI_BACKGROUND);
		
		reset = new JButton("Reset");
		init = new JButton("Random Initialization");
		start = new JButton(PLAY);
		stop = new JButton(STOP);
		plusOne = new JButton("+1");
		
		reset.setFont(DEFAULT_GUI_FONT);
		init.setFont(DEFAULT_GUI_FONT);
		start.setFont(DEFAULT_GUI_FONT);
		stop.setFont(DEFAULT_GUI_FONT);
		plusOne.setFont(DEFAULT_GUI_FONT);
		
		start.setEnabled(false);
		stop.setEnabled(false);
		plusOne.setEnabled(false);
		
		// event listeners
		reset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Resetting grid");
				if(inProgress)
					haltGame();
				killAll();
				initButtons();
			}
		});
		
		init.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!inProgress) {
					generateRandomPopulation();
					start.setEnabled(true);
					reset.setEnabled(true);
				}
			}
		});
		
		// start is used both for play and pause
		start.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(start.getText().equals(PLAY)) {
					start.setText(PAUSE);
					stop.setEnabled(true);
					plusOne.setEnabled(false);
					inProgress = true;
					startTicking();
				}
				else {
					start.setText(PLAY);
					plusOne.setEnabled(true);
				}
			}
		});
		
		stop.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				haltGame();
			}
		});
		
		plusOne.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				advanceToNextGen();
			}
		});
		
		topPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		topPanel.add(init);
		topPanel.add(reset);
		
		//centerPanel.setLayout(new BorderLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		centerPanel.setLayout(new GridBagLayout());
		centerPanel.add(getStatsPanel(), constraints);
		centerPanel.setForeground(DEFAULT_GUI_FOREGROUND);
		
		bottomPanel.setLayout(new GridLayout(1, 3, 0, 0));
		bottomPanel.add(start);
		bottomPanel.add(stop);
		bottomPanel.add(plusOne);
		
		settingsPanel.setLayout(new BorderLayout());
		settingsPanel.add(topPanel, BorderLayout.NORTH);
		settingsPanel.add(centerPanel, BorderLayout.CENTER);
		settingsPanel.add(bottomPanel, BorderLayout.SOUTH);
	}
	
	private JPanel getStatsPanel() {
		JPanel temp = new JPanel();
		temp.setBackground(DEFAULT_GUI_BACKGROUND);
		temp.setForeground(DEFAULT_GUI_FOREGROUND);
		temp.setLayout(new BoxLayout(temp, BoxLayout.Y_AXIS));
		temp.add(getStylizedLabel("Cells can either be selected or randomly initialized or both"));
		temp.add(getStylizedLabel("----- ----- ----- ----- ----- ----- ----- ----- ----- -----"));
		temp.add(cgLabel);
		temp.add(cpLabel);
		temp.add(mxpLabel);
		temp.add(mnpLabel);
		temp.add(pdLabel);
		temp.add(agpLabel);
		return temp;
	}
	
	private JLabel getStylizedLabel() {
		return getStylizedLabel("");
	}
	
	private JLabel getStylizedLabel(String text) {
		JLabel label = getStylizedLabel(INFO_FONT, DEFAULT_GUI_FOREGROUND);
		label.setText(text);
		return label;
	}
	
	private JLabel getStylizedLabel(Font font, Color fontColor) {
		JLabel label = new JLabel();
		label.setFont(font);
		label.setForeground(fontColor);
		label.setForeground(DEFAULT_GUI_FOREGROUND);
		return label;
	}
	
	private void startTicking() {
		tickTimer = new Timer();
		tickTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				advanceToNextGen();
			}
		}, 0, TICK_INTERVAL);
	}
	
	private void stopTicking() {
		if(tickTimer != null) {
			tickTimer.cancel();
		}
	}
	
	private void makeAlive(int x, int y) {
		cells[x][y] = ALIVE;
		grid[x][y].setBackground(DEFAULT_CELL_COLOR);
		curPopulation++;
		updateStats();
	}
	
	private void makeDead(int x, int y) {
		makeDead(x, y, DEFAULT_DEAD_COLOR);
	}
	
	private void makeDead(int x, int y, Color color) {
		cells[x][y] = DEAD;
		grid[x][y].setBackground(color);
		curPopulation--;
		updateStats();
	}
	
	private void updateStats() {
		if(maxPopulation < curPopulation)
			maxPopulation = curPopulation;
		if(minPopulation > curPopulation)
			minPopulation = curPopulation;
	}
	
	private void flipCell(int x, int y) {
		if(isValid(x, y)) {
			if(isAlive(x, y))
				makeDead(x, y);
			else
				makeAlive(x, y);
		}
	}
	
	private boolean isValid(int x, int y) {
		return (x>=0 && x<currentGridSize) && (y>=0 && y<currentGridSize);
	}
	
	private boolean isAlive(int x, int y) {
		if(isValid(x , y))
			return (cells[x][y] == ALIVE);
		else
			return false;
	}
	
	private void generateRandomPopulation() {
		Random r = new Random();
		while(curPopulation < THRESHOLD)
			flipCell(r.nextInt(currentGridSize), r.nextInt(currentGridSize));
		population.clear();
		population.add(curPopulation);
		updateStats();
	}
	
	private int getAliveNeighbourCount(int x, int y) {
		int count = 0;
		if(isAlive(x-1, y-1))
			count++;
		if(isAlive(x-1, y))
			count++;
		if(isAlive(x-1, y+1))
			count++;
		if(isAlive(x, y-1))
			count++;
		if(isAlive(x, y+1))
			count++;
		if(isAlive(x+1, y-1))
			count++;
		if(isAlive(x+1, y))
			count++;
		if(isAlive(x+1, y+1))
			count++;
		return count;
	}
	
	private int[][] getNextGeneration() {
		int[][] nextGeneration = new int[cells.length][cells.length];
		for(int i=0; i<nextGeneration.length; i++) {
			for(int j=0; j<nextGeneration[i].length; j++) {
				int aliveNeighbours = getAliveNeighbourCount(i, j);
				if(aliveNeighbours == 3 || (isAlive(i, j) && aliveNeighbours == 2))
					nextGeneration[i][j] = ALIVE;
				else
					nextGeneration[i][j] = DEAD;
			}
		}
		return nextGeneration;
	}
	
	private void advanceToNextGen() {
		int[][] nextGen = getNextGeneration();
		for(int i=0; i<cells.length; i++) {
			for(int j=0; j<cells.length; j++) {
				if(nextGen[i][j] != cells[i][j]) {
					flipCell(i, j);
				}
			}
		}
		curGeneration++;
		population.add(curPopulation);
		totPopulation += curPopulation;
		avgPopulation = totPopulation/curGeneration;
		updateLabels();
		if(isTerminalStage() || curPopulation == 0) {
			haltGame();
		}
	}
	
	private void disableActionButtons() {
		start.setEnabled(false);
		stop.setEnabled(false);
		plusOne.setEnabled(false);
	}
	
	private void haltGame() {
		inProgress = false;
		stopTicking();	
		start.setText(PLAY);
		disableActionButtons();
	}
	
	
	// get population difference between current & last gen
	private int getPopulationDifference() {
		if(population.size() > 2)
			return population.get(population.size()-1) - population.get(population.size()-2);
		else if(population.size() == 1)
			return population.get(population.size()-1);
		else
			return 0;
	}
	
	private float getPercentageDifference() {
		int difference = getPopulationDifference();
		float percent;
		if(population.size() >= 2 && population.get(population.size()-2) > 0)
			percent = (difference / population.get(population.size()-2)) * 100;
		else
			percent = 100;
		return percent;
	}
	
	private String percentToString(float difference) {
		String str = "";
		if(difference < 0)
			str += DOWN_ARROW;
		else if(difference > 0)
			str += UP_ARROW;
		str += " (" + difference + ")";
		return str;
	}
	
	private void updateLabels() {
		cgLabel.setText("Current Generation: " + curGeneration);
		cpLabel.setText("Current Population: " + curPopulation);
		mxpLabel.setText("Maximum Population: " + maxPopulation);
		mnpLabel.setText("Minimum Population: " + minPopulation);
		pdLabel.setText("Percentage Change : " + percentToString(getPercentageDifference()));
		agpLabel.setText("Average Population: " + avgPopulation);
	}
	
	private boolean isTerminalStage() {
		/*if(population.size() > 25) {
			int i;
			for(i=population.size()-2; i>0; i--)
				if(population.get(i+1) != population.get(i))
					break;
			if(population.size() - i >= 25)
				return true;
		}*/
		return false;
	}
	
	private void killAll() {
		for(int i=0; i<cells.length; i++)
			for(int j=0; j<cells[i].length; j++) {
				if(isAlive(i, j))
					makeDead(i, j, DEFAULT_BACKGROUND);
				else
					grid[i][j].setBackground(DEFAULT_BACKGROUND);
			}
	}
	
	private void initButtons() {
		start.setEnabled(false);
		stop.setEnabled(false);
		plusOne.setEnabled(false);
		init.setEnabled(true);
	}
	
	private void setLastPoint(Point p) {
		lastMouseOverPoint = p;
		updateStatusLabel("Position: " + p);
	}
	
	private void updateStatusLabel(String text) {
		statusLabel.setText(text);
	}
	
	private void clearStatusLabel() {
		statusLabel.setText("..");
	}
	
	private boolean confirmExit() {
		int result = showConfirmDialog("Confirm Exit", "Sure you want to exit?");
		return result == JOptionPane.YES_OPTION;
	}
	
	private int showConfirmDialog(String title, String text) {
		return JOptionPane.showConfirmDialog(this, text, title, JOptionPane.YES_NO_OPTION);
	}
}
