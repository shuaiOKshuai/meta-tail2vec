package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/* Example Use

	//Collection of items to process in parallel
	Collection<Integer> elems = new LinkedList<Integer>();
	for (int i = 0; i < 40; ++i) {
		elems.add(i);
	}
	
	Parallel.For(elems, new Parallel.Operation<Integer>() {
		public void perform(Integer param) {
			System.out.println(param);
	 	};
	});	

*/


public class Parallel {
    
	public static <T> void forEach(Collection<T> elements, int numCores, final Operation<T> operation) throws Exception {
    	ExecutorService forPool = Executors.newFixedThreadPool(numCores);
    	
    	try {
	    	List<Future<?>> futures = new ArrayList<Future<?>>();
	    	
	    	for (final T e : elements) {
	    		Future<?> f = forPool.submit(new Runnable() {
					@Override
					public void run() {
						try {
							operation.perform(e);
						} catch (Exception ex) {
							ex.printStackTrace(System.out);
						}					
					}
				});
	    		futures.add(f);
	    	}
	    	
	    	// blocks until all tasks finishes
	    	for (Future<?> f : futures)
	    		f.get();
    	}
    	finally {
    		forPool.shutdown();
    	}
    }
    
    public static <T> void forEach(Collection<T> elements, Operation<T> operation) throws Exception {
    	forEach(elements, Runtime.getRuntime().availableProcessors(), operation);
    }
    
    public static <T> void forEach(T[] elements, Operation<T> operation) throws Exception {
    	forEach(Arrays.asList(elements), operation);
    }
    
    public static <T> void forEach(T[] elements, int numCores, Operation<T> operation) throws Exception {
    	forEach(Arrays.asList(elements), numCores, operation);
    }
    
    public static void forLoop(int fromInclusive, int toExclusive, int numCores, Operation<Integer> operation) throws Exception {
    	Collection<Integer> elements = new ArrayList<Integer>();
    	for (int i = fromInclusive; i < toExclusive; i++) 
    		elements.add(i);
    	forEach(elements, numCores, operation);
    }
    
    public static void forLoop(int fromInclusive, int toExclusive, Operation<Integer> operation) throws Exception {
        forLoop(fromInclusive, toExclusive, Runtime.getRuntime().availableProcessors(), operation);
    }

   

    public static interface Operation<T> {
        public void perform(T param) throws Exception;
    }
    
}