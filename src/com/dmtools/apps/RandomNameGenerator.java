package com.dmtools.apps;

import java.util.Random;

public class RandomNameGenerator {
	
	private final static String[] firstNameSyllables = {"kal", "eb", "e", "mi", "da", "ruk", "von", "ral", "sai", "to",
			                                     "ol", "ug", "tsu", "ra", "kai", "mek", "ra", "ka", "ya"};
	private final static String[] lastNameSyllables = {"chi", "ta", "za", "strum", "vek", "ker", "der", "sal", "son", "sun", "tem",
			                                     "me", "li", "yo", "ya"};

	public static String getRandomName(int firstSyllableCount, int lastSyllableCount) {
		int firstLen = firstNameSyllables.length;
		int lastLen = lastNameSyllables.length;
		
		Random rand = new Random();
		
		String firstName = "";
		String lastName = "";
		
		for (int i = 0; i < firstSyllableCount; i++) {
			int n = rand.nextInt(firstLen);
			String tmp = firstNameSyllables[n];
			if (i == 0) {
				// Capitalize the first letter
				tmp = tmp.substring(0, 1).toUpperCase() + tmp.substring(1);
			}
			firstName += tmp;
		}
		for (int i = 0; i < lastSyllableCount; i++) {
		    int n = rand.nextInt(lastLen);
			String tmp = lastNameSyllables[n];
			if (i == 0) {
				// Capitalize the first letter
				tmp = tmp.substring(0, 1).toUpperCase() + tmp.substring(1);
			}
			lastName += tmp;
		}
		
		String fullName = firstName + " " + lastName;
		return fullName;
		
	}
	
}
