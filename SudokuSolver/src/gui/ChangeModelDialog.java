package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.AbstractDocument;

import sudoku.ElementSet;
import sudoku.ElementSet.CannotCreateElementSetException;
import sudoku.Grid;
import util.Dims;

public class ChangeModelDialog extends JDialog {

	private static final long serialVersionUID = -8430299697176235159L;
	
	// Constant constructor parameters
	private static final String TITLE = "Sudoku Type";
	private static final boolean MODAL = true;

	// Constants
	private static final int MAX_NUM_ELEMENTS = 100;		// To avoid breaking everything, cap the table size at 100 elements
	private static final String NUMBER_ELEMENT_TYPE = "Numbers";
	private static final String UPPER_CASE_ELEMENT_TYPE = "Upper Case Letters";
	private static final String LOWER_CASE_ELEMENT_TYPE = "Lower Case Letters";
	private static final String CUSTOM_ELEMENT_TYPE = "Custom";
	private static final ElementGenerator[] PREDEFINED_ELEMENT_TYPES = createElementGenerators();
	private static final Color ERROR_COLOR = Color.RED;
	
	// Dynamic Swing objects, interacted with by the user
	private final SudokuGUI gui;		// Parent frame
	private final ChangeModelDialog dialog;		// A reference to the current instance of this class, for use by inner classes.
	private JTextField rowsTextField = new JTextField(Integer.toString(Grid.DEFAULT_ROWS_PER_BOX), 2);
	private JTextField colsTextField = new JTextField(Integer.toString(Grid.DEFAULT_COLS_PER_BOX), 2);
	private JComboBox<ElementGenerator> predefinedElements = new JComboBox<ElementGenerator>(PREDEFINED_ELEMENT_TYPES);
	private DefaultTableModel elementsTableModel;
//	private JTable elementTable;
	private JLabel errorTextLabel = new JLabel(null, null, SwingConstants.CENTER);
	
	// Listeners
	private SizeFieldListener rowColListener = new SizeFieldListener();
	private ElementTypeComboBoxListener comboListener = new ElementTypeComboBoxListener();
	private TableChangeListener tableListener = new TableChangeListener();
	
	ChangeModelDialog(SudokuGUI gui) {
		super(gui, TITLE, MODAL);
		this.gui = gui;
		dialog = this;
		
		// Set up content pane.
		JPanel contentPane = new JPanel();
		setContentPane(contentPane);
		
		// Set up an input filter on the row/column fields.
		NumberFilter filter = new NumberFilter();
		((AbstractDocument) rowsTextField.getDocument()).setDocumentFilter(filter);
		((AbstractDocument) colsTextField.getDocument()).setDocumentFilter(filter);
		
		// Create the table model and view, and size it according to the
		// default values in the row/column fields.
		elementsTableModel = new DefaultTableModel(new String[]{"Elements"}, 0);
		JTable elementTable = new JTable(elementsTableModel);
		resizeElementsTable();
		
		// Add all listeners.
		rowsTextField.addFocusListener(rowColListener);
		colsTextField.addFocusListener(rowColListener);
		predefinedElements.addActionListener(comboListener);
		elementsTableModel.addTableModelListener(tableListener);
		
		// Define a map of labels to their respective JComponents.
		// LinkedHashMap iterates through the elements in the order of
		// insertion, which is what we want to preserve.
		Map<String, JComponent> labelToInputMap = new LinkedHashMap<String, JComponent>();
		labelToInputMap.put("Number of rows per box: ", rowsTextField);
		labelToInputMap.put("Number of columns per box: ", colsTextField);
		labelToInputMap.put("Element type: ", predefinedElements);
		//labelToInputMap.put("Element names: ", elementTable);
	
		// Create the components to be contained in the content pane.
		JPanel labels = new JPanel(new GridLayout(labelToInputMap.size(), 1));
		JPanel inputFields = new JPanel(new GridLayout(labelToInputMap.size(), 1));
		
		// Fill in the labels and input fields from the map created above.
		for (Entry<String, JComponent> entry : labelToInputMap.entrySet()) {
			labels.add(new JLabel(entry.getKey()));
			inputFields.add(entry.getValue());
		}
		
		// TODO: Put elementTable in a scroll pane
		// Lay out the content pane.
		setLayout(new BorderLayout());
		add(errorTextLabel, BorderLayout.PAGE_START);
		add(labels, BorderLayout.LINE_START);
		add(inputFields, BorderLayout.CENTER);
		add(elementTable, BorderLayout.LINE_END);
		
		// Set borders and style
		Border outerBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		contentPane.setBorder(outerBorder);
		errorTextLabel.setFont(new Font(Font.DIALOG_INPUT, Font.ITALIC, 14));
		errorTextLabel.setForeground(ERROR_COLOR);
		
		// Display dialog.
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
	}
	
	private void resizeElementsTable() {
		String rowsText = rowsTextField.getText();
		String colsText = colsTextField.getText();
		int maxDigits = ((int) Math.floor(Math.log10(MAX_NUM_ELEMENTS))) + 1;

		// If the number of characters exceeds the maximum number of digits,
		// we don't even need to parse the string to an integer.
		if (rowsText.length() > maxDigits || colsText.length() > maxDigits) {
			setErrorText("Maximum " + maxDigits + " digits in the row/column fields");
			return;
		}

		int numRows, numCols;
		try {
			numRows = Integer.parseInt(rowsTextField.getText());
			numCols = Integer.parseInt(colsTextField.getText());
		} catch (NumberFormatException e) {
			// If cannot parse integers, return prematurely without changing the table
			setErrorText("Please write only digits in the row/column fields");
			return;
		}
		if (numRows <= 0 || numCols <= 0) {
			setErrorText("The row/column counts must be positive");
			return;
		}
		int numElements = numRows * numCols;
		int prevNumElements = elementsTableModel == null ? 0 : elementsTableModel.getRowCount();
		// Only proceed if the numElements is positive and less than the maximum.
		if (numElements > MAX_NUM_ELEMENTS) {
			setErrorText("The product of the row/column count must be less than " + MAX_NUM_ELEMENTS);
			return;
		}
		
		// There were no errors parsing or verifying row/column counts
		setErrorText(null);
		// No need to do anything if the number of elements hasn't changed.
		if (numElements != prevNumElements) {
			// The following table changes are not user initiated, so disable
			// the table listener temporarily.
			tableListener.active = false;
			if (numElements > prevNumElements) {
				// If the table is bigger than before, generate and fill in the
				// extra elements.
				ElementGenerator generator = (ElementGenerator) predefinedElements.getSelectedItem();
				String[] elements;
				try {
					elements = generator.generateElements(numElements);
					// Resize table
					elementsTableModel.setRowCount(numElements);
					if (elements != null) {
						for (int i = prevNumElements; i < numElements; i++) {
							elementsTableModel.setValueAt(elements[i], i, 0);
						}
					}
				} catch (CannotCreateElementSetException e) {
					setErrorText(e.getMessage());
				}
			} else {
				// Resize table
				elementsTableModel.setRowCount(numElements);
			}
			tableListener.active = true;
//			setErrorText(null);
			// Resize the dialog window, as the table size has now changed.
			dialog.pack();
		}
	}
	
	private void setErrorText(String error) {
		errorTextLabel.setText(error);
	}
	
	private String[] getElementStrings() {
		String[] elementStrings = new String[elementsTableModel.getRowCount()];
		for (int i = 0; i < elementStrings.length; i++) {
			Object value = elementsTableModel.getValueAt(i, 0);
			elementStrings[i] = value != null ? (String) value : "";
		}
		return elementStrings; 
	}
	
	private Grid verifyInput() {
		resizeElementsTable();
		if (errorTextLabel.getText() != null && errorTextLabel.getText().length() > 0) {
			return null;
		}
		
		String[] elementStrings = getElementStrings();
		ElementSet elementSet;
		try {
			elementSet = new ElementSet(elementStrings);
		} catch (CannotCreateElementSetException e) {
			setErrorText(e.getMessage());
			return null;
		}
		
		Dims cellsPerBox = new Dims(Integer.parseInt(colsTextField.getText()),
									Integer.parseInt(rowsTextField.getText()));
		try {
			return new Grid(cellsPerBox, elementSet.elementContents);
		} catch (CannotCreateElementSetException e) {
			setErrorText(e.getMessage());
			return null;
		}
		
	}
	
	@Override
	protected void processWindowEvent(WindowEvent e) {
//		System.out.println("WindowEvent: " + e);
//		System.out.println("old state: " + e.getOldState() + ", new state: " + e.getNewState() + ", ID: " + e.getID());
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
//			System.out.println("Rows: " + rowsTextField.getText() + ", Cols: " + colsTextField.getText() + ", Table size: " + elementsTableModel.getRowCount());
			Grid newModel = verifyInput();
//			System.out.println("newModel: " + newModel);
			if (newModel != null) {
				gui.setModel(newModel);
				super.processWindowEvent(e);
			}
		}
	}
	

	/**
	 * Method which creates 3 element generators, for numbers and upper/lower
	 * case letters, plus one custom element generator, which lets the user
	 * define the elements.
	 * @return array of ElementGenerators
	 */
	private static ElementGenerator[] createElementGenerators() {
		ElementGenerator[] generators = new ElementGenerator[] {
			new ElementGenerator(NUMBER_ELEMENT_TYPE) {
				@Override
				protected String[] generateElements(int numElements)
						throws CannotCreateElementSetException {
					return ElementSet.generateNumberRange(1, numElements);
				}
			},
			new ElementGenerator(UPPER_CASE_ELEMENT_TYPE) {
				@Override
				protected String[] generateElements(int numElements)
						throws CannotCreateElementSetException {
					return ElementSet.generateAlphabeticRange('A', numElements);
				}
			},
			new ElementGenerator(LOWER_CASE_ELEMENT_TYPE) {
				@Override
				protected String[] generateElements(int numElements)
						throws CannotCreateElementSetException {
					return ElementSet.generateAlphabeticRange('a', numElements);
				}
			},
			new ElementGenerator(CUSTOM_ELEMENT_TYPE) {
				@Override
				protected String[] generateElements(int numElements) {
					/* When selecting custom elements, we don't want to clear
					 * the table, in case the user simply wants to make a minor
					 * change to a predefined set of elements.
					 */
					
					//String[] emptyStrings = new String[numElements];
					//Arrays.fill(emptyStrings, "");
					//return emptyStrings;
					return null;
				}
			}			
		};
		return generators;
	}

	////////////////////////////////////////////////////////////////////////////
	//                              INNER CLASSES                             //
	////////////////////////////////////////////////////////////////////////////
	
	/**
	 *  Abstract class for generating elements of a particular type.
	 * @author Anders Thorbeck
	 *
	 */
	private static abstract class ElementGenerator {
		private final String name;
		protected abstract String[] generateElements(int numElements)
				throws CannotCreateElementSetException;
		private ElementGenerator(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return name;
		}
	}
	
	/**
	 * Only allow digits to be filled in.
	 * @author Anders Thorbeck
	 *
	 */
	private class NumberFilter extends DocumentFilter {
		
		private boolean isNumber(String str) {
			try {
				Integer.parseInt(str);
			} catch (NumberFormatException e) {
				return false;
			}
			return true;
		}
		
		@Override
		public void insertString(DocumentFilter.FilterBypass fb,
                int offset,
                String string,
                AttributeSet attr)
                  throws BadLocationException {
			if (isNumber(string)) {
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
			if (isNumber(text)) {
				super.replace(fb, offset, length, text, attrs);
			}
		}

	}
	
	private class SizeFieldListener extends FocusAdapter {

		@Override
		public void focusLost(FocusEvent arg0) {
			// When the row/column fields are changed, resize the table of elements.
			resizeElementsTable();
		}
		
	}
	
	private class ElementTypeComboBoxListener implements ActionListener {

		// The tableChanged method will only trigger when the listener is active.
		// This field can be set to false to avoid triggering the listener when
		// the combobox selection is not user initiated.
		private boolean active = true;
		
		@Override
		public void actionPerformed(ActionEvent event) {
			// Only handle event if listener is active.
			if (active) {
				System.out.println("Triggering combo select action");
				// Retrieve the selected element generator.
				ElementGenerator gen = (ElementGenerator) predefinedElements.getSelectedItem();
				int numElements = elementsTableModel.getRowCount();
				// Generate the required number of elements.
				String[] elements;
				try {
					elements = gen.generateElements(numElements);
				} catch (CannotCreateElementSetException e) {
					setErrorText(e.getMessage());
					return;
				}
				if (elements == null) {
					elements = new String[numElements];
					Arrays.fill(elements, "");
				}
				// The following table changes are not user initiated, so
				// temporarily disable table change listener.
				tableListener.active = false;
				// When the user manually selects an element type, replace all
				// the table contents with the corresponding elements of this 
				// selected element type.
				for (int i = 0; i < numElements; i++) {
					elementsTableModel.setValueAt(elements[i], i, 0);
				}
				tableListener.active = true;
			}
		}
		
	}
	
	private class TableChangeListener implements TableModelListener {
		// TODO: Maybe limit the contents of an element: characters or length of string.

		// The tableChanged method will only trigger when the listener is active.
		// This field can be set to false to avoid triggering the listener when
		// the table change is not user initiated.
		private boolean active = true;
		
		@Override
		public void tableChanged(TableModelEvent event) {
			// Only handle event if listener is active.
			if (active) {
				System.out.println("Triggering table change action");
				// Don't trigger the combobox listener, as this is not a user-initiated action.
				comboListener.active = false;
				// Set element type to Custom (assumed to be last element of combobox)
				predefinedElements.setSelectedIndex(predefinedElements.getItemCount() - 1);
				comboListener.active = true;
			}
		}
		
	}

}
