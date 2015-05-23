package jake;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

public class WorkerThread extends ForkJoinWorkerThread {

    public WorkerThread(ForkJoinPool pool) {
        super(pool);
        System.out.println("custom");
    }

}
