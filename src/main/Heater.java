package main;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Heater {
	private boolean startFlag;
	private int temperature;
	private boolean started;

	public Heater() {
		this.temperature = 0;
		startFlag = true;
		started = false;
	}

	public void start(int temp) {
		this.temperature = temp;
		startFlag = true;
		if(!started) {
			startProcess.start();
			started = true;
		}
		else 
			startProcess.resume();
		}

	

	private Thread startProcess = new Thread() {
		public void run() {
			Random rand = new Random(); 
			try {
				while (startFlag) {
					Thread.sleep(rand.nextInt(6)*1000);
					int rand_int1 = rand.nextInt(6);
					temperature += rand_int1;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};

	public void increment(int add) {
		this.temperature += add;
	}

	public int getTemp() {
		return temperature;
	}

	public void stopHeater() {
		startFlag = false;
		System.out.println("Current temperature: "+ temperature);
		startProcess.suspend();
		temperature = 0;
	}
}
