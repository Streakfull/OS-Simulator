package apps;

import main.Engine;

public class ChildLock extends Application{
	
	public ChildLock(int privelege) {
		setName("Child Lock");
		setPrivelege(privelege);
	}

	@Override
	public void run(Engine engine) {
		engine.enterKernelMode(privelege);
		engine.enterChildMode();
	}

	@Override
	public void Exit(Engine engine) {
		// TODO Auto-generated method stub
		engine.exitKernelMode();
		engine.exitChildMode();
	}

	@Override
	public int getTime() {
		// TODO Auto-generated method stub
		return 100000;
	}

}
