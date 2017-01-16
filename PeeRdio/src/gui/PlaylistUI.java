package gui;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;

public class PlaylistUI extends JPanel {
 
	private static final long serialVersionUID = 1L;
	private JTable table;
	private JScrollPane scrollPane;

	public PlaylistUI() {
        super(new GridLayout(1,0));
 
        table = new JTable();
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
       
        scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 
        //Add the scroll pane to this panel.
        add(scrollPane);
    }
	
	public void setTableModel(AbstractTableModel model) {
		table.setModel(model);
	}
	
	public int getCurrentIndex() { 
		return table.getSelectedRow();
	}
	
	public void setCurrentIndex(int index) {
		table.getSelectionModel().setSelectionInterval(index, index);
	}

	public void setCurrentPosition(float position) {
		
	}
	
}