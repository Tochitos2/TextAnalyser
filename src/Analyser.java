import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class Analyser {
    private ArrayList<String> commonWords;
    private String commonWordsPath = "src/CommonWords.txt";
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

        String currentDirectory = System.getProperty("user.dir");
        File file = new File(commonWordsPath);
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

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
            String word = capitalise(scanner.next().replaceAll("[^a-zA-Z'-]", ""));

            // If not a common word and contains at least 1 word character, then add.
            if(!commonWords.contains(word) && word.matches(".*\\w+.*")) {
                words.merge(word, 1, Integer::sum);
                wordCount++;
            }
        }
    }

    public LinkedHashMap getSortedList(){
        sortedWords =  words.entrySet()
                .stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        return sortedWords;
    }

    private String capitalise(String word)
    {
        if(word.length() > 1) {
            StringBuilder casedWord = new StringBuilder();
            casedWord.append(word.substring(0,1).toUpperCase());
            casedWord.append(word.substring(1).toLowerCase());
            word = casedWord.toString();
        }
        else {
            word = word.toUpperCase();
        }
        return word;
    }

    public int getWordCount(){
        return wordCount;
    }

}


