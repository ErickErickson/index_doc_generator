# index_doc_generator

The name is misleading. This is what I was using to gather the stats reported in SOLR-14137.

It's a bit of a mish-mash, there are three main parts. Originally, I was using jmeter and parameter substitution to test the different field types, but I eventually found it easier to just regenerate all the queries.

- queryCreator contains a groovy script to generate the csv file for input to jmeter.

- jmeter.jmx is the configuration for, well, jmeter. You'll have to tweak it around a bit to reflect local paths. It doesn't do any parameter substitution, as I mentioned above it's actually easier to regenerate the query file when you want to change which field you test.
solr6cfg and solr7cfg are the schemas I was using. solr7cfg can be used for Solr8 as well.

- Main (combined with words.txt) is the indexing program. words.txt is necessary for the indexing program to pick random words out for the text field. You'll have to change the source to path to it.

- queries_rerank.csv is an example of one of the things I tried, I was wondering if smaller result sets for boosting made a difference, and apparently not.

Ping me if you are confused.
