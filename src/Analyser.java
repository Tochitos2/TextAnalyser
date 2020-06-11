import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Analyser {
    //region Fields
    private final ArrayList<String> commonWords;
    private ArrayList<String> blackList;
    private ArrayList<String> whiteList;
    private final ArrayList<String> documentPaths;
    private final String commonWordsPath;
    private HashMap<String, Integer> words;
    private LinkedHashMap<String, Integer> sortedWords;
    private int wordCount;
    private boolean excludeCommon;
    private Restriction restriction;
    //endregion

    // Constructor
    public Analyser(){

        commonWords = new ArrayList<String>();
        documentPaths = new ArrayList<String>();
        blackList = new ArrayList<String>();
        whiteList= new ArrayList<String>();
        words = new HashMap<String, Integer>();
        sortedWords = new LinkedHashMap<String, Integer>();
        commonWordsPath = "src/CommonWords.txt";
        excludeCommon = true;
        restriction = Restriction.TEXT;

        LoadCommonWords();
    }

    //region File Loading

    /**
     * Loads list files, and adds document paths to list.
     * @param path The filepath
     * @param fileType Specifies document, blacklist, or whitelist.
     */
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

    /**
     * Remove all files with a given filename.
     * @param filename The name of the file to remove, including file extension.
     */
    public void removeDocument(String filename){
        // Splits on \ to get last section of address, aka the filename.
        documentPaths.removeIf(p -> (p.split("\\\\")[p.split("\\\\").length-1]).contains(filename));
    }

    /**
     * Loads list of common words from specified filepath.
     */
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

    //endregion

    //region File Parsing & Analysis

    /**
     * Reads all documents and adds words and occurrence number to unsorted hashmap, obeying filter rules and normalising text.
     */
    public void analyse(){
        words = new HashMap<String, Integer>();
        boolean inSpeech = false;
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
                String word = capitalise(scanner.next());

                // If restrictions are applied words must be parsed for speech marks.
                if(restriction == Restriction.DIALOGUE || restriction == Restriction.NARRATION) {
                    StringBuilder newWord = new StringBuilder();

                    for (int i = 0; i < word.length(); i++) {
                        if(word.charAt(i) == '“' ){
                            inSpeech = true;
                        }
                        else if(word.charAt(i) == '”') {
                             inSpeech = false;
                        }
                        else if (word.charAt(i) == '\"'){
                            inSpeech = !inSpeech;
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
                // Remove punctuation. Must be in before this point for dialogue tracking.
               word = capitalise(word.replaceAll("[^a-zA-Z'-]", ""));
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

    /**
     * Takes the unsorted list and sorts it by occurrence descending
     * @return returns a sorted linkedhashmap.
     */
    public LinkedHashMap<String, Integer> getSortedList(){
        sortedWords =  words.entrySet()
                .stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        return sortedWords;
    }

    /**
     * Capitalises the first character of a string, and lowercases all subsequent characters.
     * @param word The string to be capitalised
     * @return Returns the string in capitalised form.
     */
    private String capitalise(String word) {
        if(word.length() > 1) {
            word = word.substring(0, 1).toUpperCase() +
                    word.substring(1).toLowerCase();
        }
        else {
            word = word.toUpperCase();
        }
        return word;
    }

    //endregion

    //region Getters

    public int getWordCount(){
        return wordCount;
    }

    public boolean hasWhiteList(){
        return (whiteList.size() != 0);
    }

    public boolean hasBlackList(){
        return (blackList.size() != 0);
    }

    ///endregion

    //region Setters

    public void setRestriction(String restriction){
        switch (restriction){
            case "All Text":
                this.restriction = Restriction.TEXT;
                break;
            case "Dialogue Only":
                this.restriction = Restriction.DIALOGUE;
                break;
            case "Narration Only":
                this.restriction = Restriction.NARRATION;
                break;
        }
    }

    public void resetWhiteList(){ whiteList = new ArrayList<>(); }

    public void resetBlackList(){ blackList = new ArrayList<>(); }

    public void setExcludeCommon(boolean exclude){ excludeCommon = exclude; }

    //endregion
}


