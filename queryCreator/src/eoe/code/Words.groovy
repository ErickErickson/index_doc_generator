package eoe.code

class Words {
  List<String> words = new ArrayList<>();

  void init() {
    File file = new File("/Users/Erick/words.txt")
    file.eachLine {
      words.add(it)
    }
  }

  Random rand = new Random();
  String getWords(int count, String joiner) {
    StringBuilder sb = new StringBuilder()
    for (int idx = 0; idx < count; ++idx) {
      if (sb.length() > 0) {
        sb.append(joiner)
      }
      sb.append(words.get(rand.nextInt(words.size())))
    }
    return sb.toString().replaceAll("[^a-z\\sA-Z]", "").trim()
  }
}
