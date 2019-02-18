package com.dmtools.apps;

public class MarkovGenerator {

	private MarkovDataAdapter mda;
	private int order;
	
	public MarkovGenerator(String databaseName, int order) {
		this.mda = new MarkovDataAdapter(databaseName);
		this.order = order;
	}
	
	private String getLetter(String context) {
		context = context.substring(context.length() - this.order, context.length());
		String nextChar = this.mda.getNextChar(context);
		return nextChar;
	}
	
	public String getName() {
		String word = this.mda.getRandomContext();
		String next_char = "";
		while (next_char != null) {
			word += next_char;
			next_char = getLetter(word);
		}
		
		// Name generation complete. Capitalize first letter
		word = word.substring(0, 1).toUpperCase() + word.substring(1);
		return word;
	}
	
}
