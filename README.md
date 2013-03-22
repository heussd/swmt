Semantic Web based Machine Translation (SWMT)
====


	Source sentence:			Pages by Apple is a word processor like Word by MS.
	
	PREFIX  :     <http://www.example.com/#>
	PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>
	PREFIX  dbo:  <http://dbpedia.org/ontology/>
	PREFIX  dbp:  <http://dbpedia.org/property/>
	PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
	PREFIX  dbpedia: <http://dbpedia.org/resource/>
	PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
	PREFIX  fn:   <http://www.w3.org/2005/xpath-functions#>
	
	SELECT  ?sbLabel ?doesLabelFrom ?sthLabel ?doesLabelTo
	WHERE
	  { ?sb ?does ?sth .
	    ?does rdfs:label ?doesLabelFrom .
	    ?does rdfs:label ?doesLabelTo .
	    ?gSb dbo:wikiPageDisambiguates ?sb .
	    ?gSb rdfs:label ?sbLabel .
	    ?gSth dbo:wikiPageDisambiguates ?sth .
	    ?gSth rdfs:label ?sthLabel
	    FILTER langMatches(lang(?doesLabelFrom), "EN")
	    FILTER langMatches(lang(?doesLabelTo), "DE")
	    FILTER ( regex(str(?sbLabel), "Apple", "i") || regex(str(?sthLabel), "Apple", "i") )
	  }
	
	Pages by Apple => Pages produziert von Apple
	Pages by Apple => Pages von Apple
	Apple produces Pages => Apple produziert Pages
	PREFIX  :     <http://www.example.com/#>
	PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>
	PREFIX  dbo:  <http://dbpedia.org/ontology/>
	PREFIX  dbp:  <http://dbpedia.org/property/>
	PREFIX  owl:  <http://www.w3.org/2002/07/owl#>
	PREFIX  dbpedia: <http://dbpedia.org/resource/>
	PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
	PREFIX  fn:   <http://www.w3.org/2005/xpath-functions#>
	
	SELECT  ?sbLabel ?doesLabelFrom ?sthLabel ?doesLabelTo
	WHERE
	  { ?sb ?does ?sth .
	    ?does rdfs:label ?doesLabelFrom .
	    ?does rdfs:label ?doesLabelTo .
	    ?gSb dbo:wikiPageDisambiguates ?sb .
	    ?gSb rdfs:label ?sbLabel .
	    ?gSth dbo:wikiPageDisambiguates ?sth .
	    ?gSth rdfs:label ?sthLabel
	    FILTER langMatches(lang(?doesLabelFrom), "EN")
	    FILTER langMatches(lang(?doesLabelTo), "DE")
	    FILTER ( regex(str(?sbLabel), "Word", "i") || regex(str(?sthLabel), "Word", "i") )
	  }
	
	Word by MS => Word produziert von MS
	Word by MS => Word von MS
	MS produces Word => MS produziert Word
	
	Semantic Web enhanced translation:	Pages von Apple ist eine Textverarbeitung wie Word von MS.



