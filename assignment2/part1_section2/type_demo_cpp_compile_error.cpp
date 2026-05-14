#include <iostream>
#include <string>
using namespace std;

int main() {
    int    x = 10;
    string y = "5";
    string z = x + y;
    bool   eq = (x == y);
    cout << z << " " << eq;
    return 0;
}
