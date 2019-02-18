package com.dmtools.apps;

public class MarkovProbability {

	private String prediction;
	private Float value;
	
	public MarkovProbability(String prediction, Float value) {
		this.prediction = prediction;
		this.value = value;
	}
	
	public String getPrediction() {
		return this.prediction;
	}
	
	public Float getValue() {
		return this.value;
	}
}
