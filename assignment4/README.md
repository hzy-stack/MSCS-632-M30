# Assignment 4 — Employee Shift Scheduler

A small application that builds a weekly employee schedule (Monday–Sunday)
across morning / afternoon / evening shifts. It is implemented twice, in
**Python** and **Java**, to demonstrate the same control structures
(conditionals, loops, branching, and helper functions) in two different
languages.

## Rules enforced by the scheduler

- No employee works more than **one shift per day**.
- No employee works more than **5 days per week**.
- Each shift must have **at least 2 employees per day**. If fewer than 2
  preferred employees are available for a shift, the scheduler randomly
  assigns additional employees who have not yet hit their 5-day cap.
- If an employee's preferred shift is **full**, they are reassigned to a
  different shift the same day, or to their preferred shift the next day.

Capacity per shift is set to `MAX_PER_SHIFT = 3` so that conflict resolution
is exercised on the sample data.

## Folder layout

```
assignment4/
├── README.md
├── python/
│   └── scheduler.py
├── java/
│   └── Scheduler.java
└── screenshots/        (add your screenshot(s) of the output here)
```

## Running the Python version

Requires Python 3.8+.

```
cd python
python3 scheduler.py
```

## Running the Java version

Requires JDK 17+.

```
cd java
javac Scheduler.java
java Scheduler
```

Both programs use the same hard-coded sample of 10 employees and the same
random seed (`42`), so the resulting schedules are deterministic and easy to
verify side by side.
