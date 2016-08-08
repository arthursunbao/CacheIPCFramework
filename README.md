# CacheIPCFramework

A Cache-based IPC Framework is aimed to provide high-speed memory data interchange with the help of off-heap memory storage.

I created this framework to test the hypothesis of utilizing off-heap memory storage for data exchange between Java threads to see the data exchange performance.

Current multi-threaded programming in Java requires heap data exchange for thread to thread communication, which poses large overheads due to Java intrinsic garbage collection mechanics. 

So what I proposed is to create an off-heap area with a well-designed data strcture to store the interexchangable data between threads, mainly like the structure layout in modern memory design. 

Expected speed performance gain should be at 100x faster than normal on-heap storage. 


