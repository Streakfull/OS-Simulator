package resources;

import main.OSProcess;
import main.PCB;

public class CPU {
	private Memory memory;

	public CPU(Memory memory) {
		this.memory = memory;
	}

	public void execute(PCB pcb) throws InterruptedException {
		int memoryAddress = pcb.getMemoryBaseAddress();
		OSProcess proc = memory.getProcess(memoryAddress);
		proc.startProc();
	}
	
	public void pauseExecution(PCB pcb) {
		int memoryAddress = pcb.getMemoryBaseAddress();
		OSProcess proc = memory.getProcess(memoryAddress);
		proc.pauseProc();
	}
	
	public void resumeExecution(PCB pcb) {
		int memoryAddress = pcb.getMemoryBaseAddress();
		OSProcess proc = memory.getProcess(memoryAddress);
		proc.resumeProc();
	}
}
