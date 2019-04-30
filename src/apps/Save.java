package apps;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Semaphore;

import main.Engine;

public class Save extends Application {
	private String csvPath = "./src/categories.csv";
	private FileWriter csvWriter;

	public Save() {
		setName("Save Category");
	}

	@Override
	public void run(Engine engine) {
		Semaphore IOHardDisk = engine.getIOHardDisk();
		try {
			if (IOHardDisk.availablePermits() == 0)
				System.out.println("Process is waiting for IO at time:" + engine.getCurrentTime());
			IOHardDisk.acquire();
			String optionsInput = Engine.gui.newCategory.getText();
			String[] options = optionsInput.split(",");
			csvWriter = new FileWriter(csvPath, true);
			csvWriter.append(options[0]);
			csvWriter.append(",");
			csvWriter.append(options[1]);
			csvWriter.append(",");
			csvWriter.append(options[2]);
			csvWriter.append("\n");
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getTime() {
		// TODO Auto-generated method stub
		return 10;
	}

	@Override
	public void Exit(Engine engine) {
		// TODO Auto-generated method stub
		try {
			csvWriter.flush();
			csvWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		engine.getIOHardDisk().release();
	}
}