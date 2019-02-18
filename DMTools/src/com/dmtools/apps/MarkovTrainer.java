package com.dmtools.apps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MarkovTrainer {

	private MarkovDataAdapter mda;
	private String[] ds;
	private int order;
	private float prior;
	private HashMap<String, ArrayList<Character>> markovMap;
	private ArrayList<Character> alphabet;
	private HashMap<MarkovKey, Float> markovChain;
	
	public MarkovTrainer(String databaseName, String datasetFile, int order, float prior) {
		this.mda = new MarkovDataAdapter(databaseName);
		digestFile(datasetFile);
		
		this.order = order;
		this.prior = prior;
		
		this.markovMap = new HashMap<String, ArrayList<Character>>();
		
		this.alphabet = new ArrayList<Character>();
		
		this.markovChain = new HashMap<MarkovKey, Float>();

		// Issue Data Adapter to create tables
		this.mda.dropTables();
		this.mda.createTables();
		
		// Train using data set and insert into tables
		System.out.println("Training Data");
		trainData();
		buildChains();
		System.out.println("Complete");
	}
	
	@SuppressWarnings("resource")
	private String[] digestFile(String datasetFile) {
		File file = new File(datasetFile);
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));

		    ArrayList<String> list = new ArrayList<String>();
		    String st;

			while ((st = br.readLine()) != null) {
				list.add(st);
			}
			this.ds = new String[list.size()];
			this.ds = list.toArray(this.ds);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void insertMarkovMap(String context, Character nextChar) {
		if (! this.markovMap.containsKey(context)) {
			this.markovMap.put(context, new ArrayList<Character>());
		}
		this.markovMap.get(context).add(nextChar);
	}
	
	private int countMatches(ArrayList<Character> arr, Character v) {
		if (arr == null) {
			return 0;
		}
		int count = 0;
		for (int i = 0; i < arr.size(); i++) {
			if (arr.get(i) == v) {
				count++;
			}
		}
		return count;
	}
	
	private void updateChain(String context, Character prediction, Float value) {
		MarkovKey mk = new MarkovKey(context, prediction);
		Float currentValue = this.markovChain.get(mk);
		if (currentValue != null) {
			value += currentValue;
		}
		this.markovChain.put(mk, value);
	}
	
	private void buildChains() {
		// Execute as a single transaction to improve performance
		this.mda.setAutoCommit(false);
		for (Map.Entry<String, ArrayList<Character>> entry : this.markovMap.entrySet()) {
			for (int j = 0; j < this.alphabet.size(); j++) {
				String context = entry.getKey();
				ArrayList<Character> chain = entry.getValue();
				Character prediction = this.alphabet.get(j);
				float value = (this.prior + (float) countMatches(chain, prediction));
				updateChain(context, prediction, value);
			}
		}
		// Write Chain into database
		for (Map.Entry<MarkovKey, Float> entry : this.markovChain.entrySet()) {
			MarkovKey mk = entry.getKey();
			Float value = entry.getValue();
			if (value > 0) {
			    this.mda.updateChain(mk.getContext(), mk.getPrediction(), value);
			}
		}
		this.mda.setAutoCommit(true);
	}
	
	private void updateAlphabet(String word) {
		for (int i = 0; i < word.length(); i++) {
			this.alphabet.add(word.charAt(i));
		}
	}
	
	private void sortAlphabet() {
		Collections.sort(this.alphabet);
	}
	
	private void load(String word) {
		updateAlphabet(word);
		for (int i = 0; i < word.length() - order; i++) {
			insertMarkovMap(word.substring(i, i + order), word.charAt(i + order));
		}
	}
	
	private void trainData() {
		if (this.ds == null) {
			return;
		}
		for (int i = 0; i < this.ds.length; i++) {
			load(this.ds[i]);
		}
		sortAlphabet();
	}

	public String getDatabase() {
		return this.mda.getDatabase();
	}
}
