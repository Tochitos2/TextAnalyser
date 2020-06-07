import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Analyser {
    private final ArrayList<String> commonWords;
    private ArrayList<String> blackList;
    private ArrayList<String> whiteList;
    private final ArrayList<String> documentPaths;
    private final String commonWordsPath;
    private HashMap<String, Integer> words;
    private LinkedHashMap<String, Integer> sortedWords;
    private int wordCount;
    private boolean inSpeech, excludeCommon;
    private Restriction restriction;

    public Analyser(){
        commonWords = new ArrayList<String>();
        documentPaths = new ArrayList<String>();
        blackList = new ArrayList<String>();
        whiteList= new ArrayList<String>();
        words = new HashMap<String, Integer>();
        sortedWords = new LinkedHashMap<String, Integer>();
        commonWordsPath = "src/CommonWords.txt";
        inSpeech = false;
        excludeCommon = true;
        restriction = Restriction.TEXT;

        LoadCommonWords();
    }

    public void LoadCommonWords(){

        File file = new File(commonWordsPath);
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while(scanner != null && scanner.hasNextLine()){
            commonWords.add(scanner.nextLine());
        }
    }


    public void addFile(String path, FileType fileType) {

        // Only read the file if it's not the document, those should be read at final step with filters in place.
        if(fileType != FileType.DOCUMENT) {
            File file = new File(path);
            Scanner scanner = null;
            try {
                scanner = new Scanner(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            switch (fileType){

                case WHITELIST:
                    while(scanner != null && scanner.hasNext()) {
                        String word = capitalise(scanner.next());
                        whiteList.add(word);
                    }
                    break;

                case BLACKLIST:
                    while(scanner != null && scanner.hasNext()) {
                        String word = capitalise(scanner.next());
                        blackList.add(word);
                    }
                    break;
            }
        }
        else{
            documentPaths.add(path);
        }
    }

    public void analyse(){
        words = new HashMap<String, Integer>();
        for(String path: documentPaths){

            // Scanner initialisation
            File file = new File(path);
            Scanner scanner = null;
            try {
                scanner = new Scanner(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            // scan in words applying appropriate filters.
            while(scanner != null && scanner.hasNext()){
                // Filters out punctuation then capitalises in text case.
                String word = capitalise(scanner.next().replaceAll("[^a-zA-Z'-]", ""));

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
                // If not a common word, fits lists and contains at least 1 word character, then add.
                if((!excludeCommon || !commonWords.contains(word)) // Not a common word, or filter disabled.
                        && word.matches(".*\\w+.*") // contains at least one word type character
                        && !blackList.contains(word) // Not in blacklist
                        && (whiteList.size() == 0 || whiteList.contains(word))) { // Whitelist empty or contains word
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
    }

    public LinkedHashMap<String, Integer> getSortedList(){
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
            word = word.substring(0, 1).toUpperCase() +
                    word.substring(1).toLowerCase();
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

    public void resetWhiteList(){ whiteList = new ArrayList<>(); }

    public void resetBlackList(){ blackList = new ArrayList<>(); }

    public void setExcludeCommon(boolean exclude){ excludeCommon = exclude; }

    public boolean hasWhiteList(){
        return (whiteList.size() != 0);
    }

    public boolean hasBlackList(){
        return (blackList.size() != 0);
    }
}


