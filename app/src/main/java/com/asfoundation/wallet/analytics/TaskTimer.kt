package com.asfoundation.wallet.analytics

import javax.inject.Inject

/**
 * Utility class to measure the duration of a task (measured to millis)
 */
class TaskTimer @Inject constructor() {

  private val taskHashMap = HashMap<String, Long>()

  /**
   * Starts measuring a specific task.
   */
  fun start(id: String) {
    taskHashMap[id] = System.currentTimeMillis()
  }

  /**
   * Ends measuring a specific task.
   *
   * @return duration of the task in milliseconds, or -1 if the task was not previously initialized
   */
  fun end(id: String): Long {
    val startTime = taskHashMap.remove(id) ?: return -1L
    return System.currentTimeMillis() - startTime
  }
}