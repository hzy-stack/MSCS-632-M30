#include <iostream>
#include <string>
using namespace std;

int main() {
    int    x = 10;
    string y = "5";
    cout << "x = " << x << " (int)" << endl;
    cout << "y = " << y << " (string)" << endl;

    string z = to_string(x) + y;
    cout << "to_string(x) + y = " << z << endl;

    double q = 7 / 2;
    double r = 7 / 2.0;
    cout << "7 / 2   stored in double = " << q << endl;
    cout << "7 / 2.0 stored in double = " << r << endl;

    bool eq = (x == stoi(y) * 2);
    cout << "(x == stoi(y) * 2) = " << boolalpha << eq << endl;

    return 0;
}
