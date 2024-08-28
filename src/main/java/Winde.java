import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Winde {
    public static void main(String[] args) {
        /* String logo =
                  " ____        _        \n"
                + "|  _ \\ _   _| | _____ \n"
                + "| | | | | | | |/ / _ \\\n"
                + "| |_| | |_| |   <  __/\n"
                + "|____/ \\__,_|_|\\_\\___|\n";

         */
        // System.out.println("Hello from\n" + "Winde");
        greet();
        loadTasks();

        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        while (!(input.equals("bye"))) {
            Commands command = getCommandType(input);
            try {
                switch (command) {
                    case LIST -> list();
                    case DELETE -> {
                        handleDeleteException(input);
                    }
                    case MARK -> {
                        handleMarkException(input);
                    }
                    case UNMARK -> {
                        handleUnmarkException(input);
                    }
                    case TODO -> {
                        handleTodoCommand(input);
                    }
                    case DEADLINE -> {
                        handleDeadlineCommand(input);
                    }
                    case EVENT -> {
                        handleEventCommand(input);
                    }
                    default -> {
                        throw new UnsupportedCommandException("TYPE /HELP FOR HELP STOOPIDD");
                    }
                }
            } catch (EmptyDescriptionException ede) {
                System.out.println("I NEED TO KNOW WHAT I'M ADDING! " + ede.getMessage());
            } catch (TooManyParametersException tmp) {
                System.out.println("ONE AT A TIME! " + tmp.getMessage());
            } catch (UnsupportedCommandException uce) {
                System.out.println("TYPE /HELP FOR HELP STOOPIDD: " + uce.getMessage());
            }
            input = scanner.nextLine();
        }
        exit();
    }


    private final static List<Task> reminder = new ArrayList<Task>();
    private static final String WINDE_FILE = "./src/main/java/WindeTasks.txt";

    enum Commands {
        TODO, DEADLINE, EVENT, LIST, DELETE, BYE, MARK, UNMARK, UNKNOWN
    }

    private static void loadTasks() {
        File file = new File(WINDE_FILE);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch(IOException ioe) {
            System.out.println("Error in creating file" + ioe.getMessage());
        }
        try {
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line = br.readLine();
                String complete = "X";
                Task task;
                while (line != null) {
                    if (line.startsWith("T")) {
                        String[] instr = line.split(" \\| ");
                        task = new Todos(instr[2], (complete.equals(instr[1]) ? true : false));
                    } else if (line.startsWith("D")) {
                        String[] instr = line.split(" \\| ");
                        task = new Deadline(instr[2], (complete.equals(instr[1]) ? true : false), instr[3]);
                    } else {
                        String[] instr = line.split(" \\| ");
                        String[] when = instr[3].split("-");
                        task = new Event(instr[2], (complete.equals(instr[1]) ? true : false), when[0], when[1]);
                    }
                    reminder.add(task);
                    line = br.readLine();
                }
            }
        } catch (IOException ioe) {
            System.out.println("Error Loading Tasks to File: " + ioe.getMessage());
        }
    }

    private static void saveTasks() {
        try {
            FileWriter fw = new FileWriter(WINDE_FILE);
            for (Task tasks : reminder) {
                fw.write(tasks.toString() + "\n");
            }
            fw.close();
        } catch(IOException ioe) {
            System.out.println("Error Saving Tasks to File: " + ioe.getMessage());
        }
    }

    private static Commands getCommandType(String input) {
        if (input.startsWith("todo")) {
            return Commands.TODO;
        } else if (input.startsWith("deadline")) {
            return Commands.DEADLINE;
        } else if (input.startsWith("event")) {
            return Commands.EVENT;
        } else if (input.equals("list")) {
            return Commands.LIST;
        } else if (input.startsWith("delete")) {
            return Commands.DELETE;
        } else if (input.equals("bye")) {
            return Commands.BYE;
        } else if (input.startsWith("mark")) {
            return Commands.MARK;
        } else if (input.startsWith("unmark")) {
            return Commands.UNMARK;
        } else {
            return Commands.UNKNOWN;
        }
    }

    public static void handleDeleteException (String input) throws EmptyDescriptionException, TooManyParametersException {
        String[] command = input.split(" ");
        if (command.length == 2) {
            Task deleted = reminder.remove(Integer.parseInt(command[1]) - 1);
            System.out.print("Noted. I've removed this task:\n" + "    " + deleted.toString() + "\n");
            System.out.println("Now you have " + reminder.size() + " tasks in the list.");
        } else if (command.length < 2) {
            throw new EmptyDescriptionException("I NEED TO KNOW WHAT I'M DELETING!");
        } else {
            throw new TooManyParametersException("ONE AT A TIME!");
        }
    }

    private static void handleEventCommand(String input) throws EmptyDescriptionException {
        String[] command = input.split(" ", 2);
        if (command.length == 2) {
            String[] order = command[1].split(" /from ");
            if (order.length == 2) {
                String[] fillerName = order[1].split(" /to ");
                Event e = new Event(order[0], fillerName[0], fillerName[1]);
                reminder.add(e);
                System.out.println("Got it. I've added this task:");
                System.out.println("    " + e.toString());
                System.out.println("Now you have " + reminder.size() + " tasks in the list.");
            } else {
                throw new EmptyDescriptionException("WHEN EVENT DATE!");
            }
        } else {
            throw new EmptyDescriptionException("I NEED TO KNOW WHAT I'M EVENT-ING!");
        }
    }
    private static void handleDeadlineCommand (String input) throws EmptyDescriptionException {
        String[] command = input.split(" ", 2);
        if (command.length == 2) {
            String[] order = command[1].split(" /by ");
            if (order.length == 2) {
                Deadline d = new Deadline(order[0], order[1]);
                reminder.add(d);
                System.out.println("Got it. I've added this task:");
                System.out.println("    " + d.toString());
                System.out.println("Now you have " + reminder.size() + " tasks in the list.");
            } else {
                throw new EmptyDescriptionException("WHEN DEADLINE END!");
            }
        } else {
            throw new EmptyDescriptionException("I NEED TO KNOW WHAT I'M DEADLINING!");
        }
    }
    private static void handleTodoCommand (String input) throws EmptyDescriptionException {
        String[] command = input.split(" ", 2);
        if (command.length == 2) {
            Todos td = new Todos(command[1]);
            reminder.add(td);
            System.out.println("Got it. I've added this task:");
            System.out.println("    " + td.toString());
            System.out.println("Now you have " + reminder.size() + " tasks in the list.");
        } else {
            throw new EmptyDescriptionException("I NEED TO KNOW WHAT I'M TODO-ING!");
        }
    }
    private static void handleUnmarkException (String input) throws EmptyDescriptionException, TooManyParametersException {
        String[] command = input.split(" ");
        if (command.length == 2) {
            System.out.print("OK, I've marked this task as not done yet:\n" + "    ");
            reminder.get(Integer.parseInt(command[1]) - 1).unmark();
            task(Integer.parseInt(command[1]));
            System.out.println();
        } else if (command.length < 2) {
            throw new EmptyDescriptionException("I NEED TO KNOW WHAT I'M MARKING!");
        } else {
            throw new TooManyParametersException("ONE AT A TIME!");
        }
    }
    public static void handleMarkException (String input) throws EmptyDescriptionException, TooManyParametersException {
        String[] command = input.split(" ");
        if (command.length == 2) {
            System.out.print("Nice! I've marked this task as done:\n" + "    ");
            reminder.get(Integer.parseInt(command[1]) - 1).mark();
            task(Integer.parseInt(command[1]));
            System.out.println();
        } else if (command.length < 2) {
            throw new EmptyDescriptionException("I NEED TO KNOW WHAT I'M MARKING!");
        } else {
            throw new TooManyParametersException("ONE AT A TIME!");
        }
    }

    public static void task(int i) {
        System.out.print(reminder.get(i - 1).toString());
    }
    public static void add(String action) {
        Task t = new Task(action);
        reminder.add(t);
        System.out.println("added: " + action);
    }

    public static void list() {
        System.out.println("Here are the tasks in your list:");
        if (reminder.size() == 0) {
            System.out.println("Hurray you got nothing to do!");
        } else {
            for (int i = 1; i <= reminder.size(); i++) {
                System.out.print(i + ".");
                task(i);
                System.out.println();
            }
        }
    }

    public static void greet() {
        System.out.println("Hello! I'm Winde\n" + "What can I do for you?");
    }

    public static void exit() {
        saveTasks();
        System.out.println("Bye. Hope to see you again soon!");
    }
    static class EmptyDescriptionException extends Exception {
        public EmptyDescriptionException(String message) {
            super(message);
        }
    }
    static class TooManyParametersException extends Exception {
        public TooManyParametersException(String message) {
            super(message);
        }
    }
    static class UnsupportedCommandException extends Exception {
        public UnsupportedCommandException(String message) {
            super(message);
        }
    }
}

class Task {
    protected String action;
    protected boolean complete;

    public Task(String action) {
        this.action = action;
        this.complete = false;
    }

    public Task(String action, boolean complete) {
        this.action = action;
        this.complete = complete;
    }

    public String isCompleted() {
        return (complete ? "X" : "O"); // mark done task with X
    }

    public void mark() {
        complete = true;
    }

    public void unmark() {
        complete = false;
    }

    @Override
    public String toString() {
        return (complete ? "X" : "O") + " | " + action;
    }
}

class Todos extends Task {

    public Todos(String action) {
        super(action);
    }

    public Todos(String action, boolean complete) {
        super(action, complete);
    }

    @Override
    public String toString() {
        return "T | " + super.toString();
    }
}

class Deadline extends Task {

    protected String date;

    public Deadline(String action, String date) {
        super(action);
        this.date = date;
    }

    public Deadline(String action, boolean complete, String date) {
        super(action, complete);
        this.date = date;
    }

    @Override
    public String toString() {
        return "D | " + super.toString() + " | " + date;
    }
}

class Event extends Task {

    protected String start;
    protected String end;

    public Event(String action, String start, String end) {
        super(action);
        this.start = start;
        this.end = end;
    }

    public Event(String action, boolean complete, String start, String end) {
        super(action, complete);
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return "E | " + super.toString() + " | " + start + "-" + end;
    }
}
