# MSCS-632-M30 — Programming Languages Assignment

Analysis of syntax, semantics, and memory management across Python,
JavaScript, C++, Rust, and Java.

## Contents

- `Programming_Languages_Report.docx` — full written report
- `part1_section1/` — array-sum programs with intentional syntax errors
  (Python, JavaScript, C++)
- `part1_section2/` — type-system comparison programs
  (Python, JavaScript, C++)
- `part2_section3/` — memory-management programs
  (Rust, Java, C++)
- `screenshots/` — terminal output captured for each program

## How to run

Each subfolder contains source files you can build and run with the
standard toolchain for that language:

```
python3 part1_section1/sum_correct.py
node      part1_section1/sum_correct.js
g++       part1_section1/sum_correct.cpp -o sum && ./sum
rustc -O  part2_section3/memory_demo.rs -o mem  && ./mem
javac     part2_section3/MemoryDemo.java && java -Xmx256m MemoryDemo
```
