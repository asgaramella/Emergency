package it.polito.tdp.emergency.model;

public class Event implements Comparable<Event> {
	
	public enum EventType { 
		TRIAGE, // the Patient finishes the triage and goes into the waiting space
		TIMEOUT, //the Patient changes state due to a timeout
		FREE_STUDIO // a patient exits from the studio and a new one is called
		} ;
		
		
	private Patient patient ;
	private int time ;
	private EventType type ;
	
	public Event(Patient patient, int time, EventType type) {
		super();
		this.patient = patient;
		this.time = time;
		this.type = type ;
	}


	@Override
	public String toString() {
		return String.format("Event [patient=%s, time=%s, type=%s]", patient, time, type);
	}


	@Override
	public int compareTo(Event other) {
		return this.time-other.time;
	}


	public Patient getPatient() {
		return patient;
	}


	public void setPatient(Patient patient) {
		this.patient = patient;
	}


	public int getTime() {
		return time;
	}


	public void setTime(int time) {
		this.time = time;
	}


	public EventType getType() {
		return type;
	}


	public void setType(EventType type) {
		this.type = type;
	}

	
	
}
