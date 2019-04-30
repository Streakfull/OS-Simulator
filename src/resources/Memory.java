package resources;

import java.util.ArrayList;

import main.OSProcess;

public class Memory {
	private OSProcess[] processes;

	public Memory() {
		this.processes = new OSProcess[1024];
	}

	public int addProcess(OSProcess proc) {
		for(int i = 0;i<processes.length;i++) {
			if(processes[i]==null) {
				processes[i] = proc;
				return i;
			}
		}
		return -1;
	}

	public void removeProcess(int processAddress) {
		processes[processAddress] = null;
	}

	public OSProcess getProcess(int Address) {
		return processes[Address];
	}

}
