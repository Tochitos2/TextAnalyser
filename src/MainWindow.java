import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainWindow extends JFrame {

    private Container contentPane;
    private JButton analyseBtn, addFileBtn, addBlacklistBtn;
    private JCheckBox excludeCommonChkBx;
    private JLabel excludeCommonLbl;
    private int fileCount;
    private Desktop desktop = null;
    private Analyser analyser = new Analyser();

    public static void main(String args[]){
        MainWindow window = new MainWindow();
    }

    public MainWindow(){
        super("Wordcount Ranker");
        contentPane = getContentPane();
        contentPane.setLayout(new GridLayout(2,1));
        contentPane.setPreferredSize(new Dimension(300,300));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addFileBtn = new JButton("Add File");
        addFileBtn.addActionListener(e-> this.addFile());
        contentPane.add(addFileBtn);

        analyseBtn = new JButton("Analyse Text");
        analyseBtn.addActionListener(e-> this.showResults());
        contentPane.add(analyseBtn);

        pack();
        setVisible(true);
    }


    private void addFile() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
        chooser.setFileFilter(filter);

        int returnVal = chooser.showOpenDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            analyser.addFile(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void showResults(){
        LinkedHashMap<String, Integer> results = analyser.getSortedList();
        int rank = 0;
        for(Map.Entry<String, Integer> entry : results.entrySet()){
            String key = entry.getKey();
            int value = entry.getValue();
            rank++;

            System.out.println(rank + ". " + key + ": " + value);
            if(rank >= 200){ return; }
        }
    }

}
