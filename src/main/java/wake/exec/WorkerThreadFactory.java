package wake.exec;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

public class WorkerThreadFactory
    implements ForkJoinPool.ForkJoinWorkerThreadFactory {

    public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
        return new WorkerThread(pool);
    }

}
