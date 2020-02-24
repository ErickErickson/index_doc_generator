package eoe.code

Words words = new Words()
words.init()

new File('/Users/Erick/apache/jmeter', 'queries.csv').withWriter('utf-8') {
  writer ->
    for (int idx in 1..1_000) {
      for (int jdx in 0..9) {
        String text = words.getWords(50, " OR ") // want to hit a LOT of docs.
        writeQuery(true, text, jdx, writer);
        //writeQuery(false, text, jdx, writer);

      }
    }
}
def writeQuery(boolean doDv, String text, int num, BufferedWriter writer) {
  StringBuilder sb = new StringBuilder('text_txt:(' + text + ')&')
  String prefix = "cur" + num  + "_c_"
  String field = prefix + ((doDv) ?  "dv" : "ndv");

  sb.append('json.facet={ bucket: { type: terms,field: bucket_s, limit: 3,')
  .append('facet: { average_price: "avg(')
  .append(field).append(')", total_price: "sum(').append(field).append(')" }}}')
  writer.writeLine sb.toString()
}
