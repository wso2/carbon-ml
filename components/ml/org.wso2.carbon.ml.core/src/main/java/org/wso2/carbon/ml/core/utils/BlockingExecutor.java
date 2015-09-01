/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.ml.core.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An executor which blocks and prevents further tasks from being submitted to the pool when the queue is full.
 * <p>
 * Based on the BoundedExecutor example in: Brian Goetz, 2006. Java Concurrency in Practice. (Listing 8.4)
 */
public class BlockingExecutor extends ThreadPoolExecutor {

    private static final Log log = LogFactory.getLog(BlockingExecutor.class);
    private final Semaphore semaphore;

    /**
     * Creates a BlockingExecutor which will block and prevent further submission to the pool when the specified queue
     * size has been reached.
     *
     * @param poolSize the number of the threads in the pool
     * @param queueSize the size of the queue
     */
    public BlockingExecutor(final int poolSize, final int queueSize) {
        super(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

        // the semaphore is bounding both the number of tasks currently executing
        // and those queued up
        semaphore = new Semaphore(poolSize + queueSize);
    }

    /**
     * Executes the given task. This method will block when the semaphore has no permits i.e. when the queue has reached
     * its capacity.
     */
    @Override
    public void execute(final Runnable task) {
        boolean acquired = false;
        do {
            try {
                semaphore.acquire();
                acquired = true;
            } catch (final InterruptedException e) {
                log.warn("InterruptedException while acquiring the semaphore", e);
            }
        } while (!acquired);

        try {
            super.execute(task);
        } catch (final RejectedExecutionException e) {
            semaphore.release();
            throw e;
        }
    }

    /**
     * Method invoked upon completion of execution of the given Runnable, by the thread that executed the task. Releases
     * a semaphore permit.
     */
    @Override
    public void afterExecute(final Runnable r, final Throwable t) {
        super.afterExecute(r, t);
        semaphore.release();
    }
}
