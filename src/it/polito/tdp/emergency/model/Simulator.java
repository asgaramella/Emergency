package it.polito.tdp.emergency.model;

import java.util.PriorityQueue;

import it.polito.tdp.emergency.model.Event.EventType;
import it.polito.tdp.emergency.model.Patient.PatientStatus;

public class Simulator {

	// Simulation parameters

	private int NS; // number of studios

	private int DURATION_TRIAGE = 5 * 60;
	private int DURATION_WHITE = 10 * 60;
	private int DURATION_YELLOW = 15 * 60;
	private int DURATION_RED = 30 * 60;

	private int WHITE_TIMEOUT = 30 * 60;
	private int YELLOW_TIMEOUT = 30 * 60;
	private int RED_TIMEOUT = 60 * 60;

	// World model
	private PriorityQueue<Patient> waitingRoom; // precedenza per lista di attesa è combinazione di codice e tempo di arrivo
	private int occupiedStudios = 0; // no necessario creare la classe studio perchè i diversi studi sono equivalenti

	// Measures of Interest
	private int patientsTreated = 0;
	private int patientsDead = 0;
	private int patientsAbandoned = 0;

	// Event queue
	private PriorityQueue<Event> queue; //lista degli eventi, precedenza per tempo

	public Simulator(int NS) {
		this.NS = NS;

		this.queue = new PriorityQueue<>();
		//la coda prioritaria usa compare e non compareTo della classe
		this.waitingRoom = new PriorityQueue<>(new PatientComparator());
	}

	public void addPatient(Patient patient, int time) {
		patient.setStatus(PatientStatus.NEW);
		Event e= new Event(patient, time+this.DURATION_TRIAGE,EventType.TRIAGE);
		queue.add(e);
	
	}

	public void run() {
		while (!queue.isEmpty()) {
			Event e = queue.poll();
			System.out.println(e);

			switch (e.getType()) {
			case TRIAGE:
				processTriageEvent(e);
				break;
			case TIMEOUT:
				processTimeoutEvent(e);
				break;
			case FREE_STUDIO:
				processFreeStudioEvent(e);
				break;
			}
		}
	}

	/**
	 * A patient finished treatment. The studio is freed, and a new patient is
	 * called in.
	 * 
	 * @param e
	 */
	private void processFreeStudioEvent(Event e) {
		Patient p=e.getPatient();
		//un paziente esce
		this.patientsTreated++;
		p.setStatus(PatientStatus.OUT);
		this.occupiedStudios--;

		//devo chiamare un nuovo paziente dalla sala di attesa
		Patient next= this.waitingRoom.poll();
		if(next!=null){
			int duration=0;
			if(next.getStatus()==PatientStatus.WHITE)
				duration=this.DURATION_WHITE;
			if(next.getStatus()==PatientStatus.YELLOW)
				duration=this.DURATION_YELLOW;
			if(next.getStatus()==PatientStatus.RED)
				duration=this.DURATION_RED;
			
			
			
			this.occupiedStudios++;
			next.setStatus(PatientStatus.TREATING);
			//creo evento per quando paziente uscirà
			queue.add(new Event(next, e.getTime()+duration,EventType.FREE_STUDIO));
		}
		
		
	}

	private void processTimeoutEvent(Event e) {
		Patient p=e.getPatient();
		
		switch(p.getStatus()){
		case WHITE:
			//abbandona, tolgo dalla lista di attesa, stato ad out, aggiorno statistiche
			this.patientsAbandoned++;
			p.setStatus(PatientStatus.OUT);
			this.waitingRoom.remove(p);
			break;
			
		case YELLOW:
			//diventa rosso, cambio status del paziente 
			//ma essendo in una coda prioritaria devo toglierlo e poi rimetterlo
			//infine settare un nuovo timeout
			this.waitingRoom.remove(p);
			p.setStatus(PatientStatus.RED);
			
			//perchè non necessario p.setqueuetime come in riga 192??
			this.waitingRoom.add(p);
			queue.add(new Event(p,e.getTime()+this.RED_TIMEOUT, EventType.TIMEOUT));
			break;
			
		case RED:
			//muori
			this.patientsDead++;
			p.setStatus(PatientStatus.BLACK);
			this.waitingRoom.remove(p);
			break;
			
		case OUT:
		case TREATING:
			//timeout arriva tardi paziente è già stato chiamato,va ignorato
			break;
			
			
		default:
			throw new InternalError("Stato paziente errato"+p.toString());
			
		}
		
		
	}

	/**
	 * Patient goes out of triage. A severity code is assigned. If a studio is
	 * free, then it is immediately assigned. Otherwise, he is put in the waiting
	 * list.
	 * Ogni volta che elaboro evento di tipo triage genero o evento di freestudio o di timeout
	 * @param e
	 */
	private void processTriageEvent(Event e) {
		Patient p= e.getPatient();
		//fine del triage
		
		//devo assegnare codice(random)
		int rand=(int)(Math.random()*3+1);
		if(rand==1)p.setStatus(PatientStatus.WHITE);
		else if(rand==2)p.setStatus(PatientStatus.YELLOW);
		else if(rand==3)p.setStatus(PatientStatus.RED);
		
		//se studio libero, lo mando in cura
		
		if(this.occupiedStudios<this.NS){
			int duration=0;
			if(p.getStatus()==PatientStatus.WHITE)
				duration=this.DURATION_WHITE;
			if(p.getStatus()==PatientStatus.YELLOW)
				duration=this.DURATION_YELLOW;
			if(p.getStatus()==PatientStatus.RED)
				duration=this.DURATION_RED;
			
			this.occupiedStudios++;
			p.setStatus(PatientStatus.TREATING);
			
			queue.add(new Event(p,e.getTime()+duration,EventType.FREE_STUDIO));
		}else{
	    //se no,lo metto in lista di attesa e schedulo evento di timeout
			int timeout=0;
			if(p.getStatus()==PatientStatus.WHITE)
				timeout=this.WHITE_TIMEOUT;
			if(p.getStatus()==PatientStatus.YELLOW)
				timeout=this.YELLOW_TIMEOUT;
			if(p.getStatus()==PatientStatus.RED)
				timeout=this.RED_TIMEOUT;
			
			//OSS il time va impostato prima di metterlo nella coda 
			//la comparazione viene fatta al momento della aggiunta
			p.setQueueTime(e.getTime());
			this.waitingRoom.add(p);
			
			queue.add(new Event(p, e.getTime()+timeout, EventType.TIMEOUT));
		}
	
	}

	public int getPatientsTreated() {
		return patientsTreated;
	}

	public int getPatientsDead() {
		return patientsDead;
	}

	public int getPatientsAbandoned() {
		return patientsAbandoned;
	}
}
