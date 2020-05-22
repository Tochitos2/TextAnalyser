import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Analyser {
    private static List<String> commonWords;
    private static String commonWordsPath = "CommonWords.txt";

    public static List<String> getCommonWords()
            throws IOException {

        Scanner scanner = new Scanner(commonWordsPath);

        while(scanner.hasNextLine()){
            commonWords.add(scanner.nextLine());
        }

        return commonWords;
    }
}
