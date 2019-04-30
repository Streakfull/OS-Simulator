package apps;

import java.util.Random;

import main.Console;
import main.Engine;

public class Safety extends Application {
	private Engine e;

	public Safety(int privelege) {
		setName("safety");
		setPrivelege(privelege);
	}

	@Override
	public void run(Engine engine) {
		this.e = engine;
		startProcess.start();

	}

	private Thread startProcess = new Thread() {
		public void run() {
			Random rand = new Random();
			try {
				while (true) {
					Thread.sleep(5000);
					int temp = e.getHeater().getTemp();
					if (temp >60) {
						e.enterKernelMode(privelege);
						e.stopAll();
						e.exitKernelMode();
						e.getConsoleSemaphore().acquire();
						Console.print("All processes have been stopped	 due to exceeding maximum temperature");
						e.getConsoleSemaphore().release();
					}

				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};

	@Override
	public void Exit(Engine engine) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getTime() {
		// TODO Auto-generated method stub
		return 100000;
	}

}
