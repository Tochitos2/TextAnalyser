import com.sun.xml.internal.ws.util.QNameMap;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class MainWindow extends JFrame {

    private Container contentPane;
    private JPanel filePanel, filterPanel, actionPanel, resultsPanel;
    private JButton analyseBtn, addFileBtn, addBlacklistBtn;
    private JTable table1;
    private JCheckBox checkBox1;
    private JComboBox comboBox1;
    private JCheckBox excludeCommonChkBx;
    private JLabel excludeCommonLbl;

    private int fileCount;
    private Desktop desktop = null;
    private Analyser analyser = new Analyser();

    public static void main(String args[]){
        MainWindow window = new MainWindow();
    }

    public MainWindow(){
        // Container initialisation
        super("Word Ranker");
        contentPane = getContentPane();
        contentPane.setLayout(new GridLayout(3,2));
        contentPane.setPreferredSize(new Dimension(400,250));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // File panel initialisation
        filePanel = new JPanel(new GridBagLayout());
        Border fileBorder = BorderFactory.createTitledBorder("Files");
        filePanel.setBorder(fileBorder);
        addFileBtn = new JButton("Add File");
        addFileBtn.addActionListener(e-> this.addFile(FileType.DOCUMENT));
        filePanel.add(addFileBtn);


        // Filter panel initialisation
        filterPanel = new JPanel(new GridBagLayout());
        Border filterBorder = BorderFactory.createTitledBorder("Filters");
        filterPanel.setBorder(filterBorder);

        // Action panel initialisation
        actionPanel = new JPanel(new GridBagLayout());
        analyseBtn = new JButton("Analyse Text");
        analyseBtn.addActionListener(e-> this.showResults());
        contentPane.add(analyseBtn);

        // Results panel initialisation
        resultsPanel = new JPanel(new GridLayout());
        pack();
        setVisible(false);
    }

    private JScrollPane createResultsTable(LinkedHashMap<String, Integer> data) {

        // Create table data model.
        String[] columnNames = { "Rank", "Word", "Count" };
        TableModel dataModel = new DefaultTableModel() {
            public String getColumnName(int col){ return columnNames[col]; }
            public int getRowCount() { return data.size(); }
            public int getColumnCount() { return columnNames.length; }
            public Object getValueAt(int rowIndex, int columnIndex) {
                if(columnIndex == 0){ return rowIndex; }
                else if(columnIndex == 1){
                    Set<Map.Entry<String, Integer>> mapSet = data.entrySet();
                    Map.Entry<String, Integer> element = (Map.Entry<String, Integer>) mapSet.toArray()[rowIndex];
                    return element.getKey();
                }
                else{
                    Set<Map.Entry<String, Integer>> mapSet = data.entrySet();
                    Map.Entry<String, Integer> element = (Map.Entry<String, Integer>) mapSet.toArray()[rowIndex];
                    return element.getValue();
                }
            }
        };

        JTable table = new JTable(dataModel);
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        // Populate data model
        int rank = 1;
        Set<String> keys = data.keySet();
        for(String key: keys){
            Object rowData[] = {rank, key, data.get(key)};
            model.addRow(rowData);
            rank++;
        }

        JScrollPane scrollPane = new JScrollPane(table);
        return scrollPane;
    }


    private void addFile(FileType fileType) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
        chooser.setFileFilter(filter);

        int returnVal = chooser.showOpenDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            analyser.addFile(chooser.getSelectedFile().getAbsolutePath(), fileType);
            if(fileType == FileType.DOCUMENT){ fileCount++; }
        }
    }

    private void showResults(){
        LinkedHashMap<String, Integer> results = analyser.getSortedList();
        JScrollPane scrollPane = createResultsTable(results);

        resultsPanel.add(scrollPane);
        resultsPanel.setVisible(true);
    }

}
