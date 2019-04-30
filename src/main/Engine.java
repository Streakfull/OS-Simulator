package main;

import java.awt.Color;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

import javax.swing.JFrame;

import Bar.Axis;
import Bar.Bar;
import Bar.BarChart;
import apps.Application;
import apps.Safety;
import resources.CPU;
import resources.Memory;

public class Engine {
	public static GUI gui;
	public static boolean chooseCategory = false;
	private ArrayBlockingQueue<PCB> readyQueue;
	private ArrayBlockingQueue<PCB> currentlyRunning;
	private ArrayBlockingQueue<PCB> doorOpenBlocked;
	private ArrayBlockingQueue<PCB> blocked;
	private boolean doorOpen;
	private int processNumber;
	private CPU cpu;
	private Memory memory;
	private Semaphore IOHardDisk;
	private Semaphore heaterSemaphore;
	private Semaphore consoleSem;
	private Heater heater;
	private Boolean kernelMode;
	private Long startingTime;
	private Long endingTime;
	private int safetyId;
	private boolean childMode;
	private String dataPath = "./src/data.csv";
	public static String testPath = "./src/data.csv";
	private Semaphore dataSem;
	private FileWriter dataWriter;
	private FileWriter responseWriter;
	private FileWriter utilizationWriter;

	public Engine(CPU cpu, Memory memory, int readySize, int runningSize, int doorOpenBlockedSize, int blockedSize,
			Heater heater) throws IOException {
		this.cpu = cpu;
		this.memory = memory;
		this.processNumber = 0;
		this.readyQueue = new ArrayBlockingQueue<PCB>(readySize);
		this.currentlyRunning = new ArrayBlockingQueue<PCB>(runningSize);
		this.doorOpenBlocked = new ArrayBlockingQueue<PCB>(doorOpenBlockedSize);
		this.blocked = new ArrayBlockingQueue<PCB>(blockedSize);
		this.heater = heater;
		IOHardDisk = new Semaphore(1);
		heaterSemaphore = new Semaphore(1);
		kernelMode = false;
		consoleSem = new Semaphore(1);
		dataSem = new Semaphore(1);
		childMode = false;
		dataWriter = new FileWriter(dataPath, false);
		String[] headings = { "PID", "Name", "TurnAround", "Response", "Execution Time", "CPU Total Running Time" };
		for (int i = 0; i < headings.length; i++) {
			dataWriter.append(headings[i]);
			dataWriter.append(",");
		}
		dataWriter.append("\n");
		dataWriter.close();

	}

	public void createProcess(int priority, Application app) {
		OSProcess newProcess = new OSProcess(processNumber, priority, this, app);
		newProcess.setReadyTime(getCurrentTime());
		int memoryAddress = memory.addProcess(newProcess);
		if (memoryAddress == -1) {
			System.out.println("No space left in memory");
			return;
		}
		createPCB(memoryAddress, app.name);
		processNumber++;
	}

	public Semaphore getIOHardDisk() {
		return IOHardDisk;
	}

	public Semaphore getheaterSemaphore() {
		return heaterSemaphore;
	}

	public Heater getHeater() {
		return heater;
	}

	public void enterKernelMode(int privilege) {
		if (privilege == 1)
			kernelMode = true;
	}

	public void exitKernelMode() {
		kernelMode = false;
	}

	private void nextProcess() {
		PCB pcb = readyQueue.poll();
		States tempState = pcb.getState();
		pcb.setState(States.RUNNING);
		currentlyRunning.add(pcb);
		try {
			if (tempState == States.DOOR_READY)
				cpu.resumeExecution(pcb);
			else
				cpu.execute(pcb);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public long getCurrentTime() {
		return (System.currentTimeMillis() - startingTime) / 1000;
	}

	public void stopProcess(int id) {
		try {
			currentlyRunning.forEach(pcb -> {
				if (pcb.getProcessID() == id) {
					currentlyRunning.remove(pcb);
					int memoryAdress = pcb.getMemoryBaseAddress();
					OSProcess removed = memory.getProcess(memoryAdress);
					memory.removeProcess(memoryAdress);
					try {
						Long time = getCurrentTime();
						String toBePrinted = "Process " + id + " finished at time:" + time;
						removed.setEndTime(time);
						consoleSem.acquire();
						Console.print(toBePrinted);
						consoleSem.release();
						dataSem.acquire();
						dataWriter = new FileWriter(dataPath, true);
						dataWriter.append("" + pcb.getProcessID());
						dataWriter.append(",");
						dataWriter.append("" + pcb.getName());
						dataWriter.append(",");
						dataWriter.append("" + (removed.getendTime() - removed.getReadyTime()));
						dataWriter.append(",");
						dataWriter.append("" + (removed.getStartingTime() - removed.getReadyTime()));
						dataWriter.append(",");
						dataWriter.append("" + (removed.getendTime() - removed.getStartingTime()));
						dataWriter.append(",");
						dataWriter.append("\n");
						dataWriter.close();
						dataSem.release();

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			});

		} catch (Exception e) {
		}
	}

	public void stopAll() {

		currentlyRunning.forEach(pcb -> {
			try {
				if (pcb.getName().equals("safety"))
					return;
				int memoryAdress = pcb.getMemoryBaseAddress();
				OSProcess removed = memory.getProcess(memoryAdress);
				memory.removeProcess(memoryAdress);
				Long time = getCurrentTime();
				removed.setEndTime(time);
				consoleSem.acquire();
				Console.print("Process " + pcb.getProcessID() + " stopped at time:" + time);
				consoleSem.release();
				Long startingTime = removed.getStartingTime();
				Long responseTime = startingTime == null ? removed.getendTime() : startingTime - removed.getReadyTime();
				Long ExecutionTime = startingTime == null ? 0 : removed.getendTime() - removed.getStartingTime();
				dataSem.acquire();
				dataWriter = new FileWriter(dataPath, true);
				dataWriter.append("" + pcb.getProcessID());
				dataWriter.append(",");
				dataWriter.append("" + pcb.getName());
				dataWriter.append(",");
				dataWriter.append("" + (removed.getendTime() - removed.getReadyTime()));
				dataWriter.append(",");
				dataWriter.append("" + (responseTime));
				dataWriter.append(",");
				dataWriter.append("" + (ExecutionTime));
				dataWriter.append(",");
				dataWriter.append("\n");
				dataWriter.close();
				dataSem.release();
				currentlyRunning.remove(pcb);
				removed.kill();
				stopHeater();
				IOHardDisk.drainPermits();
				heaterSemaphore.drainPermits();
				IOHardDisk.release();
				heaterSemaphore.release();
			} catch (Exception e) {
				// e.printStackTrace();
			}
			;
		});

	}

	private void createPCB(int address, String appName) {
		readyQueue.add(new PCB(processNumber, address, States.READY, appName));
	}

	public Thread scheduler = new Thread() {
		@Override
		public void run() {
			while (true) {
				if (!doorOpen) {
					if (currentlyRunning.remainingCapacity() > 0) {
						;
						if (!readyQueue.isEmpty()) {
							nextProcess();
						}
					}
				}
			}
		}
	};

	public void handleDoorOpenInterrupt() throws InterruptedException {
		doorOpen = true;
		while (currentlyRunning.size() > 0) {
			PCB pcb = currentlyRunning.poll();
			pcb.setState(States.DOOR_BLOCKED);
			doorOpenBlocked.add(pcb);
			cpu.pauseExecution(pcb);
			try {
				String toBePrinted = "Process " + pcb.getProcessID() + " Blocked";
				consoleSem.acquire();
				Console.print(toBePrinted);
				consoleSem.release();
			} catch (Exception e) {

			}

		}
	}

	public void handleDoorClose() {
		while (doorOpenBlocked.size() > 0) {
			PCB pcb = doorOpenBlocked.poll();
			pcb.setState(States.DOOR_READY);
			readyQueue.add(pcb);
			try {
				String toBePrinted = "Process" + pcb.getProcessID() + " ready";
				consoleSem.acquire();
				Console.print(toBePrinted);
				consoleSem.release();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		doorOpen = false;
	}

	public void turnOn() {
		this.startingTime = System.currentTimeMillis();
		// write to csv file
	}

	public void turnOff() throws InterruptedException, IOException {
		this.endingTime = System.currentTimeMillis();
		currentlyRunning.forEach(pcb -> {
			int memoryAdress = pcb.getMemoryBaseAddress();
			OSProcess removed = memory.getProcess(memoryAdress);
			removed.setEndTime(getCurrentTime());
			Long responseTime = removed.getStartingTime() == null ? 0:removed.getStartingTime() - removed.getReadyTime();
			Long ExecutionTime = removed.getStartingTime() == null?0:removed.getendTime() - removed.getStartingTime();
			try {
				dataSem.acquire();
				dataWriter = new FileWriter(dataPath, true);
				dataWriter.append("" + pcb.getProcessID());
				dataWriter.append(",");
				dataWriter.append("" + pcb.getName());
				dataWriter.append(",");
				dataWriter.append("" + (removed.getendTime() - removed.getReadyTime()));
				dataWriter.append(",");
				dataWriter.append("" + responseTime);
				dataWriter.append(",");
				dataWriter.append("" + ExecutionTime);
				dataWriter.append(",");
				dataWriter.append("\n");
				dataWriter.close();
				dataSem.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		dataSem.acquire();
		dataWriter = new FileWriter(dataPath, true);
		dataWriter.append("");
		dataWriter.append(",");
		dataWriter.append("");
		dataWriter.append(",");
		dataWriter.append("");
		dataWriter.append(",");
		dataWriter.append("");
		dataWriter.append(",");
		dataWriter.append("");
		dataWriter.append(",");
		dataWriter.append("" + ((endingTime - startingTime) / 1000));
		dataWriter.append(",");
		dataWriter.append("\n");
		dataWriter.close();
		dataSem.release();

		generateAnalysis();

	}

	public void startHeater(int temp) {
		heater.start(temp);
	}

	public void stopHeater() {
		heater.stopHeater();
	}

	public Long getStartingTime() {
		return startingTime;
	}

	public void setSafetyId(int id) {
		this.safetyId = id;
	}

	public Semaphore getConsoleSemaphore() {
		return consoleSem;
	}

	public void enterChildMode() {
		childMode = true;
	}

	public void exitChildMode() {
		childMode = false;
	}

	public boolean isChildMode() {
		return childMode;
	}

	public void stopChildLockProcess() {
		currentlyRunning.forEach(pcb -> {
			if (pcb.getName().equals("Child Lock")) {
				currentlyRunning.remove(pcb);
				int memoryAdress = pcb.getMemoryBaseAddress();
				OSProcess removed = memory.getProcess(memoryAdress);
				memory.removeProcess(memoryAdress);

				try {
					Long time = getCurrentTime();
					removed.setEndTime(time);
					consoleSem.acquire();
					Console.print("Process " + pcb.getProcessID() + " finished at time:" + getCurrentTime());
					consoleSem.release();
					dataSem.acquire();
					dataWriter = new FileWriter(dataPath, true);
					dataWriter.append("" + pcb.getProcessID());
					dataWriter.append(",");
					dataWriter.append("" + pcb.getName());
					dataWriter.append(",");
					dataWriter.append("" + (removed.getendTime() - removed.getReadyTime()));
					dataWriter.append(",");
					dataWriter.append("" + (removed.getStartingTime() - removed.getReadyTime()));
					dataWriter.append(",");
					dataWriter.append("" + (removed.getendTime() - removed.getStartingTime()));
					dataWriter.append(",");
					dataWriter.append("\n");
					dataWriter.close();
					dataSem.release();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void generateAnalysis() throws IOException {
		JFrame turnAroundFrame = new JFrame();
		JFrame responseFrame = new JFrame();
		JFrame utilizationFrame = new JFrame();
		turnAroundFrame.setSize(1000, 430);
		responseFrame.setSize(1000, 430);
		utilizationFrame.setSize(1000, 430);
		ArrayList<String[]> data = new ArrayList<String[]>();
		BufferedReader br = new BufferedReader(new FileReader(testPath));
		String line;
		while ((line = br.readLine()) != null) {
			System.out.println(line);
			data.add(line.split(","));
		}
		br.close();
		int maxResponse = 0;
		int maxExecution = 0;
		int maxTurnAround = 0;
		int totalTime = 0;
		Double sumExecution = 0.0;
		Double sumResponse = 0.0;
		Double sumTurnAround = 0.0;
		ArrayList<Bar> executionV = new ArrayList<Bar>();
		ArrayList<Bar> responseV = new ArrayList<Bar>();
		ArrayList<Bar> turnAroundV = new ArrayList<Bar>();
		for (int i = 1; i < data.size(); i++) {
			String[] arr = data.get(i);
			if (i == data.size() - 1) {
				totalTime = Integer.parseInt(arr[5]);
				break;
			}
			int turnAround = Integer.parseInt(arr[2]);
			int execution = Integer.parseInt(arr[4]);
			int response = Integer.parseInt(arr[3]);
			sumExecution += execution;
			sumTurnAround += turnAround;
			sumResponse += response;
			executionV.add(new Bar(execution, Color.GREEN, arr[1]));
			responseV.add(new Bar(response, Color.BLUE, arr[1]));
			turnAroundV.add(new Bar(turnAround, Color.BLACK, arr[1]));
			if (turnAround > maxTurnAround) {
				maxTurnAround = turnAround;
			}
			if (execution > maxExecution) {
				maxExecution = execution;
			}
			if (response > maxResponse) {
				maxResponse = response;
			}

		}

		int primaryT = maxTurnAround / 4;
		int primaryR = maxResponse / 4;
		int primaryU = maxExecution / 4;
		int secondaryT = primaryT / 2;
		int secondaryR = primaryR / 2;
		int secondaryU = primaryU / 2;
		int lastT = primaryT / 4;
		int lastR = primaryR / 4;
		int lastU = primaryU / 4;
		double avgT = sumTurnAround / (data.size() - 2);
		double avgU = sumExecution / (totalTime * 3);
		double avgR = sumResponse / (data.size() - 2);
		primaryT = zeroCheck(primaryT);
		primaryR = zeroCheck(primaryR);
		primaryU = zeroCheck(primaryU);
		secondaryT = zeroCheck(secondaryT);
		secondaryR = zeroCheck(secondaryR);
		secondaryU = zeroCheck(secondaryU);
		lastT = zeroCheck(lastT);
		lastR = zeroCheck(lastR);
		lastU = zeroCheck(lastU);

		Axis responseY = new Axis(maxResponse, 0, primaryR, secondaryR, lastR, "Response Time");
		Axis turnAroundY = new Axis(maxTurnAround, 0, primaryT, secondaryT, lastT, "Turn Around Time");
		Axis executionY = new Axis(maxExecution, 0, primaryU, secondaryU, lastU, "Execution Time");
		BarChart barChart1 = new BarChart(turnAroundV, turnAroundY, "Turn Around avg:" + Math.round(avgT));
		BarChart barChart2 = new BarChart(responseV, responseY, "Response avg:" + Math.round(avgR));
		BarChart barChart3 = new BarChart(executionV, executionY, "Utilization:" + Math.round(avgU * 100) + "%");
		turnAroundFrame.add(barChart1);
		responseFrame.add(barChart2);
		utilizationFrame.add(barChart3);
		turnAroundFrame.setVisible(true);
		responseFrame.setVisible(true);
		utilizationFrame.setVisible(true);

	}

	public static int zeroCheck(int x) {
		if (x <= 0)
			return 1;
		else
			return x;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Memory memory = new Memory();
		CPU cpu = new CPU(memory);
		Heater heater = new Heater();
		Engine os = new Engine(cpu, memory, 5, 3, 5, 5, heater);
		os.scheduler.start();
		//Screen screen = new Screen(os);
		//os.scheduler.start();
		//Screen screen = new Screen(os);
		gui = new GUI(os);
	}

}
