fn sum_by_borrow(data: &Vec<i64>) -> i64 {
    data.iter().sum()
}

fn sum_by_move(data: Vec<i64>) -> i64 {
    data.into_iter().sum()
}

fn main() {
    let numbers: Vec<i64> = (1..=1_000_000).collect();
    println!("Allocated {} i64 values on the heap (~{} MB)",
             numbers.len(),
             numbers.len() * std::mem::size_of::<i64>() / 1_048_576);

    let s1 = sum_by_borrow(&numbers);
    let s2 = sum_by_borrow(&numbers);
    println!("Borrow sums (same vector used twice): {} {}", s1, s2);

    let s3 = sum_by_move(numbers);
    println!("Move sum: {}", s3);

    println!("End of main -- any remaining owned values are dropped here.");
}
