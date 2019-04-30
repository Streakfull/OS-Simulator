package apps;

import java.util.concurrent.Semaphore;

import main.Console;
import main.Engine;

public class Heating extends Application {
	private int temperature;
	private int time;

	public Heating(int time, int temperature) {
		setName("Normal");
		this.time = time;
		this.temperature = temperature;
	}

	@Override
	public void run(Engine engine) {
		Semaphore heaterSemaphore = engine.getheaterSemaphore();
		try {
			if (heaterSemaphore.availablePermits() == 0) {
				engine.getConsoleSemaphore().acquire();
				Console.print("Process is waiting for Heater at time:" + engine.getCurrentTime());
				engine.getConsoleSemaphore().release();
			}
			heaterSemaphore.acquire();
			engine.startHeater(temperature);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void Exit(Engine engine) {
		engine.stopHeater();
		engine.getheaterSemaphore().release();

	}

	public int getTime() {
		return time;
	}
}
