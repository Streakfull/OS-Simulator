# FornOS — Operating System Simulator

A multi-threaded OS simulator built in Java that demonstrates core operating system concepts — process scheduling, memory management, semaphores, and hardware interrupt handling — through the domain of a smart oven appliance. The simulator includes both a Swing GUI and a console interface, and logs real-time process metrics (turnaround time, response time, execution time) to CSV.

## Features

- **Process management** — create, schedule, pause, and terminate processes, each running on its own thread
- **Memory allocation** — fixed-size 1024-slot memory with address tracking
- **Semaphore synchronization** — mutual exclusion on shared resources (CPU, Heater, I/O Hard Disk)
- **Hardware interrupts** — door open/close events pause and resume running processes
- **Kernel mode** — privilege-based operations for safety-critical actions
- **Thermal safety** — daemon process monitors temperature and triggers emergency shutdown above 60 °C
- **Child lock** — password-protected lock mode to prevent unauthorized operation
- **Preset categories** — configurable cooking profiles loaded from CSV (Fish, Chicken, Meat, etc.)
- **Metrics logging** — process performance (PID, turnaround, response, execution time) exported to `data.csv`
- **Dual UI** — interactive Swing GUI with CardLayout and a console-based CLI

## Architecture

```
src/
├── main/                Core OS kernel and UI
│   ├── Engine.java      Kernel — scheduler, process queues, semaphores, interrupts
│   ├── GUI.java         Swing interface (On/Off, Heating, Categories, Door, ...)
│   ├── Screen.java      Console-based CLI
│   ├── OSProcess.java   Process representation with thread lifecycle
│   ├── PCB.java         Process Control Block (PID, state, memory address)
│   ├── States.java      Process states: READY, RUNNING, DOOR_BLOCKED, DOOR_READY
│   ├── Heater.java      Simulated heater hardware with temperature tracking
│   └── Console.java     Output wrapper
├── apps/                Application layer
│   ├── Application.java Abstract base class (priority levels)
│   ├── Heating.java     Temperature control process
│   ├── Categories.java  Cooking preset selection
│   ├── Safety.java      Thermal monitoring daemon (kernel mode)
│   ├── ChildLock.java   Child safety lock
│   └── Save.java        Persist custom categories to CSV
├── resources/           Resource management
│   ├── CPU.java         Process executor (run, pause, resume via PCB)
│   └── Memory.java      Fixed 1024-slot memory allocator
└── Bar/                 Visualization
    ├── BarChart.java    Swing bar chart for process metrics
    ├── Bar.java         Individual bar data
    └── Axis.java        Chart axis configuration
```

### Process Lifecycle

```
User input → Engine.createProcess() → allocate Memory → enqueue PCB
  → Scheduler polls readyQueue → set RUNNING → CPU executes thread
  → Application acquires semaphores → runs → releases → logs metrics
```

## Getting Started

### Prerequisites

- Java SE 8+

### Running

The project is an Eclipse IDE project. Import it directly, or compile from the command line:

```bash
# Compile
javac -d bin src/main/*.java src/apps/*.java src/resources/*.java src/Bar/*.java

# Run (GUI mode)
java -cp bin main.GUI
```

### Configuration

Cooking presets are defined in `src/categories.csv`:

```
Fish,30,10
Chicken,20,12
Meat,30,12
Rice,10,7
Pasta,40,5
```

Format: `Name, Temperature, Time`

