package main;

import java.awt.Button;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import apps.Categories;
import apps.ChildLock;
import apps.Heating;
import apps.Safety;
import apps.Save;

public class GUI extends JFrame implements ActionListener{
	Engine engine;
	CardLayout cardLayout;
	JPanel cards;
	JPanel on;
	JPanel buttons;
	JPanel childLock;
	JTextField childField;
	JLabel wrongPassword;
	JPanel savePanel;
	JButton addButton;
	public JTextField newCategory;
	JPanel categoriesPanel;
	public JLabel categoriesLabel;
	public JTextField categoriesNumber;
	
	public GUI(Engine os) throws IOException, InterruptedException{
		super();
		this.engine = os;
		setSize(500,500);
		setTitle("FornOS");
		cardLayout = new CardLayout();
		cards = new JPanel();
		cards.setLayout(cardLayout);
		this.add(cards);
		on = new JPanel();
		JButton onButton = new JButton("On");
		onButton.addActionListener(this);
		on.add(onButton);
		cards.add(on, "on");
		cardLayout.show(cards, "on");
		buttons = new JPanel();
		JButton normalHeating = new JButton("Normal Heating");
		buttons.add(normalHeating);
		normalHeating.addActionListener(this);
		JButton safety = new JButton("Safety");
		buttons.add(safety);
		safety.addActionListener(this);
		JButton categories = new JButton("Categories");
		buttons.add(categories);
		categories.addActionListener(this);
		JButton save = new JButton("Save");
		buttons.add(save);
		save.addActionListener(this);
		JButton child = new JButton("Child Lock");
		buttons.add(child);
		child.addActionListener(this);
		JButton openDoor = new JButton("Open Door");
		buttons.add(openDoor);
		openDoor.addActionListener(this);
		JButton closeDoor = new JButton("Close Door");
		buttons.add(closeDoor);
		closeDoor.addActionListener(this);
		JButton off = new JButton("Off");
		buttons.add(off);
		off.addActionListener(this);
		cards.add(buttons,"buttons");
		childLock = new JPanel();
		JLabel childLabel = new JLabel("Enter password to unlock:");
		childField = new JTextField(20);
		JButton childButton = new JButton("Unlock");
		childButton.addActionListener(this);
		wrongPassword = new JLabel();
		childLock.add(childLabel);
		childLock.add(childField);
		childLock.add(childButton);
		childLock.add(wrongPassword);
		cards.add(childLock, "child");
		savePanel = new JPanel();
		newCategory = new JTextField(20);
		addButton = new JButton("Add");
		addButton.addActionListener(this);
		savePanel.add(newCategory);
		savePanel.add(addButton);
		cards.add(savePanel, "save");
		categoriesPanel = new JPanel();
		categoriesLabel = new JLabel();
		categoriesNumber = new JTextField(20);
		JButton selectCategory = new JButton("Select");
		selectCategory.addActionListener(this);
		categoriesPanel.add(categoriesLabel);
		categoriesPanel.add(categoriesNumber);
		categoriesPanel.add(selectCategory);
		cards.add(categoriesPanel,"categories");
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e){
		// TODO Auto-generated method stub
		String action = e.getActionCommand();
		switch(action) {
			case "On": 
				cardLayout.show(cards,"buttons");
				engine.turnOn();
				break;
			case "Off":
				cardLayout.show(cards, "on");
			try {
				engine.turnOff();
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
				break;
			case "Normal Heating":
				engine.createProcess(0,new Heating(10,50));
				break;
			case "Safety":
				engine.createProcess(1, new Safety(1));
				break;
			case "Categories":
				cardLayout.show(cards, "categories");
				engine.createProcess(0, new Categories());
				break;
			case "Select":
				Engine.chooseCategory = true;
				cardLayout.show(cards, "buttons");
				break;
			case "Save":
				cardLayout.show(cards, "save");
				break;
			case "Add":
				engine.createProcess(0, new Save());
				cardLayout.show(cards, "buttons");
				break;
			case "Child Lock":
				cardLayout.show(cards,"child");
				engine.createProcess(1, new ChildLock(1));
				break;
			case "Open Door": 
				try {
					engine.handleDoorOpenInterrupt();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
			case "Close Door":
				engine.handleDoorClose();
				break;
			case "Unlock":
				unlock(engine);
				break;
			default:
				break;
		}
		
	}
	
	public void unlock(Engine engine) {
		if(childField.getText().equals("fornos")) {
			engine.exitKernelMode();
			engine.exitChildMode();
			engine.stopChildLockProcess();
			cardLayout.show(cards,"buttons");
		}
		else
			wrongPassword.setText("Wrong Password");
	}
}
