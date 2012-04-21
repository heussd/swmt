public class Main {

	public static void main(String[] args) {
		String sentence = "Pages by Apple is better than Word by MS.";
		System.out.println("Source sentence:\t\t\t" + sentence);

		TrivialDictionary dict = TrivialDictionary.getInstance();
		SemanticCore core = SemanticCore.getInstance();

		System.out.println("Babel Fish like translation:\t\t" + dict.translate(sentence));

		if (core.hasTriggerWord(sentence))
			dict.addCorpus(core.translate(sentence, "de"));

		System.out.println("Semantic Web enhanced translation:\t" + dict.translate(sentence));
	}

}
