import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class Analyser {
    private ArrayList<String> commonWords;
    private String commonWordsPath = "CommonWords.txt";
    private HashMap<String, Integer> words;
    private LinkedHashMap<String, Integer> sortedWords;
    private int wordCount;

    public Analyser(){
        commonWords = new ArrayList<String>();
        words = new HashMap<String, Integer>();
        sortedWords = new LinkedHashMap<String, Integer>();

        LoadCommonWords();
    }

    public void LoadCommonWords(){

        Scanner scanner = new Scanner(commonWordsPath);

        while(scanner.hasNextLine()){
            commonWords.add(scanner.nextLine());
        }
    }

    public void addFile(String path) {

        File file = new File(path);
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while(scanner.hasNext()){
            String word = scanner.next().replaceAll("[^a-zA-Z'-]", "");
            if(!commonWords.contains(word)) {
                words.merge(word, 1, Integer::sum);
                wordCount++;
            }
        }
    }

    public LinkedHashMap getSortedList(){
        sortedWords =  words.entrySet()
                .stream().sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        return sortedWords;
    }

    public int getWordCount(){
        return wordCount;
    }

}


