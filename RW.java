// COMMANDS
// cd filepath to src
// javac RW.java
// java RW int int int


import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
public class RW {
    private static int maxNumber = 5;
    private static int readCount = 0;
    private static Random randomNum = new Random();
    private static int buffer = 0; // Starting number for the Readers amd Writers.
    private static final Semaphore mutex = new Semaphore(1);
    private static final Semaphore writerSemaphore = new Semaphore(1);
    private static final Semaphore readerSemaphore = new Semaphore(1);

    static class Reader extends Thread {
        private final char name;
        private final int accesses;

        Reader(char name, int accesses) {
            this.name = name;
            this.accesses = accesses;
        }

        @Override
        public void run() {
            for (int i = 0; i < accesses; i++) {
                try {
                    Thread.sleep(randomNum.nextInt(1000)); // Simulate random delay
                    readerSemaphore.acquire(); // Prevent reader starvation
                    mutex.acquire();
                    readCount++;
                    if (readCount == 1) {
                        writerSemaphore.acquire(); // First reader blocks writers
                    }
                    mutex.release();
                    readerSemaphore.release();

                    // Reading the buffer
                    System.out.println("Reader " + name + " retrieved " + buffer);

                    mutex.acquire();
                    readCount--;
                    if (readCount == 0) {
                        writerSemaphore.release(); // Last reader unblocks writers
                    }
                    mutex.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class Writer extends Thread {
        private final char name;
        private final int accesses;

        Writer(char name, int accesses) {
            this.name = name;
            this.accesses = accesses;
        }

        @Override
        public void run() {
            for (int i = 0; i < accesses; i++) {
                try {
                    Thread.sleep(randomNum.nextInt(1000)); // Simulate random delay
                    writerSemaphore.acquire(); // Writer priority
                    buffer = ThreadLocalRandom.current().nextInt(1, 10); // Write to buffer
                    System.out.println("Writer " + name + " set buffer to " + buffer);
                    writerSemaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        // Check if the correct number of arguments is passed
        if (args.length != 3) {
            System.out.println("Usage: java RW <# of readers> <# of writers> <# of accesses>");
            return;
        }

        try {
            // Parse the arguments
            int numReaders = Integer.parseInt(args[0]);
            int numWriters = Integer.parseInt(args[1]);
            int numAccesses = Integer.parseInt(args[2]);

            // Validate inputs
            if (numReaders < 0 || numReaders > maxNumber || numWriters < 0 || numWriters > maxNumber || numAccesses <= 0) {
                System.out.println("Invalid input. Ensure 0 <= # of readers/writers <= 5 and # of accesses > 0.");
                return;
            }

            System.out.println("# of readers is: " + numReaders);
            System.out.println("# of writers is: " + numWriters);
            System.out.println("Reader/Writer is starting...");

            // Create and start reader threads
            Thread[] readers = new Thread[numReaders];
            for (int i = 0; i < numReaders; i++) {
                readers[i] = new Reader((char) ('A' + i), numAccesses);
            }

            // Create and start writer threads
            Thread[] writers = new Thread[numWriters];
            for (int i = 0; i < numWriters; i++) {
                writers[i] = new Writer((char) ('F' + i), numAccesses);
            }

            // Start all threads
            for (Thread reader : readers) {
                reader.start();
            }
            for (Thread writer : writers) {
                writer.start();
            }

            // Wait for all threads to finish
            for (Thread reader : readers) {
                reader.join();
            }
            for (Thread writer : writers) {
                writer.join();
            }

            System.out.println("All threads have finished execution.");

        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please provide integer values for arguments.");
        } catch (InterruptedException e) {
            System.err.println("Thread interrupted: " + e.getMessage());
        }
    }
}

