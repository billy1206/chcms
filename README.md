# Community Health Clinic Management System (CHCMS)

Java Swing desktop application built on the MVC architecture, with plain-text
file persistence, hand-written searching/sorting algorithms and JUnit 5 tests.

## Requirements
- JDK 17 or newer (developed and tested on JDK 21)
- JUnit 5 (only needed to run the tests)

## Project layout
```
chcms/
├── src/
│   ├── Main.java                 application entry point
│   ├── model/                    Patient, Doctor, Administrator, Appointment,
│   │                             Treatment, Clinic, Person, AppointmentStatus
│   ├── view/                     Swing screens (MainMenuFrame + 5 panels)
│   ├── controller/               5 controllers, one per use-case area
│   ├── persistence/              Repository interface, FileRepository, CsvMapper
│   ├── util/                     Validator, SearchUtil, SortUtil, IdGenerator
│   └── exception/                ValidationException
├── test/                         4 JUnit 5 test classes (32 test cases)
├── data/                         text files written at runtime
├── docs/                         UML class diagram (.png / .svg / .dot source)
└── screenshots/                  reference screenshots of a working run
```

## Run from the command line
```bash
javac -d build -encoding UTF-8 $(find src -name "*.java")
java -cp build Main
```
The first launch seeds two doctors, two patients and two appointments so the
screens are not empty. Delete the `data` folder to start clean.

## Run in Visual Studio Code
Note: this needs **Visual Studio Code**, not Visual Studio — Visual Studio does
not support Java.

1. Install the **Extension Pack for Java** (publisher: Microsoft). It includes
   the language server, the debugger and the Test Runner.
2. **File > Open Folder** and select this `chcms` folder — open the folder that
   contains `src`, not `src` itself, or the packages will not resolve.
3. A `.vscode/settings.json` is already included, so `src` and `test` are
   registered as source roots automatically. Wait for "Java: Ready" in the
   status bar.
4. Open `src/Main.java` and click **Run** above the `main` method, or press F5
   (a launch configuration named "Run CHCMS" is included).
5. To run the tests, open the **Testing** flask icon in the left sidebar and
   press the play button. The Test Runner extension supplies the JUnit 5
   runtime, so no jar download is needed.

Troubleshooting: if classes appear unresolved, open the Command Palette
(Ctrl+Shift+P / Cmd+Shift+P) and run **Java: Clean Java Language Server
Workspace**, then reload.

## Run in IntelliJ IDEA
1. **File > New > Project from Existing Sources**, select this folder.
2. Right-click `src` > **Mark Directory as > Sources Root**.
3. Right-click `test` > **Mark Directory as > Test Sources Root**.
4. Open any test class, click the red bulb on `import org.junit.jupiter.api.Test;`
   and choose **Add 'JUnit5' to classpath**.
5. Run `Main` for the GUI, or right-click the `test` folder > **Run 'All Tests'**.

## Run in Eclipse
1. **File > New > Java Project**, untick "Use default location", pick this folder.
2. Right-click the project > **Build Path > Add Libraries > JUnit > JUnit 5**.
3. Run `Main` as a Java Application; run the `test` folder as a JUnit Test.

## Data files
Records are stored in `data/*.txt`, one record per line, fields separated by `|`.
Files load on start-up and save after every create/update/delete, as well as via
the **Save all data** button. A malformed line is reported on the console and
skipped rather than crashing the application.

## Algorithms implemented (Task 8)
| Algorithm | Location | Used by |
|---|---|---|
| Linear Search | `util.SearchUtil.linearSearch` | keyword search on every screen |
| Binary Search | `util.SearchUtil.binarySearch` | exact lookup by record ID |
| Bubble Sort | `util.SortUtil.bubbleSort` | sort by name |
| Quick Sort | `util.SortUtil.quickSort` | sort by date, fee, age, cost |
