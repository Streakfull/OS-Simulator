package apps;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import main.Console;
import main.Engine;

public class Categories extends Application {
	private String csvPath = "./src/categories.csv";
	private int chosenTime = 10;

	public Categories() {
		setName("Categories");
	}

	@Override
	public void run(Engine engin) {
		try {
			String line;
			Semaphore IOHardDisk = engin.getIOHardDisk();
			if (IOHardDisk.availablePermits() == 0)
				Console.print("Process is waiting for IO at time:" + engin.getCurrentTime());
			IOHardDisk.acquire();
			String output = "";
			output += "<html>Please pick and confirm an option";
			BufferedReader br = new BufferedReader(new FileReader(csvPath));
			ArrayList<String> options = new ArrayList<String>();
			while ((line = br.readLine()) != null) {
				output += "<br/>" + line;
				options.add(line);
			}
			engin.gui.categoriesLabel.setText(output);
			while(!Engine.chooseCategory) {
				//wait
			}
			Engine.chooseCategory = false;
			int choice = Integer.parseInt(Engine.gui.categoriesNumber.getText());
			String[] optionsSplit = options.get(choice).split(",");
			Semaphore heaterSemaphore = engin.getheaterSemaphore();
			chosenTime = Integer.parseInt(optionsSplit[2]);
			int temperature = Integer.parseInt(optionsSplit[1]);
			br.close();
			if (heaterSemaphore.availablePermits() == 0)
				Console.print("Process is waiting for Heater at time:" + engin.getCurrentTime());
			heaterSemaphore.acquire();
			engin.startHeater(temperature);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void Exit(Engine engine) {
		engine.stopHeater();
		engine.getIOHardDisk().release();
		engine.getheaterSemaphore().release();

	}

	@Override
	public int getTime() {
		// TODO Auto-generated method stub
		return chosenTime;
	}

}
