import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class Analyser {
    private ArrayList<String> commonWords;
    private String commonWordsPath;
    private HashMap<String, Integer> words;
    private LinkedHashMap<String, Integer> sortedWords;
    private int wordCount;
    private boolean inSpeech;
    private Restriction restriction;

    public Analyser(){
        commonWords = new ArrayList<String>();
        words = new HashMap<String, Integer>();
        sortedWords = new LinkedHashMap<String, Integer>();
        commonWordsPath = "src/CommonWords.txt";
        inSpeech = false;
        restriction = Restriction.TEXT;

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
            String word = capitalise(scanner.next());

            // If restrictions are applied words must be parsed for speech marks.
            if(restriction == Restriction.DIALOGUE || restriction == Restriction.NARRATION) {
                StringBuilder newWord = new StringBuilder();
                for (int i = 0; i < word.length(); i++) {
                    if (word.charAt(i) == '\"') {
                        inSpeech = !inSpeech; //Flip whether in dialogue on speech mark.
                    }
                    // Builds the word according to whether the analyser is set to count dialogue or vice versa.
                    if (restriction == Restriction.DIALOGUE && inSpeech) {
                        newWord.append(word.charAt(i));
                    }else if(restriction == Restriction.NARRATION && !inSpeech){
                        newWord.append(word.charAt(i));
                    }
                }
                word = newWord.toString();
            }
            // Filter out most punctuation.
            word.replaceAll("[^a-zA-Z'-]", "");
            // If not a common word and contains at least 1 word character, then add.
            if(!commonWords.contains(word) && word.matches(".*\\w+.*")) {
                words.merge(word, 1, Integer::sum);
                wordCount++;
            }
        }
        // If the document ends with a single open speech mark there may be missing speech marks
        // in the text, which would throw off the tracking of dialogue.
        if(restriction != Restriction.TEXT && inSpeech){
            System.out.println("Document ends with a trailing speech mark. Possible punctuation error in text." +
                    "\nResults may be inaccurate.");
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

    public void setRestriction(Restriction restriction) { this.restriction = restriction; }

}


