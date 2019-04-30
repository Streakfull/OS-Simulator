package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import apps.Categories;
import apps.ChildLock;
import apps.Heating;
import apps.Safety;
import apps.Save;

public class Screen {
	private Engine engine;

 public Screen(Engine os) throws IOException, InterruptedException {
	 this.engine = os;
	System.out.println("Enter an app number");
	InputStreamReader reader = new InputStreamReader(System.in);
	BufferedReader input = new BufferedReader(reader);
	while (true) {
		if(engine.isChildMode()) {
			while(true) {
				System.out.println("Please enter password:");
				String password = input.readLine();
				if(password.equals("fornos")) {
					engine.exitKernelMode();
					engine.exitChildMode();
					engine.stopChildLockProcess();
					break;
				}
				else
					System.out.println("Wrong Password!");
			}
		}
	
		String process = input.readLine();
		switch (process) {
		case "On":
			os.turnOn();
			break;
		case "Off":
			os.turnOff();
			break;
		case "normalHeating":
			os.createProcess(0,new Heating(10,50));
			break;
		case "safety":
			os.createProcess(1, new Safety(1));
			break;
		case "categories":
			os.createProcess(0, new Categories());
			break;
		case "save":
			os.createProcess(0, new Save());
			break;
		case "child":
			os.createProcess(1, new ChildLock(1));
			break;
		case "openDoor": 
			os.handleDoorOpenInterrupt();
			break;
		case "closeDoor":
			os.handleDoorClose();
			break;
		default:
			break;
		}
	}
 }
}


	

