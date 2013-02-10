package org.nilis.view.swing;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

import javax.swing.JPanel;

import org.nilis.utils.debug.D;
import org.nilis.utils.execution_flow.TaskWithListeners;
import org.nilis.utils.execution_flow.TasksExecutor;
import org.nilis.utils.execution_flow.TasksProcessor.OnTaskExecutionListener;

public abstract class AdaptedView<TDataToDisplay, TInputData> extends JPanel implements OnTaskExecutionListener<TDataToDisplay>{
	
	private static final long serialVersionUID = 6263454610626799372L;
	
	protected abstract TDataToDisplay convert(TInputData input);
	
	public class ViewAdapter {
		protected TasksExecutor<TDataToDisplay> executor = new TasksExecutor<TDataToDisplay>();
		
		public ViewAdapter() {
		}
		
		public class GetTask extends TaskWithListeners<TDataToDisplay> {
			protected TInputData input;
			public GetTask(OnTaskExecutionListener<TDataToDisplay> listener, TInputData input) {
				super(listener);
				this.input = input;
			}

			@Override
			public void run() {
				D.d("GetTask run "+toString());
				notifyListenersAboutComplete(AdaptedView.this.convert(input));
			}
			
			public String toString() {
				String ret = "GetTask: ";
				ret+=input.toString();
				ret+="\n";
				return ret;
			}
		}
		
		public void get(TInputData input) {
			if(executor.tasksCount() > 3) {
				D.w("executor already has tasks");
				return;
				//executor.clearTasks();
			}
			executor.addTask(new GetTask(AdaptedView.this, input));
			//executor.printTasks();
		}
	};
	
	protected TDataToDisplay cache = null;
	
	@Override
	public void onTaskCompleted(TDataToDisplay result) {
		updateCache(result);
		repaint();
	}

	protected void updateCache(TDataToDisplay result) {
		cache = result;
	}
	
	protected boolean toUpdateData = true;
	public void refresh() {
		toUpdateData = true;
		repaint();
	}

	@Override
	public void onTaskFailed(Exception e) {
	}
	
	protected int BOTTOM_PADDING = 24;
	protected int LEFT_PADDING = 10;
	protected int RIGHT_PADDING = 70;
	protected int TOP_PADDING = 10;
	
	protected Color BACKGROUND_COLOR = new Color(47, 47,47);
	protected Color BACKGROUND_COLOR2 = BACKGROUND_COLOR;
	
	protected Boolean isDrawing = false;
	
	protected Paint defautPaint;
	protected Graphics2D g;
	protected ViewAdapter adapter;
	
	public AdaptedView() {
		adapter = new ViewAdapter();
	}
	
	public boolean isDrawing() {
		return isDrawing.booleanValue();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		if(isDrawing()) {
			return;
		}
		
		synchronized (isDrawing) {
			isDrawing = true;
		}
		this.g = (Graphics2D) g;
		defautPaint = this.g.getPaint();
		
		TInputData adapterInput = prepareInputForAdapter();
		if(adapter != null && toUpdateData) {
			adapter.get(adapterInput);
			toUpdateData = false;
		}
		calculateDrawRelatedValues();
		super.paintComponent(g);
		drawBackground();
		drawGrid();
		if(cache != null) {
			drawValues(cache);
		}
		
		synchronized (isDrawing) {
			isDrawing = false;
		}
	}
	
	protected abstract TInputData prepareInputForAdapter();
	protected abstract void calculateDrawRelatedValues();
	protected abstract void drawGrid();
	protected abstract void drawValues(TDataToDisplay data);
	
	protected void drawBackground() {
		GradientPaint gp = new GradientPaint(0, 0, BACKGROUND_COLOR2, 0, getHeight(), BACKGROUND_COLOR);
		this.g.setPaint(gp);
		this.g.fillRect(0, 0, getWidth(), getHeight());
		this.g.setPaint(defautPaint);
	}
	
	protected int workingAreaHeight()  {
		return (int)(getHeight() - TOP_PADDING - BOTTOM_PADDING);
	}
	
	protected int workingAreaWidth()  {
		return (int)(getWidth() - LEFT_PADDING - RIGHT_PADDING);
	}
	
	protected int yRelatedToWorkingArea(double partialHeight) {//0..1
		return (int) Math.round(TOP_PADDING + (getHeight() - TOP_PADDING - BOTTOM_PADDING)*partialHeight);
	}
	
	protected int xRelatedToWorkingArea(double partialWidth) {//0..1
		return (int) Math.round(LEFT_PADDING + (getWidth() - LEFT_PADDING - RIGHT_PADDING)*partialWidth);
	}
}
