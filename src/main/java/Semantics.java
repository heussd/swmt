import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Semantics {

	private static final class InstanceHolder {
		static final Semantics INSTANCE = new Semantics();
	}

	public static Semantics getInstance() {
		return InstanceHolder.INSTANCE;
	}

	private Model rdfStore;
	private PrefixMapping prefixMapping;

	private Semantics() {
		prefixMapping = PrefixMapping.Factory.create();
		prefixMapping.setNsPrefix("rdf", RDF.getURI());
		prefixMapping.setNsPrefix("rdfs", RDFS.getURI());
		prefixMapping.setNsPrefix("owl", OWL.getURI());
		prefixMapping.setNsPrefix("", "http://www.example.com/#");
		prefixMapping.setNsPrefix("fn", "http://www.w3.org/2005/xpath-functions#");

		prefixMapping.setNsPrefix("dbpedia", "http://dbpedia.org/resource/");
		prefixMapping.setNsPrefix("dbp", "http://dbpedia.org/property/");
		prefixMapping.setNsPrefix("dbo", "http://dbpedia.org/ontology/");

		rdfStore = ModelFactory.createDefaultModel();
		rdfStore.read(new File("store.rdf").toURI().toString(), "N3");
	}

	public String dumpStore() {
		StringWriter stringWriter = new StringWriter();
		rdfStore.write(stringWriter, "N3");

		System.out.println("RDF STORE DUMP\n" + stringWriter.toString());
		return stringWriter.toString();
	}

	public HashMap<String, String> createTripleTranslations(String subjectOrObjectLabel, String toLanguage) {
		String language = (toLanguage.equals("EN") ? "DE" : "EN");

		String queryString = "select ?sbLabel ?doesLabelFrom ?sthLabel ?doesLabelTo where { ?sb ?does ?sth . ?does rdfs:label ?doesLabelFrom . ?does rdfs:label ?doesLabelTo . ?gSb dbo:wikiPageDisambiguates ?sb . ?gSb rdfs:label ?sbLabel . ?gSth dbo:wikiPageDisambiguates ?sth . ?gSth rdfs:label ?sthLabel . FILTER(langMatches(lang(?doesLabelFrom), \""
				+ language
				+ "\")) FILTER(langMatches(lang(?doesLabelTo), \""
				+ toLanguage
				+ "\")) FILTER(REGEX(STR(?sbLabel), \""
				+ subjectOrObjectLabel
				+ "\", \"i\") || REGEX(STR(?sthLabel), \"" + subjectOrObjectLabel + "\", \"i\"))}";

		HashMap<String, String> result = new HashMap<String, String>();
		Query query = QueryFactory.make();
		query.setPrefixMapping(prefixMapping);
		query = QueryFactory.parse(query, queryString, null, Syntax.syntaxSPARQL);

		Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
		InfModel infmodel = ModelFactory.createInfModel(reasoner, rdfStore);

		System.out.println(query.toString());
		QueryExecution qexec = null;
		try {
			qexec = QueryExecutionFactory.create(query, infmodel);

			ResultSet results = qexec.execSelect();

			for (; results.hasNext();) {
				QuerySolution rb = results.nextSolution();

				// System.out.println("Query result: " + rb);

				String subject = rb.get("sbLabel").toString();
				String predicateFrom = rb.get("doesLabelFrom").toString().replaceAll("@\\w*", "");
				String predicateTo = rb.get("doesLabelTo").toString().replaceAll("@\\w*", "");

				String object = rb.get("sthLabel").toString();

				System.out.println(subject + " " + predicateFrom + " " + object + " => " + subject + " " + predicateTo + " " + object);

				result.put(subject, subject);
				result.put(predicateFrom, predicateTo);
				result.put(object, object);
				result.put(subject + " " + predicateFrom + " " + object, subject + " " + predicateTo + " " + object);
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return result;

	}

	private boolean ask(String queryString) {
		Query query = QueryFactory.make();
		query.setPrefixMapping(prefixMapping);
		query = QueryFactory.parse(query, queryString, null, Syntax.syntaxSPARQL);

		QueryExecution qexec = null;

		Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
		InfModel infmodel = ModelFactory.createInfModel(reasoner, rdfStore);

		try {
			return QueryExecutionFactory.create(query, infmodel).execAsk();

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (qexec != null)
				qexec.close();
		}
	}

	public Boolean isTrigger(String word) {
		if (ask("ASK { ?trigger a  <http://www.example.org/#trigger> . ?term dbo:wikiPageDisambiguates ?trigger . ?term rdfs:label \"" + word + "\" }")) {
			return true;
		}
		return false;
	}

}
