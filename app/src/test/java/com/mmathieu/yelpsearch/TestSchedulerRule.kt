package com.mmathieu.yelpsearch

import io.reactivex.Scheduler
import io.reactivex.internal.schedulers.ExecutorScheduler
import io.reactivex.plugins.RxJavaPlugins
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.concurrent.Executor


class TestSchedulerRule : TestRule {

    private val testScheduler = object : Scheduler() {
        override fun createWorker(): Worker {
            return ExecutorScheduler.ExecutorWorker(Executor { it.run() }, false)
        }
    }

    override fun apply(base: Statement, d: Description?): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                RxJavaPlugins.setIoSchedulerHandler { testScheduler }
                RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
                RxJavaPlugins.setNewThreadSchedulerHandler { testScheduler }

                try {
                    base.evaluate()
                } finally {
                    RxJavaPlugins.reset()
                }
            }
        }
    }
}