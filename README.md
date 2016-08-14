# Phoenix: A framework for highly-concurrent inter-process communication based on JVM off-heap cache.

Phoenix is a cache-based IPC Framework aiming to provide high-speed memory data interchange with the help of off-heap memory storage based on JVM ecosystem.

Unlike other high-speed memory interchange system like LMAX or distributed hash-table. Phoenix focuses on utilizing off-heap memory of JVM to reduce JVM Garbage collection issues to improve inter-process data exchange speed as it is well known that current multi-threaded/process programming in Java requires heap data exchange for thread to thread communication, which poses large overheads due to Java intrinsic garbage collection mechanics.

Basic Framework has been created with the following highlights:

1. Using Sun.misc.Unsafe class to directly control memory region with FileChannel to RamAccessFile class in Java, which is a map-to-memory mechanics for large data write and copy.

2. Designed a RingBuffer like data strcture to read and write data to allocated memory region in high-speed.

3. Concurrent multi-threaded RingBuffer Pool is enabled to provide multi-threaded read and write for user.

4. Basic write and read integration test has been conducted and already finished. 

Next Step:

1. Performance experiment comparing to other IPC Framework under certaind datasets.

2. Adding unit tests and regression tests.

3. Making it a distributed one.




