package org.nilis.utils.execution_flow;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TasksQueryProcessor<TTask extends TaskWithListeners<TTaskResult>, TTaskResult> implements TasksProcessor<TTask> {
    final ExecutorService executor;
    final HashMap<TaskWrapper, Future<?>> tasks;
    
    private class TaskWrapper implements OnTaskExecutionListener<TTaskResult> {
    	TaskWithListeners<TTaskResult> task;
    	public TaskWrapper(TaskWithListeners<TTaskResult> task){
    		this.task = task;
    		task.addListener(this);
    	}
		
		@Override
		public void onTaskFailed(Exception e) {
			dequeTask(task);
		}
		
		private void dequeTask(TaskWithListeners<TTaskResult> task2) {
			tasks.remove(this);
		}
		
		@Override
		public void onTaskCompleted(TTaskResult result) {
			dequeTask(task);
		}
    }
    
    public TasksQueryProcessor() {
        executor = Executors.newFixedThreadPool(8);
        tasks = new HashMap<TaskWrapper, Future<?>>();
    }

	@Override
	public void addTask(final TTask task) {
		if(!tasks.containsKey(task)) {
			TaskWrapper taskWrapper = new TaskWrapper(task);
			Future<?> f = executor.submit(task);
			tasks.put(taskWrapper, f);
		}
	}

	@Override
	public void cancelTask(final TTask task) {
		if(task == null)
			return;
		
		Future<?> tasksFuture = tasks.get(task);
		if(tasksFuture != null && (!tasksFuture.isCancelled() && !tasksFuture.isDone())) {
			tasksFuture.cancel(true);
		}
	}

	@Override
	public void cancelAllTasks() {
		executor.shutdownNow();
	}
}