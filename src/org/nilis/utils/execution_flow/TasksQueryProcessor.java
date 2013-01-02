package org.nilis.utils.execution_flow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.nilis.utils.debug.D;

public class TasksQueryProcessor<TTask extends TaskWithListeners<TTaskResult>, TTaskResult> implements TasksProcessor<TTask> {
	protected final ExecutorService executor;
	protected HashSet<TaskWrapper> tasks;
    
    private class TaskWrapper implements OnTaskExecutionListener<TTaskResult> {
    	TaskWithListeners<TTaskResult> task;
    	public TaskWrapper(TaskWithListeners<TTaskResult> task){
    		this.task = task;
    		task.addListener(TaskWrapper.this);
    	}
		
		@Override
		public void onTaskFailed(Exception e) {
			//System.out.print("task failed "+toString()+"\n");
			dequeTask();
		}
		
		private void dequeTask() {
			tasks.remove(TaskWrapper.this);
			//printTasks();
		}
		
		@Override
		public void onTaskCompleted(TTaskResult result) {
			//System.out.print("task completed "+toString()+"\n");
			dequeTask();
		}
		
		@Override 
		public boolean equals( Object other ) {
			return task.equals(other);    
		}
	
		@Override 
		public int hashCode() {
			return task.hashCode();
		}
		
		@Override
		public String toString() {
			return "TaskWrapper:\n"+task.toString();
		}
    }
    
    public TasksQueryProcessor() {
        this(8);
    }
    
    public TasksQueryProcessor(int threadsCount) {
        executor = Executors.newFixedThreadPool(threadsCount);
        tasks = new HashSet<TaskWrapper>();
    }
    
    public boolean taskInQuery(final TTask task) {
    	for(TaskWrapper w : tasks) {
    		if(w.equals(task)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    protected TaskWrapper findTask(final TTask task) {
    	for(TaskWrapper w : tasks) {
    		if(w.equals(task)) {
    			return w;
    		}
    	}
    	return null;
    }

	@Override
	public synchronized void addTask(final TTask task) {
		if(!taskInQuery(task)) {
			TaskWrapper taskWrapper = new TaskWrapper(task);
			//Future<?> f = 
			tasks.add(taskWrapper);
			executor.submit(task);
		}
//		if(tasks.size() >= 10) {
//			int a = 0;
			//System.out.print(D.getStackTrace());
//		}
		//System.out.print("\ntasks query length: "+tasks.size()+"\n");
		//printTasks();
	}
	
	public void printTasks() {
		System.out.print("\nTasks in queue:\n");
		synchronized (tasks) {
			for(TaskWrapper task : tasks) {
				System.out.print(task.toString());
			}
			System.out.print("Tasks count: "+tasks.size()+"\n");
		}
	}

	@Override
	public void cancelTask(final TTask task) {
		if(task == null) {
			return;
		}
		
		TaskWrapper wrapper = findTask(task);
		if(wrapper == null) {
			return;
		}
		//Future<?> tasksFuture = tasks.get(wrapper);
		int s1 = tasks.size();
		tasks.remove(wrapper);
//		for(TaskWrapper w : tasks) {
//			tasks.remove(w);
//		}
		int s2 = tasks.size();
		System.out.print(s1+" vs "+s2+"\n");
//		if(tasksFuture != null && (!tasksFuture.isCancelled() && !tasksFuture.isDone())) {
//			tasksFuture.cancel(true);
//		}
		
	}

	@Override
	public void cancelAllTasks() {
		tasks.clear();
		executor.shutdownNow();
	}
}