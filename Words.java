package eoe.code;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Words {
  private static String WORD_FILE =  "/Users/Erick/words.txt";
  private List<String> words = new ArrayList<>(110000);
  private Random rand = new Random();
  
  public Words() throws IOException {
    try (BufferedReader br = new BufferedReader(new InputStreamReader(
        new FileInputStream(WORD_FILE), Charset.forName("UTF-8")))) {

      String line;
      while ((line = br.readLine()) != null) {
        words.add(line.trim());
      }
    }
    
  }
  public String getSomeWords(int count) {
    StringBuilder sb = new StringBuilder();
    for (int idx= 0; idx < count; ++idx) {
      sb.append(words.get(rand.nextInt(words.size()))).append(" ");
    }
    return sb.toString();
  }
  
  public String getAWord() {
    return words.get(rand.nextInt(words.size()));
  }

}