import java.io.IOException;
import java.util.*;

public class Analyser {
    private List<String> commonWords;
    private String commonWordsPath = "CommonWords.txt";
    private HashMap<String, MutableInt> words;
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
            MutableInt wordCount = words.get(word);
            if(wordCount == null){
                words.put(word, new MutableInt());
            }
            else{
                wordCount.increment();
            }
        }
    }

    class MutableInt {
        int value = 1;
        public void increment () { ++value;      }
        public int  get ()       { return value; }
    }

}


