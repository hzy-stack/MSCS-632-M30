"""Employee Shift Scheduler -- Python implementation."""

import random
from collections import defaultdict

DAYS = [
    "Monday", "Tuesday", "Wednesday", "Thursday",
    "Friday", "Saturday", "Sunday",
]
SHIFTS = ["morning", "afternoon", "evening"]

MIN_PER_SHIFT = 2
MAX_PER_SHIFT = 3
MAX_DAYS_PER_EMPLOYEE = 5

EMPLOYEES = {
    "Alice":   {"Monday": "morning",   "Tuesday": "morning",    "Wednesday": "morning",   "Thursday": "afternoon", "Friday": "morning"},
    "Bob":     {"Monday": "afternoon", "Tuesday": "afternoon",  "Wednesday": "evening",   "Thursday": "evening",   "Friday": "afternoon"},
    "Carol":   {"Monday": "evening",   "Tuesday": "evening",    "Wednesday": "evening",   "Saturday": "morning",   "Sunday": "morning"},
    "David":   {"Monday": "morning",   "Tuesday": "morning",    "Thursday": "morning",    "Friday": "morning",     "Saturday": "afternoon"},
    "Eve":     {"Wednesday": "afternoon", "Thursday": "afternoon", "Friday": "afternoon", "Saturday": "evening",   "Sunday": "evening"},
    "Frank":   {"Monday": "morning",   "Wednesday": "morning",  "Friday": "evening",      "Saturday": "morning",   "Sunday": "afternoon"},
    "Grace":   {"Tuesday": "evening",  "Wednesday": "evening",  "Thursday": "morning",    "Saturday": "evening",   "Sunday": "evening"},
    "Henry":   {"Monday": "evening",   "Tuesday": "morning",    "Thursday": "evening",    "Friday": "morning",     "Sunday": "morning"},
    "Iris":    {"Monday": "morning",   "Wednesday": "afternoon", "Thursday": "afternoon", "Friday": "evening",     "Saturday": "afternoon"},
    "Jack":    {"Tuesday": "afternoon", "Wednesday": "morning", "Thursday": "evening",    "Saturday": "morning",   "Sunday": "afternoon"},
}


def initialize_schedule():
    return {day: {shift: [] for shift in SHIFTS} for day in DAYS}


def is_shift_full(schedule, day, shift):
    return len(schedule[day][shift]) >= MAX_PER_SHIFT


def employee_worked_that_day(schedule, employee, day):
    for shift in SHIFTS:
        if employee in schedule[day][shift]:
            return True
    return False


def try_assign(schedule, days_worked, employee, day, shift):
    if days_worked[employee] >= MAX_DAYS_PER_EMPLOYEE:
        return False
    if employee_worked_that_day(schedule, employee, day):
        return False
    if is_shift_full(schedule, day, shift):
        return False
    schedule[day][shift].append(employee)
    days_worked[employee] += 1
    return True


def assign_with_conflict_resolution(schedule, days_worked, employee, day, preferred_shift):
    if try_assign(schedule, days_worked, employee, day, preferred_shift):
        return preferred_shift, day, "preferred"

    for alt in SHIFTS:
        if alt == preferred_shift:
            continue
        if try_assign(schedule, days_worked, employee, day, alt):
            return alt, day, "same-day-alternate"

    next_day = DAYS[(DAYS.index(day) + 1) % len(DAYS)]
    if try_assign(schedule, days_worked, employee, next_day, preferred_shift):
        return preferred_shift, next_day, "next-day"

    return None, None, "unassigned"


def fill_minimum_staffing(schedule, days_worked):
    employees = list(EMPLOYEES.keys())
    for day in DAYS:
        for shift in SHIFTS:
            while len(schedule[day][shift]) < MIN_PER_SHIFT:
                candidates = [
                    e for e in employees
                    if days_worked[e] < MAX_DAYS_PER_EMPLOYEE
                    and not employee_worked_that_day(schedule, e, day)
                ]
                if not candidates:
                    print(f"  WARNING: cannot meet minimum on {day} {shift}")
                    break
                chosen = random.choice(candidates)
                schedule[day][shift].append(chosen)
                days_worked[chosen] += 1


def build_schedule():
    schedule = initialize_schedule()
    days_worked = defaultdict(int)
    notes = []

    # Pass 1: honor preferences
    for employee, prefs in EMPLOYEES.items():
        for day, preferred_shift in prefs.items():
            assigned_shift, assigned_day, status = assign_with_conflict_resolution(
                schedule, days_worked, employee, day, preferred_shift)
            if status == "unassigned":
                notes.append(
                    f"{employee}: could not honor {day} {preferred_shift} "
                    f"(no available shifts).")
            elif status != "preferred":
                notes.append(
                    f"{employee}: conflict on {day} {preferred_shift} -> "
                    f"reassigned to {assigned_day} {assigned_shift}.")

    # Pass 2: enforce minimum staffing
    fill_minimum_staffing(schedule, days_worked)
    return schedule, days_worked, notes


def print_schedule(schedule, days_worked, notes):
    bar = "=" * 80
    print(bar)
    print(" Weekly Employee Schedule ".center(80, "="))
    print(bar)

    col_width = 22
    header = "Day".ljust(10) + "".join(s.capitalize().ljust(col_width) for s in SHIFTS)
    print(header)
    print("-" * 80)
    for day in DAYS:
        cells = []
        for shift in SHIFTS:
            people = schedule[day][shift]
            cells.append(", ".join(people) if people else "(empty)")
        print(day.ljust(10) + "".join(c.ljust(col_width) for c in cells))

    print()
    print("Days worked per employee:")
    for emp in sorted(days_worked.keys()):
        print(f"  {emp:<8} {days_worked[emp]}")

    if notes:
        print()
        print("Conflict resolution notes:")
        for note in notes:
            print(f"  - {note}")


def main():
    random.seed(42)
    schedule, days_worked, notes = build_schedule()
    print_schedule(schedule, days_worked, notes)


if __name__ == "__main__":
    main()
