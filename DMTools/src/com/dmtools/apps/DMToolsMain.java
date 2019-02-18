package com.dmtools.apps;

public class DMToolsMain {

	public static void main(String args[]) {
		String db = "American";
		int order = 3;
		//MarkovTrainer mt = new MarkovTrainer(db, "src/conf/english.txt", order, 0);
		MarkovGenerator mg = new MarkovGenerator(db, order);
		System.out.println(mg.getName());
	}
	
}
