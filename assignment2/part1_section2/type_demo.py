x = 10
y = "5"
print("x =", x, "type:", type(x).__name__)
print("y =", y, "type:", type(y).__name__)

try:
    z = x + y
    print("x + y =", z)
except TypeError as e:
    print("TypeError when adding int + str:", e)

print("Explicit cast int(y):", x + int(y))
print("Explicit cast str(x):", str(x) + y)

print("10 == '10':", 10 == "10")
print("10 == 10.0:", 10 == 10.0)

print("7 / 2  =", 7 / 2)
print("7 // 2 =", 7 // 2)
