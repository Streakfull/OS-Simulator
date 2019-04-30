package apps;

import main.Engine;

public abstract class Application {
	public String name;
	public int privelege;
	
	public abstract void run(Engine engin); 	

	public void setName(String name) {
		this.name = name;
	}
	public void setPrivelege(int priv) {
		privelege = priv;
	}
	public abstract void Exit(Engine engine);
	
	public abstract int getTime();
}
