package com.dmtools.apps;

public class DMToolsMain {

	public static void main(String args[]) {
		MarkovDataAdapter mda = new MarkovDataAdapter();
		mda.insertWord("ja");
		mda.insertWord("tak");
		mda.insertWord("fe");
		mda.insertWord("ja");
	}
	
}
