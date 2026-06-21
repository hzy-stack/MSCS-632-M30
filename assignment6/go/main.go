package main

import (
	"bufio"
	"fmt"
	"log"
	"os"
	"sort"
	"sync"
	"time"
)

const (
	workerCount = 4
	taskCount   = 20
	outputFile  = "results.txt"
)

type Task struct {
	ID      int
	Payload int
}

type Result struct {
	TaskID int
	Value  int64
	Worker string
}

func process(name string, t Task) (Result, error) {
	time.Sleep(50 * time.Millisecond)
	if t.Payload < 0 {
		return Result{}, fmt.Errorf("task %d: invalid payload %d", t.ID, t.Payload)
	}
	value := int64(t.Payload) * int64(t.Payload)
	return Result{TaskID: t.ID, Value: value, Worker: name}, nil
}

func worker(name string, tasks <-chan Task, results chan<- Result, wg *sync.WaitGroup) {
	defer wg.Done()
	log.Printf("%s started", name)

	for t := range tasks {
		res, err := process(name, t)
		if err != nil {
			log.Printf("%s error: %v", name, err)
			continue
		}
		results <- res
	}

	log.Printf("%s completed", name)
}

func writeResults(path string, results []Result) (err error) {
	f, err := os.Create(path)
	if err != nil {
		return fmt.Errorf("create %s: %w", path, err)
	}
	defer func() {
		if cerr := f.Close(); cerr != nil && err == nil {
			err = fmt.Errorf("close %s: %w", path, cerr)
		}
	}()

	sort.Slice(results, func(i, j int) bool {
		return results[i].TaskID < results[j].TaskID
	})

	w := bufio.NewWriter(f)
	for _, r := range results {
		if _, werr := fmt.Fprintf(w, "task=%d value=%d worker=%s\n", r.TaskID, r.Value, r.Worker); werr != nil {
			return fmt.Errorf("write %s: %w", path, werr)
		}
	}
	if ferr := w.Flush(); ferr != nil {
		return fmt.Errorf("flush %s: %w", path, ferr)
	}
	return nil
}

func main() {
	log.SetFlags(log.Ltime | log.Lmicroseconds)

	tasks := make(chan Task, taskCount)
	results := make(chan Result, taskCount)

	for i := 1; i <= taskCount; i++ {
		tasks <- Task{ID: i, Payload: i}
	}
	close(tasks)
	log.Printf("enqueued %d tasks", taskCount)

	var wg sync.WaitGroup
	for i := 1; i <= workerCount; i++ {
		wg.Add(1)
		go worker(fmt.Sprintf("worker-%d", i), tasks, results, &wg)
	}

	go func() {
		wg.Wait()
		close(results)
	}()

	collected := make([]Result, 0, taskCount)
	for r := range results {
		collected = append(collected, r)
	}

	if err := writeResults(outputFile, collected); err != nil {
		log.Fatalf("could not write results: %v", err)
	}

	if len(collected) != taskCount {
		log.Printf("warning: expected %d results, got %d", taskCount, len(collected))
	}

	log.Printf("all workers done. processed %d tasks, results written to %s", len(collected), outputFile)
}
