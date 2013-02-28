public class Main {

	public static void main(String[] args) {
		String sentence = "Pages by Apple is better than Word by MS.";
		System.out.println("Source sentence:\t\t\t" + sentence);

		TrivialDictionary dict = TrivialDictionary.getInstance();
		Semantics semantics = Semantics.getInstance();

		for (String word : sentence.split(" ")) {
			if (semantics.isTrigger(word))
				dict.addCorpus(semantics.createTripleTranslations(word, "DE"));
		}

		System.out.println("Semantic Web enhanced translation:\t" + dict.translate(sentence));
	}

}
