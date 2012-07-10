package org.nilis.utils.debug;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class ThreadMonitor {
	public static final String TAG = ThreadMonitor.class.getSimpleName();
	private static final int CRITICAL_COUNT = 3;
	
	protected static final long VMCHECK_DELAY = 2000;
	
	private static final String RUNLOOP_NAME = "ViberDebug:ThreadMonitor";
	
	final static int FILTERSPEC_NOFILTER = 0; // All suspected threads should be detected (noisy)
	final static int FILTERSPEC_EXCLUDE_NATIVES = 1; // All native calls should be suppressed (guess native calls are always working correctly)
	final static int FILTERSPEC_FILTER_ANDROID_NATIVE_CALLS = 2; // Android/Dalvik targeted methods should be suppressed
	final static int FILTERSPEC_FILTER_JAVA_NATIVE_CALLS = 4; // Java native calls should be suppressed (targeted as "java." packages)
	
	final static int FILTERSPEC_DEFAULT = FILTERSPEC_FILTER_ANDROID_NATIVE_CALLS | FILTERSPEC_FILTER_JAVA_NATIVE_CALLS;
			
	static final Map<Thread, StackTraceElement[]> stackSet; 
	static final Map<Thread, AtomicInteger> potentialDeadLockSet; // potential deadlock storage
	static final Set<Thread> trustedThreads;
	static final ThreadStateComparator comparator;
	static final HandlerThread runLoop;
	static final Handler handler;
	static final ThreadMonitor monitorInstance;
	static final Runnable scheduleTask;
	
	public static void init() {
		// @dummy static initialize routine
	}
	
	static {
		stackSet = Collections.synchronizedMap(new WeakHashMap<Thread, StackTraceElement[]>());
		potentialDeadLockSet = Collections.synchronizedMap(new WeakHashMap<Thread, AtomicInteger>());
		trustedThreads = Collections.synchronizedSet(new HashSet<Thread >());
		comparator = new ThreadStateComparator();
		scheduleTask = new Runnable() {
			@Override
			public void run() {
				if (null != monitorInstance) {
					monitorInstance.checkVMState();
				}
				handler.postDelayed(this, VMCHECK_DELAY);
			}
		};
		
		monitorInstance = new ThreadMonitor();
		(runLoop = new HandlerThread(RUNLOOP_NAME)).start();
		handler = new Handler(runLoop.getLooper());
		//scheduleTask.run();//TODO D.R. Not for commit
	}
	
	public static void addTrustedThread(Thread t) {
		trustedThreads.add(t);
	}
	
	public static void removeTrustedThread(Thread t) {
		trustedThreads.remove(t);
	}
	
	private interface IThreadStateComparator {
		boolean compare(Thread tid, StackTraceElement[] stc1, StackTraceElement[] stc2, int filterSpec);
	};
	
	static class ThreadStateComparator implements IThreadStateComparator {
		@Override
		public boolean compare(Thread tid, StackTraceElement[] stc1,
				StackTraceElement[] stc2, int filterSpec) {
			if (null == stc1 && null == stc2) return true;
			if (null == stc1 || null == stc2 || stc1.length != stc2.length) return false;
			if (trustedThreads.contains(tid)) return false;
			int index = 0;
			boolean hasSemaphoreCalls = false, hasMutexCalls = false;
			try {
				for(; index < stc1.length; index++) {
					hasSemaphoreCalls |= stc1[index].getClassName().contains(java.util.concurrent.Semaphore.class.getCanonicalName());
					hasMutexCalls |= stc1[index].getClassName().contains(java.util.concurrent.locks.ReentrantLock.class.getCanonicalName());				
					if ( !stc1[index].getClassName().equals(stc2[index].getClassName()) ||
							stc1[index].getLineNumber() != stc2[index].getLineNumber() ||
							! stc1[index].getMethodName().equals(stc2[index].getMethodName())
							)
						return false;
				}
			} catch (Exception e) {
				return false;
			}
			boolean bResult = true;
			// if snapshots are identical			
			if (FILTERSPEC_EXCLUDE_NATIVES == (filterSpec & FILTERSPEC_EXCLUDE_NATIVES)) {
				bResult &= !(stc1[0].isNativeMethod());
			} else {
				if (FILTERSPEC_FILTER_ANDROID_NATIVE_CALLS == (filterSpec & FILTERSPEC_FILTER_ANDROID_NATIVE_CALLS)) {
					bResult &= !((stc1[0].getClassName().startsWith("android.") || stc1[0].getClassName().startsWith("dalvik.")) && (stc1[0].isNativeMethod()));
				}
				if (FILTERSPEC_FILTER_JAVA_NATIVE_CALLS == (filterSpec & FILTERSPEC_FILTER_JAVA_NATIVE_CALLS)) {
					if (!hasSemaphoreCalls && !hasMutexCalls) // having danger calls from java
						bResult &= !(stc1[0].getClassName().startsWith("java.") && (stc1[0].isNativeMethod()));
				}
			}
			return bResult;
		}
	};
	
	private ThreadMonitor() {
	}
	
	private void checkVMState() {
		Map<Thread, StackTraceElement[]> newMap = getVMThreadsSnapshot();
		int potentials = compareSnapshots(newMap, stackSet, comparator);
		newMap.clear();
	}

	Map<Thread, StackTraceElement[]> getVMThreadsSnapshot() {
		ThreadGroup tg = Thread.currentThread().getThreadGroup();
		// collecting stack trace info
		int threadsCount = tg.activeCount();
		Thread[] threads = new Thread[threadsCount];
		Map<Thread, StackTraceElement[]> threadsInfo = new WeakHashMap<Thread, StackTraceElement[]>();
		Thread.enumerate(threads);
		for(Thread tid : threads) {
			if (null == tid) continue;
			synchronized(tid) {
			StackTraceElement[] arrStack = tid.getStackTrace();
			StackTraceElement[] arrCopyStack = new StackTraceElement[arrStack.length];
			synchronized(arrStack) {
				for (int index = 0; index < arrStack.length; index++) {
					arrCopyStack[index] = new StackTraceElement(arrStack[index].getClassName(), arrStack[index].getMethodName(), arrStack[index].getFileName(), arrStack[index].getLineNumber());				
				}
			}
			threadsInfo.put(tid, arrCopyStack);
			}
		}
		return threadsInfo;
	}
	
	void dumpThreadState(Thread tid, StackTraceElement[] ste, StackTraceElement[] ste2) {
		System.out.println("----------------"+tid+"---------------"+tid.getName()+"---------------");
//		int index = 0;
		for(StackTraceElement stel : ste) {
			System.out.println("::"+RUNLOOP_NAME 
				+ "\t" + stel.getClassName() + "." + stel.getMethodName() + ":" + stel.getLineNumber()
//				+ "/"
//				+ "\t" + ste2[index].getClassName() + "." + ste2[index].getMethodName() + ":" + ste2[index].getLineNumber()
				);
//			index++;
		}
	}
	
	int compareSnapshots(Map<Thread, StackTraceElement[]> currentSnapshot, Map<Thread, StackTraceElement[]> s_orig, IThreadStateComparator comparator) {
		int dlCount = 0;
		Map<Thread, StackTraceElement[]> previousSnapshot;
		synchronized(s_orig) {
			previousSnapshot = new WeakHashMap<Thread, StackTraceElement[]>(); 
			/* filtering previous snapshot */
			for(Map.Entry<Thread, StackTraceElement[]> recentSnapshotEntry : s_orig.entrySet()) {
				if (!currentSnapshot.containsKey(recentSnapshotEntry.getKey())) { // if previous snapshot doesn't contain the thread associated with current snapshot 					
					potentialDeadLockSet.remove(recentSnapshotEntry.getKey()); // remove also from potential deadlocked because it was freed
				} else {
					previousSnapshot.put(recentSnapshotEntry.getKey(), recentSnapshotEntry.getValue());
				}
			}
		}
		for(Map.Entry<Thread, StackTraceElement[]> currentSnapshotEntry : currentSnapshot.entrySet()) {
			if (currentSnapshotEntry.getKey() == Thread.currentThread()) continue; // exclude self from the list
			StackTraceElement[] previousSnapshotEntry = previousSnapshot.get(currentSnapshotEntry.getKey());
			// examining previous snapshot entry
			if (comparator.compare(currentSnapshotEntry.getKey(), currentSnapshotEntry.getValue(), previousSnapshotEntry, FILTERSPEC_DEFAULT)) {
				// potential deadlock - snapshots are identical
				AtomicInteger potentialDeadLockCount = potentialDeadLockSet.get(currentSnapshotEntry.getKey());
				if (null != potentialDeadLockCount) {
					if (potentialDeadLockCount.incrementAndGet() > CRITICAL_COUNT) {
						dumpThreadState(currentSnapshotEntry.getKey(), previousSnapshotEntry, currentSnapshotEntry.getValue());
						dlCount++;
					} // else skip till the next check
				} else {
					potentialDeadLockSet.put(currentSnapshotEntry.getKey(), new AtomicInteger(1));
				}
			} else { // snapshot entry recently marked as potential deadlocked is not found in currentSnapshotEntry or working 
				potentialDeadLockSet.remove(currentSnapshotEntry.getKey());
			}
			// update the history record with new stacktrace info
			previousSnapshot.put(currentSnapshotEntry.getKey(), currentSnapshotEntry.getValue());
		}
		synchronized(s_orig) {
			s_orig.clear();
			s_orig.putAll(previousSnapshot);
			previousSnapshot.clear();
			Runtime.getRuntime().gc();
		}
		return dlCount;
	}
}
