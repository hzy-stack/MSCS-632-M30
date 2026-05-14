#include <iostream>
#include <memory>
#include <numeric>

constexpr std::size_t N = 1'000'000;

long long sum_through_pointer(long long* data, std::size_t n) {
    long long total = 0;
    for (std::size_t i = 0; i < n; ++i) total += data[i];
    return total;
}

int main() {
    long long* numbers = new long long[N];
    for (std::size_t i = 0; i < N; ++i) numbers[i] = static_cast<long long>(i + 1);

    long long sum1 = sum_through_pointer(numbers, N);
    std::cout << "Manual new[]/delete[] sum = " << sum1 << "\n";

    delete[] numbers;
    numbers = nullptr;

    auto smart = std::make_unique<long long[]>(N);
    for (std::size_t i = 0; i < N; ++i) smart[i] = static_cast<long long>(i + 1);

    long long sum2 = std::accumulate(smart.get(), smart.get() + N, 0LL);
    std::cout << "unique_ptr<long long[]> sum = " << sum2 << "\n";

    // Deliberate leak below.
    long long* leaked = new long long[1024]{};
    leaked[0] = 42;
    std::cout << "Leaked block first element = " << leaked[0] << "\n";

    return 0;
}
