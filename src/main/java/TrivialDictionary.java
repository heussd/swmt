import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class TrivialDictionary {

	private static final class InstanceHolder {
		static final TrivialDictionary INSTANCE = new TrivialDictionary();
	}

	public static TrivialDictionary getInstance() {
		return InstanceHolder.INSTANCE;
	}

	private HashMap<String, String> semanticDictionary;
	private HashMap<String, String> babelFishDictionary;

	private TrivialDictionary() {
		babelFishDictionary = new HashMap<String, String>();
		semanticDictionary = new HashMap<String, String>();

		babelFishDictionary.put("Apple", "Apple");
		babelFishDictionary.put("by", "durch");
		babelFishDictionary.put("MS", "Frau");
		babelFishDictionary.put("Pages", "Seiten");
		babelFishDictionary.put("Word", "Wort");
		babelFishDictionary.put("is", "ist");
		babelFishDictionary.put("better", "besser");
		babelFishDictionary.put("than", "als");
		babelFishDictionary.put("word processor", "Textverarbeitung");
		babelFishDictionary.put(" a", " eine");
		babelFishDictionary.put("like", "wie");
	}

	public void addCorpus(HashMap<String, String> corpus) {
		semanticDictionary.putAll(corpus);
	}

	public String translate(String sentence) {
		String mask = sentence;

		Iterator<Entry<String, String>> iterator = semanticDictionary.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry e = (Map.Entry) iterator.next();
			
			if (!mask.replaceAll((String) e.getKey(), "x").equals(mask)) {
				mask = mask.replaceAll((String) e.getKey(), "x");
				sentence = sentence.replaceAll((String) e.getKey(), (String) e.getValue());
			}
		}
		
		iterator = babelFishDictionary.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry e = (Map.Entry) iterator.next();

			if (!mask.replaceAll((String) e.getKey(), "x").equals(mask)) {
				mask = mask.replaceAll((String) e.getKey(), "x");

				sentence = sentence.replaceAll((String) e.getKey(), (String) e.getValue());
			}
			
		}
		return sentence;
	}
}
