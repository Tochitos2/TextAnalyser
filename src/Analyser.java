import java.io.IOException;
import java.util.*;

public class Analyser {
    private List<String> commonWords;
    private String commonWordsPath = "CommonWords.txt";
    private HashMap<String, Integer> words;
    private LinkedHashMap<String, Integer> sortedWords;

    public List<String> getCommonWords(){

        Scanner scanner = new Scanner(commonWordsPath);

        while(scanner.hasNextLine()){
            commonWords.add(scanner.nextLine());
        }

        return commonWords;
    }

    public void addFile(String path){

        Scanner scanner = new Scanner(path);
        while(scanner.hasNext()){
            String word = scanner.nextLine().replaceAll("[^a-zA-Z'-]", "");
            words.merge(word, 1, Integer::sum);

        }
    }

}


