package org.nilis.utils.execution_flow;

public interface TasksProcessor<TTask> {
	public static interface OnTaskExecutionListener<TTaskResult> {
		void onTaskCompleted(TTaskResult result);

		void onTaskFailed(Exception e);
	}

	void addTask(TTask task);

	void cancelTask(TTask task);

	void cancelAllTasks();
}
