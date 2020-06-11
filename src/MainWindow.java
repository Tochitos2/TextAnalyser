import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.File;
import java.util.*;

public class MainWindow extends JFrame {

    //region Fields
    private final Container contentPane;
    private final JPanel filePanel;
    private final JPanel filterPanel;
    private final JPanel actionPanel;
    private final JPanel resultsPanel;
    private final JButton analyseBtn;
    private final JButton addFileBtn;
    private final JButton addBlackListBtn;
    private final JButton addWhiteListBtn;
    private final JButton removeFileBtn;
    private JTable fileTable;
    private JCheckBox checkBox1;
    private final JComboBox restrictionComboBox;
    private final JCheckBox excludeCommonChkBx;
    private JLabel excludeCommonLbl;
    private final JLabel dialogueLbl;
    private final GridBagConstraints paneConstraints = new GridBagConstraints();
    private int fileCount;
    private final Desktop desktop = null;
    private final Analyser analyser = new Analyser();
    private boolean firstRun = true;
    //endregion

    public static void main(String[] args){
        MainWindow window = new MainWindow();
    }

    public MainWindow(){
        //region UI Code
        // Container initialisation
        super("Word Ranker");
        contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());

        contentPane.setPreferredSize(new Dimension(700,400));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // file panel initialisation
        filePanel = new JPanel(new GridBagLayout());
        GridBagConstraints fileConstraints = new GridBagConstraints();
        Border fileBorder = BorderFactory.createTitledBorder("Files");
        filePanel.setBorder(fileBorder);
        addFileBtn = new JButton("Add File");
        fileConstraints.fill = GridBagConstraints.BOTH;
        fileConstraints.anchor = GridBagConstraints.PAGE_START;
        fileConstraints.weightx = 0.1;
        fileConstraints.weighty = 0.5;
        fileConstraints.insets = new Insets(5,10, 5, 5);
        fileConstraints.gridx = 0;
        addFileBtn.addActionListener(e-> this.addFile(FileType.DOCUMENT));
        filePanel.add(addFileBtn, fileConstraints);
        removeFileBtn = new JButton("Remove File");
        removeFileBtn.setEnabled(false);
        removeFileBtn.addActionListener(e -> removeDocument());
        fileConstraints.gridx = 0;
        fileConstraints.gridy = 1;
        filePanel.add(removeFileBtn, fileConstraints);
        fileConstraints.fill = GridBagConstraints.BOTH;
        fileConstraints.weightx = 0.9;
        fileConstraints.weighty = 1;
        fileConstraints.gridx = 1;
        fileConstraints.gridy = 0;
        fileConstraints.gridheight = 2;
        fileConstraints.insets = new Insets(5,5, 5, 10);
        filePanel.add(makeFileTable(), fileConstraints);

        // Filter panel initialisation
        filterPanel = new JPanel(new GridBagLayout());
        GridBagConstraints filterConstraints = new GridBagConstraints();
        Border filterBorder = BorderFactory.createTitledBorder("Filters");
        filterPanel.setBorder(filterBorder);
        addBlackListBtn = new JButton("Add Blacklist");
        filterConstraints.fill = GridBagConstraints.BOTH;
        filterConstraints.weightx = 0.5;
        filterConstraints.weighty = 0.6;
        filterConstraints.gridx = 0;
        filterConstraints.gridy = 0;
        filterConstraints.insets = new Insets(10,10,0,7);
        addBlackListBtn.addActionListener(e -> this.handleFilterButton(FileType.BLACKLIST));
        filterPanel.add(addBlackListBtn, filterConstraints);
        addWhiteListBtn = new JButton("Add Whitelist");
        filterConstraints.fill = GridBagConstraints.BOTH;
        filterConstraints.weightx = 0.5;
        filterConstraints.weighty = 0.6;
        filterConstraints.gridx = 1;
        filterConstraints.gridy = 0;
        filterConstraints.insets = new Insets(10,7,0,10);
        addWhiteListBtn.addActionListener(e -> this.handleFilterButton(FileType.WHITELIST));
        filterPanel.add(addWhiteListBtn, filterConstraints);
        excludeCommonChkBx = new JCheckBox("Exclude 200 most common words in English language.", true);
        filterConstraints.gridwidth = 2;
        filterConstraints.weighty = 0.2;
        filterConstraints.insets = new Insets(5,10,0,0);
        filterConstraints.gridx = 0;
        filterConstraints.gridy = 1;
        excludeCommonChkBx.addItemListener(e -> changeCommonFilter(e.getStateChange()));
        filterPanel.add(excludeCommonChkBx, filterConstraints);
        filterConstraints.gridwidth = 1;
        filterConstraints.gridx = 0;
        filterConstraints.gridy = 2;
        filterConstraints.weightx = 0.3;
        filterConstraints.weighty = 0.2;
        dialogueLbl = new JLabel("Count:");
        filterPanel.add(dialogueLbl, filterConstraints);
        filterConstraints.gridx = 0;
        filterConstraints.gridy = 2;
        filterConstraints.weightx = 0.2;
        filterConstraints.weighty = 0.2;
        filterConstraints.insets = new Insets(5,60,5,5);
        String[] comboBoxOptions = { "All Text", "Dialogue Only", "Narration Only" };
        restrictionComboBox = new JComboBox(comboBoxOptions);
        restrictionComboBox.setSelectedIndex(0);
        restrictionComboBox.addItemListener(e -> analyser.setRestriction(restrictionComboBox.getSelectedItem().toString()));
        filterPanel.add(restrictionComboBox, filterConstraints);



        // Action panel initialisation
        actionPanel = new JPanel(new GridBagLayout());
        GridBagConstraints actionConstraints = new GridBagConstraints();
        analyseBtn = new JButton("Analyse Text");
        actionConstraints.fill = GridBagConstraints.BOTH;
        actionConstraints.weightx = 0.5;
        actionConstraints.weighty = 0.5;
        actionConstraints.gridx = 1;
        actionConstraints.gridy = 0;
        actionConstraints.insets = new Insets(15,15,10,15);
        analyseBtn.addActionListener(e-> this.showResults());
        analyseBtn.setEnabled(false);
        actionPanel.add(analyseBtn, actionConstraints);

        // Results panel initialisation
        resultsPanel = new JPanel(new GridLayout());

        // Panel addition to content pane.
        paneConstraints.fill = GridBagConstraints.BOTH;
        paneConstraints.gridx = 0;
        paneConstraints.weightx = 0.25;
        paneConstraints.gridy = 0;
        paneConstraints.weighty = 0.7;
        contentPane.add(filePanel, paneConstraints);
        paneConstraints.gridy = 1;
        paneConstraints.weighty = 0.2;
        contentPane.add(filterPanel, paneConstraints);
        paneConstraints.gridy = 2;
        paneConstraints.weighty = 0.1;
        contentPane.add(actionPanel, paneConstraints);
        pack();
        setVisible(true);
        //endregion
    }

    //region Document Handling

    /**
     * Creates the document file table.
     * @return JScrollPane containing document list in JTable.
     */
    private JScrollPane makeFileTable() {

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("FileName");
        fileTable = new JTable(model);
        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileTable.getSelectionModel().addListSelectionListener( e -> removeFileBtn.setEnabled(true));

        JScrollPane scrollPane = new JScrollPane(fileTable);
        scrollPane.setSize(300,100);

        return scrollPane;
    }

    /**
     * Handles pop-up file chooser dialogue and passes chosen file to analyser.
     * @param fileType Specifies whether the file is a document, blacklist or whitelist.
     */
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
                removeFileBtn.setEnabled(fileCount == 1 || (fileTable.getSelectedRow() != -1));
                analyseBtn.setEnabled(true);
            }
        }
    }

    /**
     * Removes the selected document or only document if singular.
     */
    private void removeDocument() {
        String filename;
        int row;
        int rowCount = fileTable.getModel().getRowCount();
        if(rowCount == 1){
            filename = fileTable.getModel().getValueAt(0,0).toString();
            row = 0;
        }
        else{
            filename = fileTable.getModel().getValueAt(fileTable.getSelectedRow(), 0).toString();
            row = fileTable.getSelectedRow();
        }

        analyser.removeDocument(filename);
        ((DefaultTableModel) fileTable.getModel()).removeRow(row);
        fileCount--;
        if (fileCount != 1) {
            removeFileBtn.setEnabled(false);

        }
        if(fileCount == 0) analyseBtn.setEnabled(false);

    }

    //endregion

    //region Filter Handling

    /**
     * Adds list to analyser and updates UI state.
     * @param listType
     */
    private void handleFilterButton(FileType listType) {
        if(listType == FileType.WHITELIST) {
            if(!analyser.hasWhiteList()){
                this.addFile(FileType.WHITELIST);
                if(analyser.hasWhiteList()) addWhiteListBtn.setText("Remove Whitelist");
            }
            else{
                analyser.resetWhiteList();
                addWhiteListBtn.setText("Add Whitelist");
            }

        }
        else if(listType == FileType.BLACKLIST) {
            if(!analyser.hasBlackList()){
                this.addFile(FileType.BLACKLIST);
                if(analyser.hasBlackList()) addBlackListBtn.setText("Remove Blacklist");
            }
            else{
                analyser.resetBlackList();
                addBlackListBtn.setText("Add Blacklist");
            }
        }
    }

    /**
     * Enables or disables the common words filter.
     * @param stateChange 1 == filter checked, 0 == filter off.
     */
    private void changeCommonFilter(int stateChange) {
        analyser.setExcludeCommon(stateChange == 1);
    }

    //endregion

    //region Results Handling

    /**
     * Creates the sorted results JTable within a JScrollPane
     * @param data The sorted word list with counts.
     * @return JScrollPane containing the sorted JTable.
     */
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
        }

        table.setRowHeight(20);
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(40);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(100);
        return new JScrollPane(table);
    }

    /**
     * Retrieves sorted results from analyser, creates the table and scrollpane, then adds to window.
     */
    private void showResults(){

        analyser.analyse();
        LinkedHashMap<String, Integer> results = analyser.getSortedList();
        JScrollPane scrollPane = createResultsTable(results);
        GridBagConstraints scrollConstraints = new GridBagConstraints();
        scrollConstraints.fill = GridBagConstraints.BOTH;
        scrollConstraints.gridx = 0;
        scrollConstraints.gridy = 0;
        scrollConstraints.weighty = 1;
        scrollConstraints.weightx = 1;
        scrollConstraints.insets = new Insets(5,5,5,5);

        // Clear result panel if not first run
        if(!firstRun) resultsPanel.removeAll();

        resultsPanel.add(scrollPane, scrollConstraints);

        paneConstraints.gridx = 1;
        paneConstraints.gridy = 0;
        paneConstraints.weighty = 1;
        paneConstraints.weightx = 1;
        paneConstraints.gridheight = 3;
        contentPane.add(resultsPanel, paneConstraints);
        pack();

        firstRun = false;
    }

    //endregion
}
