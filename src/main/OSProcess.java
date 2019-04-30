package main;

import apps.Application;

public class OSProcess {
	private int processId;
	private int priority;
	private Engine engine;
	private Application application;
	private Long readyTime;
	private Long startTime;
	private Long endTime;
	private boolean over;

	public OSProcess(int processId, int priority, Engine engine, Application application) {
		this.processId = processId;
		this.priority = priority;
		this.engine = engine;
		this.application = application;
		this.over = false;
		startTime=null;
	}

	private Thread startProcess = new Thread() {
		public void run() {
			try {
				application.run(engine);
				Long time = engine.getCurrentTime();
				startTime = time;
				String toBePrinted = "Process " + processId + " for " + application.name + " is Running at time:"
						+ time;
				engine.getConsoleSemaphore().acquire();
				Console.print(toBePrinted);
				engine.getConsoleSemaphore().release();
				Thread.sleep(application.getTime() * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(!over) {
			application.Exit(engine);
			engine.stopProcess(processId);
			}
		}
	};

	public void startProc() {
		try {
			startProcess.start();
		} catch (Exception e) {

		}
	}
	public void kill(){
		this.over = true;
	}

	public void pauseProc() {
		startProcess.suspend();
	}

	public void resumeProc() {
		startProcess.resume();
	}

	public int getProcessID() {
		return processId;
	}

	public void setReadyTime(Long readyTime) {
		this.readyTime = readyTime;
	}

	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	public Long getStartingTime() {
		return startTime;
	}

	public Long getReadyTime() {
		return readyTime;
	}

	public Long getendTime() {
		return endTime;
	}

}
