package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import solver.SudokuSolver;
import sudoku.Element;
import sudoku.ElementSet;
import sudoku.ElementSet.CannotCreateElementSetException;
import sudoku.Grid;
import sudoku.SizeInfo;
import util.Coord;
import util.Dims;

// TODO: If multiple solutions, warn about it, ask whether to continue finding all?
// TODO: Allow to step through a solution one step at a time
// TODO: Option to specify different elements
// TODO: Option to specify different Grid sizes
// TODO: Better provision for manually solving sudoku.
// TODO: Add tooltip for why a cell is marked red.
// TODO: Functionality to save/load sudoku starting grids, to avoid having to type into code or GUI manually every time. Design file format.
// MAYBE: Lock grid size to only the sizes where the borders are adjacent to the grid. Maybe by putting the grid centred in another panel with borderlayout, letting that panel resize freely, but only letting the grid snap to certain sizes.
// MAYBE: Option to use other types of elements than strings, e.g. colours

public class SudokuGUI extends JFrame {

	private static final long serialVersionUID = -2839093900184656846L;

	// Constants
	private static final int CELL_BORDER_WIDTH = 1;
	private static final int BOX_BORDER_WIDTH = 2;
	private static final Color BORDER_COLOR = Color.BLACK;
	private static final float TEXT_SIZE_RATIO = (float) 0.9;
	private static final int CELL_COLUMNS = 1;
	private static final Color CELL_FIXED = Color.GRAY;
	private static final Color CELL_UNFILLED = Color.WHITE;
	private static final Color CELL_FILLED = Color.CYAN;
	private static final Color CELL_INCORRECT = Color.RED;

	// Shorthand for model.dimensions.*
	private SizeInfo dimensions;
	private Dims cellsPerBox;

	// Model
	Grid model;
	
	// View	
	private final SudokuGUI gui;	// So it can be referenced by nested classes
	private Container contentPane;
	private GridPanel gridPanel;
	private JButton setupButton;
	private JMenuBar menuBar;
	private JMenuItem changeModelMenuItem;
	
	private CellInputVerifier cellInputVerifier = new CellInputVerifier();
	
	// Controller (Listeners)
	
	
	// TODO: Dialog to choose what the elements and dimensions are, maybe?
	public SudokuGUI() {
		gui = this;
		initModelInvariantComponents();

		Dims cellsPerBoxInput = new Dims(3, 3);
//		Element[] elements = new Element[cellsPerBoxInput.product()];
//		for (int i = 0; i < elements.length; ++i) {
//			elements[i] = new Element(i, String.valueOf(i + 1));
//		}
//		String[] elementStrings = new String[cellsPerBoxInput.product()];
//		for (int i = 0; i < elementStrings.length; ++i) {
//			elementStrings[i] = String.valueOf(i + 1);
//		}
		Grid model = null;
		try {
			String[] elementStrings = ElementSet.generateNumberRange(1, cellsPerBoxInput.product());
			model = new Grid(cellsPerBoxInput, elementStrings);
		} catch (CannotCreateElementSetException e) {
			// Should never get this exception except on user-inputted element strings
			e.printStackTrace();
			System.exit(1);
		}
		setModel(model);
	}
	
	private void initModelInvariantComponents() {
		setupButton = new JButton("Setup complete");

		contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(setupButton, BorderLayout.SOUTH);
		
		// Add menu bar
		menuBar = new JMenuBar();
		JMenu menu = new JMenu("Menu");
		menu.setMnemonic(KeyEvent.VK_M);
		menuBar.add(menu);
		changeModelMenuItem = new JMenuItem("Change Sudoku Type", KeyEvent.VK_C);
		menu.add(changeModelMenuItem);
		this.setJMenuBar(menuBar);
		
		// Add resize listener
		this.addComponentListener(new FrameResizeListener());
		setupButton.addActionListener(new SetupButtonListener());
		changeModelMenuItem.addActionListener(new ChangeModelButtonListener());
		
		this.setTitle("Sudoku");
		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
//		this.setLocationRelativeTo(null);	// Center GUI on screen.
	}
	
	void setModel(Grid newModel) {
		model = newModel;
		dimensions = model.dimensions;
		cellsPerBox = model.dimensions.cellsPerBox;
		initModelDependentComponents();
	}
	
	private void initModelDependentComponents() {
		// Check if grid panel already exists
		if (gridPanel != null) {
			contentPane.remove(gridPanel);
		}
		
		gridPanel = new GridPanel();
		contentPane.add(gridPanel, BorderLayout.CENTER);
		this.revalidate();
		this.setSize(new Dimension(400, 400));
		this.setVisible(true);
		
	}
	
//	BoxPanel getBoxPanel(int boxIndex) {
//		return (BoxPanel) gridPanel.getComponent(boxIndex);
//	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new SudokuGUI();
			}
		});
	}

	// TODO: Maybe use actions which apply to a group of cells?
	class GridPanel extends JPanel
					implements Iterable<Cell> {

		private static final long serialVersionUID = -3321281515102768493L;
		
		private Cell[][] cells;
		
		GridPanel() {
			cells = new Cell[dimensions.cellsPerGrid.height][dimensions.cellsPerGrid.width];
			
			for (int r = 0; r < cells.length; ++r) {
				for (int c = 0; c < cells[r].length; ++c) {
					Coord cellCoord = new Coord(r, c);
					Cell cell = new Cell(cellCoord);
					cells[r][c] = cell;
					this.add(cell, dimensions.cellCoordToCellIndex(cellCoord));
				}
			}

			this.setLayout(new GridLayout(dimensions.cellsPerGrid.height, dimensions.cellsPerGrid.width));
			this.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(5, 5, 5, 5),
					BorderFactory.createLineBorder(BORDER_COLOR, BOX_BORDER_WIDTH)));
			
			initKeyBindings();
		}
		
		// NOTE: This works for up/down arrows, but not right/left arrows. Why? Action consumed by caret movement.
		// Maybe listen to all key events to figure out what key event gets triggered when I press left/right buttons.
		private void initKeyBindings() {
			CellFocusMoveAction upAction = new CellFocusMoveAction() {
				private static final long serialVersionUID = 5595666474512572674L;
				@Override
				protected boolean getBoundaryCondition(Coord focusCoord) {
					return focusCoord.row > 0;
				}
				@Override
				protected Coord getCoordDisplacement() {
					return new Coord(-1, 0);
				}
			};
			CellFocusMoveAction downAction = new CellFocusMoveAction() {
				private static final long serialVersionUID = 1645315982277096503L;
				@Override
				protected boolean getBoundaryCondition(Coord focusCoord) {
					return focusCoord.row < dimensions.cellsPerGrid.height - 1;
				}
				@Override
				protected Coord getCoordDisplacement() {
					return new Coord(1, 0);
				}
			};
			CellFocusMoveAction leftAction = new CellFocusMoveAction() {
				private static final long serialVersionUID = -1879136665826949420L;
				@Override
				protected boolean getBoundaryCondition(Coord focusCoord) {
					return focusCoord.col > 0;
				}
				@Override
				protected Coord getCoordDisplacement() {
					return new Coord(0, -1);
				}
			};
			CellFocusMoveAction rightAction = new CellFocusMoveAction() {
				private static final long serialVersionUID = 4558433443106561070L;
				@Override
				protected boolean getBoundaryCondition(Coord focusCoord) {
					return focusCoord.col < dimensions.cellsPerGrid.width - 1;
				}
				@Override
				protected Coord getCoordDisplacement() {
					return new Coord(0, 1);
				}
			};

			InputMap inputMap = this.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
			ActionMap actionMap = this.getActionMap();			
			String cellFocusMovementActionStringPrefix = "cellFocus";
			// Note: Left/right arrow keys will not work with ctrl, shift or no modifier,
			// as these events are consumed to move the caret in the JTextField.
			int keyModifiers = KeyEvent.ALT_DOWN_MASK;
			
			// Order: Up, Down, Left, Right
			String[] actionStrings = new String[] {"Up", "Down", "Left", "Right"};
			int[] virtualKeys = new int[] {KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT};
//			String[] keyNames = new String[] {"UP", "DOWN", "LEFT", "RIGHT"};
			CellFocusMoveAction[] actions = new CellFocusMoveAction[] {upAction, downAction, leftAction, rightAction};
			
			for (int dir = 0; dir < actionStrings.length; dir++) {
				String cellFocusMovementActionString
						= cellFocusMovementActionStringPrefix + actionStrings[dir];
				inputMap.put(KeyStroke.getKeyStroke(virtualKeys[dir], keyModifiers),
							 cellFocusMovementActionString);
//				inputMap.put(KeyStroke.getKeyStroke(keyNames[dir]),
//						 cellFocusMovementActionString);
				actionMap.put(cellFocusMovementActionString, actions[dir]);
			}
		}

		@Override
		public Iterator<Cell> iterator() {
			return new CellIterator();
		}

		abstract class CellFocusMoveAction extends AbstractAction {

			private static final long serialVersionUID = 8509419069952953708L;

			protected Cell getFocusedCell() {
				Component focusOwner = gui.getMostRecentFocusOwner();
				return (focusOwner instanceof Cell) ? (Cell) focusOwner : null; 
			}

			protected Coord getCoordOfFocusedCell() {
				Cell focusedCell = getFocusedCell();
				return (focusedCell != null) ? focusedCell.cellCoord : null;
			}
			
			protected abstract boolean getBoundaryCondition(Coord focusCoord);
			
			protected abstract Coord getCoordDisplacement();
			
			public void actionPerformed(ActionEvent e) {
//				System.out.println("In actionPerformed");
				Coord focusCoord = getCoordOfFocusedCell();
//				if (focusCoord != null) {
//					System.out.println("BoundaryCondition result: " + getBoundaryCondition(focusCoord));
//				}
				if (focusCoord != null && getBoundaryCondition(focusCoord)) {
					Coord disp = getCoordDisplacement();
					int newRow = focusCoord.row + disp.row;
					int newCol = focusCoord.col + disp.col;
					gridPanel.cells[newRow][newCol].requestFocusInWindow();
				}
			}
			
		}
		
		class CellIterator implements Iterator<Cell> {
			
			private int cellIndex = 0;
			private final int numCells = dimensions.cellsPerGrid.product();
			
			@Override
			public boolean hasNext() {
				return cellIndex < numCells;
			}

			@Override
			public Cell next() {
				Coord cellCoord = model.dimensions.cellIndexToCellCoord(cellIndex++);
				return cells[cellCoord.row][cellCoord.col];
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		}
		
	}
	

	class Cell extends JTextField {

		private static final long serialVersionUID = -6776981738103229885L;
		
		private final Coord cellCoord;
		private boolean fixed = false;
		
		Cell(Coord cellCoord) {
			super(model.elements.maxContentWidth);
			this.cellCoord = cellCoord;
//			AbstractDocument doc = (AbstractDocument) getDocument();
//			doc.setDocumentFilter(new CellDocumentFilter());
			initComponents();
		}
		
		private void initComponents() {
			this.setHorizontalAlignment(Cell.CENTER);
			this.setFont(new Font(Font.DIALOG, Font.PLAIN, 20)); //cell.getSize().height - 3
			this.setSize(new Dimension(20, 20));
			this.setBackground(CELL_UNFILLED);

			// Hack to ensure unit pixel border between cells in a box
			int bottom = (cellCoord.row == dimensions.cellsPerGrid.height - 1)
						 ? 0
						 : (cellCoord.row % cellsPerBox.height == cellsPerBox.height - 1)
						   ? BOX_BORDER_WIDTH
						   : CELL_BORDER_WIDTH;
			int right  = (cellCoord.col == dimensions.cellsPerGrid.width - 1)
						 ? 0
						 : (cellCoord.col % cellsPerBox.width == cellsPerBox.width - 1)
						   ? BOX_BORDER_WIDTH
						   : CELL_BORDER_WIDTH;
			this.setBorder(BorderFactory.createMatteBorder(0, 0, bottom, right, BORDER_COLOR));
			this.setInputVerifier(cellInputVerifier);

//			this.getDocument().getProperty("");//TODO Document of Cell?
			
		}
		
		Element parseElement() {
			return model.elements.getCorrespondingElement(getText());
		}
		
		void setFixed() {
//			this.setEnabled(false);
			this.setEditable(false);
			this.setBackground(CELL_FIXED);
			fixed = true;
		}
		
		void setElement(Element elem) {
			if (!fixed) {
				this.setText(elem != null ? elem.content : "");
				model.set(cellCoord, elem);
				this.setBackground(elem != null ? CELL_FILLED : CELL_UNFILLED);
			}
		}
		
		@Override
		public void setBackground(Color bg) {
			if (!fixed)
				super.setBackground(bg);
		}
		
		boolean isContentValid() {
			return isValidElement() && !isInConflict();
		}
		
		boolean isValidElement() {
//			System.out.println("text length: " + getText().length() + ", parsed element: " + parseElement());
			return getText().length() == 0 || parseElement() != null;
		}
		
		// Pre-condition: isValidElement returns true
		boolean isInConflict() {
			Element elem = parseElement();
			sudoku.Cell cellModel = model.getCell(cellCoord);
//			System.out.println("cellElem: " + elem);
//			if (elem != null) {
//				System.out.println("isPossible: " + cellModel.isPossible(elem) + ", model already contains this elem: " + elem.equals(cellModel.getValue()) + " (== comparison: " + (elem == cellModel.getValue()) + "), model cell: " + cellModel);
//			}
			return elem != null && !cellModel.isPossible(elem) && !elem.equals(cellModel.getValue());
		}
		
	}
	
	// Use these to ensure that the inserted/replaced text in a cell conforms to an element.
	// NOTE: This only checks the last inserted character. Want to check the full text. Use FocusListener or PropertyChangeListener instead.
	@Deprecated
	class CellDocumentFilter extends DocumentFilter {
		
		private boolean isAnElement(String str) {
			return model.elements.getCorrespondingElement(str) != null;
		}
		
		@Override
		public void insertString(DocumentFilter.FilterBypass fb,
                int offset,
                String string,
                AttributeSet attr)
                  throws BadLocationException {
			if (isAnElement(string)) {
				super.insertString(fb, offset, string, attr);
			}
		}
		
		@Override
		public void replace(DocumentFilter.FilterBypass fb,
		           int offset,
		           int length,
		           String text,
		           AttributeSet attrs)
		             throws BadLocationException {
			if (isAnElement(text)) {
				super.replace(fb, offset, length, text, attrs);
			}
		}
	}
	
	class CellInputVerifier extends InputVerifier {

		@Override
		public boolean verify(JComponent input) {
//			System.out.println("In verify");
			Cell cell = (Cell) input;
			boolean inputOK = checkField(cell, false);
			
			if (!inputOK) {
				Toolkit.getDefaultToolkit().beep();
				cell.setBackground(CELL_INCORRECT);
			} else {
				cell.setElement(cell.parseElement());
//				model.printBoard();
			}
			return inputOK;
		}
		
		private boolean checkField(Cell input, boolean changeIt) {
			return input.isContentValid();
		}
		
	}
	
	// Listeners

	class FrameResizeListener extends ComponentAdapter {
		@Override
		public void componentResized(ComponentEvent e) {
			// Set the font size to be a ratio of the cell size
			for (Cell cell : gridPanel) {
				cell.setFont(cell.getFont().deriveFont(cell.getSize().height
													   * TEXT_SIZE_RATIO));
			}
		}
	}
	
	class SetupButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// Check all content is valid
			for (Cell cell : gridPanel) {
				if (cell.getText().length() > 0 && !cell.isContentValid()) {
					JOptionPane.showMessageDialog(gui,
							"One or more of the cells contains text which is not a valid element.",
							"Invalid cell(s)",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}

			// Disable the filled in cells
			for (Cell cell : gridPanel) {
				if (cell.getText().length() > 0) {
					cell.setFixed();
				}
			}
			setupButton.setText("Solve");
			setupButton.removeActionListener(this);
			setupButton.addActionListener(new SolveButtonListener());
		}
		
	}
	
	class SolveButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			SudokuSolver solver = new SudokuSolver(model);
			Grid solvedModel = solver.solve();
			
			if (solvedModel == null || !solvedModel.isSolved()) {
				JOptionPane.showMessageDialog(gui,
						"Could not solve Sudoku",
						"Could not solve Sudoku",
						JOptionPane.INFORMATION_MESSAGE);
				System.out.println("Could not solve Sudoku");
			} else {
				for (Cell cell : gridPanel) {
					Element cellElem = solvedModel.getCell(cell.cellCoord).getValue();
					cell.setElement(cellElem);
				}
			}
			
			setupButton.removeActionListener(this);
			setupButton.setEnabled(false);
			
//			System.out.println("Model board state:");
//			model.printBoard();
		}
		
	}
	
	class ChangeModelButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			new ChangeModelDialog(gui);
		}
		
	}
	
	
}



