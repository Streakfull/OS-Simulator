package main;

public class PCB {
	private int processID;
	private int memoryBaseAddress;
	private States state;
	private String appName;
	
	public PCB (int processID,int memoryBaseAddress,States state,String appName) {
		this.processID = processID;
		this.memoryBaseAddress = memoryBaseAddress;
		this.state = state;
		this.appName = appName;
	}
	public int getProcessID() {
		return processID;
	}
	public void setProcessID(int processID) {
		this.processID = processID;
	}
	public int getMemoryBaseAddress() {
		return memoryBaseAddress;
	}
	public void setMemoryBaseAddress(int memoryBaseAddress) {
		this.memoryBaseAddress = memoryBaseAddress;
	}
	public States getState() {
		return state;
	}
	public void setState(States state) {
		this.state = state;
	}
	public String getName() {
		return appName;
	}
	
}

