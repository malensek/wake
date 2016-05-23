/* wake - http://sigpipe.io/wake                       *
 * Copyright (c) 2016 Matthew Malensek                 *
 * Distributed under the MIT License (see LICENSE.txt) */

package wake.exec;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

public class WorkerThreadFactory
    implements ForkJoinPool.ForkJoinWorkerThreadFactory {

    public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
        return new WorkerThread(pool);
    }

}
