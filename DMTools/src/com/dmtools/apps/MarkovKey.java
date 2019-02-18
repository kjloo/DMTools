package com.dmtools.apps;

public class MarkovKey {

	// Key Pair
	private String context;
	private Character prediction;
	
	public MarkovKey(String context, Character prediction) {
		this.context = context;
		this.prediction = prediction;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		
		if (!(o instanceof MarkovKey)) {
			return false;
		}
		
		MarkovKey mk = (MarkovKey) o;
		return mk.getContext() == this.context && mk.getPrediction() == this.prediction;
	}
	
	@Override
	public int hashCode() {
		return (this.context + this.prediction).hashCode();
	}
	
	public String getContext() {
		return this.context;
	}
	
	public Character getPrediction() {
		return this.prediction;
	}
	
}
