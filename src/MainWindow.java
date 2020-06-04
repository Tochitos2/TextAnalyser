import com.sun.xml.internal.ws.util.QNameMap;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTable;
import java.awt.*;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class MainWindow extends JFrame {

    private final Container contentPane;
    private final JPanel filePanel;
    private final JPanel filterPanel;
    private final JPanel actionPanel;
    private final JPanel resultsPanel;
    private final JButton analyseBtn;
    private final JButton addFileBtn;
    private final JButton addBlackListBtn;
    private final JButton addWhiteListBtn;
    private JTable fileTable;
    private JCheckBox checkBox1;
    private JComboBox comboBox1;
    private final JCheckBox excludeCommonChkBx;
    private JLabel excludeCommonLbl;

    private int fileCount;
    private final Desktop desktop = null;
    private final Analyser analyser = new Analyser();

    public static void main(String[] args){
        MainWindow window = new MainWindow();
    }

    public MainWindow(){
        // Container initialisation
        super("Word Ranker");
        contentPane = getContentPane();
        contentPane.setLayout(new GridLayout(3,2));
        contentPane.setPreferredSize(new Dimension(600,300));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // File panel initialisation
        filePanel = new JPanel(new GridBagLayout());
        Border fileBorder = BorderFactory.createTitledBorder("Files");
        filePanel.setBorder(fileBorder);
        addFileBtn = new JButton("Add File");
        addFileBtn.addActionListener(e-> this.addFile(FileType.DOCUMENT));
        filePanel.add(addFileBtn);
        filePanel.add(makeFileTable());


        // Filter panel initialisation
        filterPanel = new JPanel(new GridBagLayout());
        Border filterBorder = BorderFactory.createTitledBorder("Filters");
        filterPanel.setBorder(filterBorder);
        addBlackListBtn = new JButton("Add Blacklist");
        addBlackListBtn.addActionListener(e -> this.addFile(FileType.BLACKLIST));
        filterPanel.add(addBlackListBtn);
        addWhiteListBtn = new JButton("Add Whitelist");
        addWhiteListBtn.addActionListener(e -> this.addFile(FileType.WHITELIST));
        filterPanel.add(addWhiteListBtn);
        excludeCommonChkBx = new JCheckBox("Exclude 200 most common words in English language.", true);
        excludeCommonChkBx.addItemListener(e -> changeCommonFilter(e.getStateChange()));
        filterPanel.add(excludeCommonChkBx);



        // Action panel initialisation
        actionPanel = new JPanel(new GridBagLayout());
        analyseBtn = new JButton("Analyse Text");
        analyseBtn.addActionListener(e-> this.showResults());
        actionPanel.add(analyseBtn);

        // Results panel initialisation
        resultsPanel = new JPanel(new GridLayout());

        contentPane.add(filePanel);
        contentPane.add(filterPanel);
        contentPane.add(actionPanel);
        pack();
        setVisible(true);
    }

    private JScrollPane makeFileTable() {

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("FileName");
        fileTable = new JTable(model);
        fileTable.setBounds(0, 0, 300, 80);

        JScrollPane scrollPane = new JScrollPane(fileTable);
        scrollPane.setSize(300,100);

        return scrollPane;
    }

    private void changeCommonFilter(int stateChange) {
        analyser.setExcludeCommon(stateChange == 1);
    }

    private JScrollPane createResultsTable(LinkedHashMap<String, Integer> data) {

        // Create table data model.
        String[] columnNames = { "Rank", "Word", "Count" };
        DefaultTableModel tableModel = new DefaultTableModel();
        JTable table = new JTable(tableModel);

        for(String columnName : columnNames){
            tableModel.addColumn(columnName);
        }

        // Populate data model
        int rank = 1;
        Set<String> keys = data.keySet();
        for(String key: keys){
            Object[] rowData = {rank, key, data.get(key)};
            tableModel.insertRow(tableModel.getRowCount(), rowData);
            rank++;

            //TODO: Remove debug output
            System.out.println(rowData[0] + ". " + rowData[1] + ": " + rowData[2]);
        }

        return new JScrollPane(table);
    }


    private void addFile(FileType fileType) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
        chooser.setFileFilter(filter);

        int returnVal = chooser.showOpenDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            analyser.addFile(path, fileType);
            if(fileType == FileType.DOCUMENT){
                File file = new File(path);
                String[] fileName = { file.getName() };
                DefaultTableModel model = (DefaultTableModel)fileTable.getModel(); // Safe cast, original type compiler is simply unaware.
                model.addRow(fileName);
                fileCount++;
            }
        }
    }

    private void showResults(){
        analyser.analyse();
        LinkedHashMap<String, Integer> results = analyser.getSortedList();
        JScrollPane scrollPane = createResultsTable(results);

        resultsPanel.add(scrollPane);
        resultsPanel.setVisible(true);
    }

}
