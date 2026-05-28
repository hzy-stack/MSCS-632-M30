import java.util.*;

/** Employee Shift Scheduler -- Java implementation. */
public class Scheduler {

    static final String[] DAYS = {
        "Monday", "Tuesday", "Wednesday", "Thursday",
        "Friday", "Saturday", "Sunday"
    };
    static final String[] SHIFTS = {"morning", "afternoon", "evening"};

    static final int MIN_PER_SHIFT = 2;
    static final int MAX_PER_SHIFT = 3;
    static final int MAX_DAYS_PER_EMPLOYEE = 5;

    static Map<String, Map<String, String>> employees = buildEmployees();
    static Map<String, Map<String, List<String>>> schedule = new LinkedHashMap<>();
    static Map<String, Integer> daysWorked = new HashMap<>();
    static List<String> notes = new ArrayList<>();
    static Random rng = new Random(42);

    public static void main(String[] args) {
        initializeSchedule();
        for (String emp : employees.keySet()) {
            daysWorked.put(emp, 0);
        }

        // Pass 1: honor preferences
        for (Map.Entry<String, Map<String, String>> entry : employees.entrySet()) {
            String emp = entry.getKey();
            for (Map.Entry<String, String> pref : entry.getValue().entrySet()) {
                assignWithConflictResolution(emp, pref.getKey(), pref.getValue());
            }
        }

        // Pass 2: enforce minimum staffing
        fillMinimumStaffing();

        printSchedule();
    }

    static void initializeSchedule() {
        for (String day : DAYS) {
            Map<String, List<String>> shifts = new LinkedHashMap<>();
            for (String shift : SHIFTS) {
                shifts.put(shift, new ArrayList<>());
            }
            schedule.put(day, shifts);
        }
    }

    static boolean isShiftFull(String day, String shift) {
        return schedule.get(day).get(shift).size() >= MAX_PER_SHIFT;
    }

    static boolean workedThatDay(String employee, String day) {
        for (String shift : SHIFTS) {
            if (schedule.get(day).get(shift).contains(employee)) {
                return true;
            }
        }
        return false;
    }

    static boolean tryAssign(String employee, String day, String shift) {
        if (daysWorked.get(employee) >= MAX_DAYS_PER_EMPLOYEE) return false;
        if (workedThatDay(employee, day)) return false;
        if (isShiftFull(day, shift)) return false;
        schedule.get(day).get(shift).add(employee);
        daysWorked.merge(employee, 1, Integer::sum);
        return true;
    }

    static void assignWithConflictResolution(String employee, String day, String preferredShift) {
        if (tryAssign(employee, day, preferredShift)) {
            return;
        }

        for (String alt : SHIFTS) {
            if (alt.equals(preferredShift)) continue;
            if (tryAssign(employee, day, alt)) {
                notes.add(employee + ": conflict on " + day + " " + preferredShift
                        + " -> reassigned to " + day + " " + alt + ".");
                return;
            }
        }

        int idx = Arrays.asList(DAYS).indexOf(day);
        String nextDay = DAYS[(idx + 1) % DAYS.length];
        if (tryAssign(employee, nextDay, preferredShift)) {
            notes.add(employee + ": conflict on " + day + " " + preferredShift
                    + " -> reassigned to " + nextDay + " " + preferredShift + ".");
            return;
        }

        notes.add(employee + ": could not honor " + day + " " + preferredShift
                + " (no available shifts).");
    }

    static void fillMinimumStaffing() {
        List<String> empList = new ArrayList<>(employees.keySet());
        for (String day : DAYS) {
            for (String shift : SHIFTS) {
                while (schedule.get(day).get(shift).size() < MIN_PER_SHIFT) {
                    List<String> candidates = new ArrayList<>();
                    for (String e : empList) {
                        if (daysWorked.get(e) < MAX_DAYS_PER_EMPLOYEE
                                && !workedThatDay(e, day)) {
                            candidates.add(e);
                        }
                    }
                    if (candidates.isEmpty()) {
                        System.out.println("  WARNING: cannot meet minimum on "
                                + day + " " + shift);
                        break;
                    }
                    String chosen = candidates.get(rng.nextInt(candidates.size()));
                    schedule.get(day).get(shift).add(chosen);
                    daysWorked.merge(chosen, 1, Integer::sum);
                }
            }
        }
    }

    static void printSchedule() {
        String bar = repeat("=", 80);
        System.out.println(bar);
        String title = " Weekly Employee Schedule ";
        int pad = (80 - title.length()) / 2;
        System.out.println(repeat("=", pad) + title + repeat("=", 80 - pad - title.length()));
        System.out.println(bar);

        int colWidth = 22;
        StringBuilder header = new StringBuilder(padRight("Day", 10));
        for (String s : SHIFTS) {
            header.append(padRight(capitalize(s), colWidth));
        }
        System.out.println(header);
        System.out.println(repeat("-", 80));

        for (String day : DAYS) {
            StringBuilder row = new StringBuilder(padRight(day, 10));
            for (String shift : SHIFTS) {
                List<String> people = schedule.get(day).get(shift);
                String cell = people.isEmpty() ? "(empty)" : String.join(", ", people);
                row.append(padRight(cell, colWidth));
            }
            System.out.println(row);
        }
        System.out.println();
        System.out.println("Days worked per employee:");
        List<String> emps = new ArrayList<>(daysWorked.keySet());
        Collections.sort(emps);
        for (String e : emps) {
            System.out.println("  " + padRight(e, 8) + " " + daysWorked.get(e));
        }

        if (!notes.isEmpty()) {
            System.out.println();
            System.out.println("Conflict resolution notes:");
            for (String n : notes) {
                System.out.println("  - " + n);
            }
        }
    }

    static String padRight(String s, int width) {
        if (s.length() >= width) return s.substring(0, width - 1) + " ";
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < width) sb.append(' ');
        return sb.toString();
    }

    static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(s);
        return sb.toString();
    }

    static String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    static Map<String, Map<String, String>> buildEmployees() {
        Map<String, Map<String, String>> m = new LinkedHashMap<>();
        m.put("Alice", prefs(
                "Monday", "morning",
                "Tuesday", "morning",
                "Wednesday", "morning",
                "Thursday", "afternoon",
                "Friday", "morning"));
        m.put("Bob", prefs(
                "Monday", "afternoon",
                "Tuesday", "afternoon",
                "Wednesday", "evening",
                "Thursday", "evening",
                "Friday", "afternoon"));
        m.put("Carol", prefs(
                "Monday", "evening",
                "Tuesday", "evening",
                "Wednesday", "evening",
                "Saturday", "morning",
                "Sunday", "morning"));
        m.put("David", prefs(
                "Monday", "morning",
                "Tuesday", "morning",
                "Thursday", "morning",
                "Friday", "morning",
                "Saturday", "afternoon"));
        m.put("Eve", prefs(
                "Wednesday", "afternoon",
                "Thursday", "afternoon",
                "Friday", "afternoon",
                "Saturday", "evening",
                "Sunday", "evening"));
        m.put("Frank", prefs(
                "Monday", "morning",
                "Wednesday", "morning",
                "Friday", "evening",
                "Saturday", "morning",
                "Sunday", "afternoon"));
        m.put("Grace", prefs(
                "Tuesday", "evening",
                "Wednesday", "evening",
                "Thursday", "morning",
                "Saturday", "evening",
                "Sunday", "evening"));
        m.put("Henry", prefs(
                "Monday", "evening",
                "Tuesday", "morning",
                "Thursday", "evening",
                "Friday", "morning",
                "Sunday", "morning"));
        m.put("Iris", prefs(
                "Monday", "morning",
                "Wednesday", "afternoon",
                "Thursday", "afternoon",
                "Friday", "evening",
                "Saturday", "afternoon"));
        m.put("Jack", prefs(
                "Tuesday", "afternoon",
                "Wednesday", "morning",
                "Thursday", "evening",
                "Saturday", "morning",
                "Sunday", "afternoon"));
        return m;
    }

    static Map<String, String> prefs(String... pairs) {
        Map<String, String> p = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            p.put(pairs[i], pairs[i + 1]);
        }
        return p;
    }
}
