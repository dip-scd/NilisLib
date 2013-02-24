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
    	synchronized (tasks) {
	    	for(TaskWrapper w : tasks) {
	    		if(w.equals(task)) {
	    			return true;
	    		}
	    	}
	    	return false;
    	}
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
	public void addTask(final TTask task) {
		synchronized(tasks) {
			if(!taskInQuery(task)) {
				TaskWrapper taskWrapper = new TaskWrapper(task);
				tasks.add(taskWrapper);
				executor.submit(task);
			} else {
				//D.i("task already in query");
			}
		}
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
		int s1 = tasks.size();
		tasks.remove(wrapper);
	}

	@Override
	public void cancelAllTasks() {
		clearTasks();
		executor.shutdownNow();
	}

	public void clearTasks() {
		tasks.clear();
	}
	
	public int tasksCount() {
		return tasks.size();
	}
}