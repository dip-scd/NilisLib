package org.nilis.utils.execution_flow;

public class TasksExecutor<TTaskResult> extends TasksQueryProcessor<TaskWithListeners<TTaskResult>, TTaskResult>{
	public TasksExecutor() {
		super(1);
	}
	
	public String toString() {
		return "TasksExecutor, task count: "+tasksCount()+"\n";
	}
}
