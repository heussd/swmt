import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

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

public class SemanticCore {

	private static final class InstanceHolder {
		static final SemanticCore INSTANCE = new SemanticCore();
	}

	public static SemanticCore getInstance() {
		return InstanceHolder.INSTANCE;
	}

	private static Logger logger = Logger.getLogger(SemanticCore.class);

	private Model rdfStore;
	private PrefixMapping prefixMapping;

	private SemanticCore() {
		logger.debug("Bringing up semantic core...");

		prefixMapping = PrefixMapping.Factory.create();
		prefixMapping.setNsPrefix("rdf", RDF.getURI());
		prefixMapping.setNsPrefix("rdfs", RDFS.getURI());
		prefixMapping.setNsPrefix("owl", OWL.getURI());
		prefixMapping.setNsPrefix("", "http://www.example.com/#");
		prefixMapping.setNsPrefix("fn", "http://www.w3.org/2005/xpath-functions#");

		rdfStore = ModelFactory.createDefaultModel();
		rdfStore.read(new File("store.rdf").toURI().toString(), "N3");

		logger.debug("Semantic core is up.");
		dumpStore();
	}

	private String dumpStore() {
		StringWriter stringWriter = new StringWriter();
		rdfStore.write(stringWriter, "N3");

		logger.debug("RDF STORE DUMP\n" + stringWriter.toString());
		return stringWriter.toString();
	}


	private List<String> fireLabelQuery(String queryString) {

		logger.debug(queryString);
		List<String> result = new ArrayList<String>();
		Query query = QueryFactory.make();
		query.setPrefixMapping(prefixMapping);

		query = QueryFactory.parse(query, queryString, null, Syntax.syntaxSPARQL);

		logger.debug(query.toString());

		QueryExecution qexec = null;

		Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
		InfModel infmodel = ModelFactory.createInfModel(reasoner, rdfStore);

		try {
			qexec = QueryExecutionFactory.create(query, infmodel);
			ResultSet results = qexec.execSelect();

			for (; results.hasNext();) {
				QuerySolution rb = results.nextSolution();

				logger.debug("Query result: " + rb);
				String line = rb.get("subjectName").toString() + " ";

				line += rb.get("predicateName").toString().replaceAll("@\\w*", "") + " ";
				line += rb.get("objectName").toString();
				result.add(line);
				System.out.println(line);
			}
		} catch (Exception e) {
			logger.error("Error while executing query:", e);
		} finally {
			if (qexec != null)
				qexec.close();
		}

		return result;
	}

	private List<String> fireTranslationQuery(String queryString, String lang) {
		logger.debug("fireTranslationQuery");
		List<String> result = new ArrayList<String>();
		Query query = QueryFactory.make();
		query.setPrefixMapping(prefixMapping);
		query = QueryFactory.parse(query, queryString, null, Syntax.syntaxSPARQL);

		logger.debug(query.toString());

		QueryExecution qexec = null;

		Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
		InfModel infmodel = ModelFactory.createInfModel(reasoner, rdfStore);

		try {
			qexec = QueryExecutionFactory.create(query, infmodel);
			ResultSet results = qexec.execSelect();

			for (; results.hasNext();) {
				QuerySolution rb = results.nextSolution();

				logger.debug("Query result: " + rb);
				String subject = rb.get("subject").toString();
				String predicate = rb.get("predicate").toString();
				String object = rb.get("object").toString();

				result.addAll(fireLabelQuery("SELECT ?subjectName ?predicateName ?objectName WHERE { <" + subject + "> <" + predicate + "> <" + object + ">  . <" + subject
						+ "> rdfs:label ?subjectName . <" + predicate + "> rdfs:label ?predicateName . <" + object
						+ ">  rdfs:label ?objectName . FILTER ( lang(?predicateName) = \"" + lang + "\" ) } "));

			}
			
			
			for (String string : result) {
				logger.debug("Inferenced dictionary entry: " + string);
			}
		} catch (Exception e) {
			logger.error("Error while executing query:", e);
		} finally {
			if (qexec != null)
				qexec.close();
		}

		return result;
	}

	private boolean ask(String queryString) {
		Query query = QueryFactory.make();
		query.setPrefixMapping(prefixMapping);
		query = QueryFactory.parse(query, queryString, null, Syntax.syntaxSPARQL);

		logger.debug(query.toString());

		QueryExecution qexec = null;

		Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
		InfModel infmodel = ModelFactory.createInfModel(reasoner, rdfStore);

		try {
			return QueryExecutionFactory.create(query, infmodel).execAsk();

		} catch (Exception e) {
			logger.error("Error while executing query:", e);
		} finally {
			if (qexec != null)
				qexec.close();
		}
		return false;
	}

	public boolean hasTriggerWord(String trigger) {
		for (String word : trigger.split(" ")) {
			if (ask("ASK { ?trigger a  <http://www.example.org/#trigger> . ?trigger rdfs:label \"" + word + "\" }")) {
				return true;
			}
		}
		return false;
	}

	public HashMap<String, String> translate(String string, String lang) {
		string = string.replaceAll("\\.", "");
		HashMap<String, String> results = new HashMap<String, String>();

		for (String[] statements = string.split(" ", 4); statements.length > 2; statements = string.split(" ", 4)) {

			logger.debug("Translation Query: " + statements[0] + " " + statements[1] + " " + statements[2]);

			List<String> translation = translateTriple(statements[0], statements[1], statements[2], lang);
			
			for (String string2 : translation) {
				System.out.println("T: " + string2);
			}
			
			if (translation.size() > 0) {
				results.put(statements[0] + " " + statements[1] + " " + statements[2], translation.get(0));
			}

			if (statements.length < 4)
				break;

			// Reduce the remaining string and redo the cutting into triples
			string = statements[3];
		}

		Iterator iterator = results.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry e = (Map.Entry) iterator.next();
			logger.debug("Found translation " + (String) e.getKey() + " = " + (String) e.getValue());
		}
		return results;
	}

	public List<String> translateTriple(String subject, String predicate, String object, String lang) {
		return fireTranslationQuery("SELECT ?subject ?predicate ?object WHERE { ?subject ?predicate ?object . ?subject rdfs:label \"" + subject + "\" . ?predicate rdfs:label \""
				+ predicate + "\"@" + (lang.equals("en") ? "de" : "en") + " . ?object rdfs:label \"" + object + "\" . }", (lang.equals("en") ? "en" : "de"));
	}

}
