package tankbattle.core.time;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import tankbattle.core.event.EventProcess;
import tankbattle.core.event.Listener;
import tankbattle.core.event.ListenerItem;

/**
 * 计时器组<br>
 * 可以向本类加入时间监听器<br>
 * 
 * @author Gogo
 *
 */
public class TimerGroup {

	private Map<String, TimerGroup> timers = Collections.synchronizedMap(new HashMap<>());

	private EventProcess process = new EventProcess();
	private boolean running = false;

	private int total = 0;
	private int next = 0;

	private int interval;

	private double change = 1;

	private TimeThread thread;

	public TimerGroup(int interval) {
		super();
		this.interval = interval;
	}

	public TimerGroup(int interval, boolean start) {
		this.interval = interval;
		if (start) {
			start();
		}
	}

	/**
	 * 该计时器经过一段时间<br/>
	 * 如果达到计时器的计时,则会向所有该计时器上的监听器发送 TimeEvent 事件<br/>
	 * 
	 * @param time
	 */
	public void pass(int time) {
		if (!running) {
			return;
		}
		time *= change;
		total += time;
		/**
		 * 达到计时器的值后向监听器发送事件
		 */
		if (total >= next) {
			next += interval;
			send(new TimeEvent(interval, total));
		}
		/**
		 * 子计时器经过时间
		 */
		for (String key : timers.keySet()) {
			TimerGroup t = timers.get(key);
			if (t.thread == null) {
				t.pass(time);
			}
		}
	}

	/**
	 * 为本计时器单独创建线程</br>
	 * 该操作会时本计时器脱离父计时器对时间的调整</br>
	 * 
	 * @return
	 */
	public TimerGroup createThread() {
		if (thread == null) {
			synchronized (this) {
				thread = new TimeThread();
			}
		}
		return this;
	}

	private class TimeThread extends GameThread {

		ReentrantLock lock = new ReentrantLock();

		/**
		 * 用于判断该Timer是否在运行的Condition,如果该 Timer的 Runing = fasle,这个条件会持续 await()
		 */
		Condition runCond = lock.newCondition();

		ExecuteThread exe = new ExecuteThread();

		TimeThread() {
			start();
			exe.start();
		}

		public void run() {
			long start;
			while (true) {
				while (!running()) {
					lock.lock();
					try {
						runCond.await();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					lock.unlock();
				}
				start = System.currentTimeMillis();
				try {
					sleep(interval);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				exe.rinterval = (int) (System.currentTimeMillis() - start);
				exe.Do();
			}
		}

		/**
		 * 用于发送 TimeEvent 事件，并让子计时器pass()的线程<br/>
		 * 每调用一次 Do() 方法会使子计时器 pass()一次<br/>
		 * 
		 * @author Gogo
		 *
		 */
		class ExecuteThread extends GameThread {
			ReentrantLock elock = new ReentrantLock();
			Condition econ = elock.newCondition();

			int rinterval;

			@Override
			public void run() {
				while (true) {
					elock.lock();
					try {
						econ.await();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					elock.unlock();
					pass(rinterval);
				}
			}

			public void Do() {
				elock.lock();
				econ.signal();
				elock.unlock();
			}

		}
	}

	public TimerGroup start() {
		running = true;
		if (thread != null) {
			thread.lock.lock();
			thread.runCond.signal();
			thread.lock.unlock();
		}
		for (String key : timers.keySet()) {
			TimerGroup sub = timers.get(key);
			if (sub != null) {
				sub.start();
			}
		}
		return this;
	}

	public TimerGroup stop() {
		running = false;
		for (String key : timers.keySet()) {
			TimerGroup sub = timers.get(key);
			if (sub != null) {
				sub.stop();
			}
		}
		return this;
	}

	public TimerGroup getTimer(String key) {
		return timers.get(key);
	}

	public TimerGroup putTimer(String key, TimerGroup timer) {
		return timers.put(key, timer);
	}

	public TimerGroup removeTimer(String key) {
		return timers.remove(key);
	}

	public TimeEvent send(TimeEvent e) {
		listeners().forEach(i -> {
			Listener<TimeEvent> l = i.getListener();
			l.listen(e);
		});
		return e;
	}

	public boolean running() {
		return running;
	}

	public int getTotal() {
		return total;
	}

	public TimerGroup setTotal(int total) {
		this.total = total;
		return this;
	}

	public int getInterval() {
		return interval;
	}

	public TimerGroup setInterval(int interval) {
		this.interval = interval;
		return this;
	}

	public double getChange() {
		return change;
	}

	public TimerGroup setChange(double change) {
		this.change = change;
		return this;
	}

	@SuppressWarnings("unchecked")
	public List<ListenerItem<TimeEvent>> listeners() {
		List<ListenerItem<TimeEvent>> l2 = new ArrayList<>();
		process.listeners().forEach(e -> {
			l2.add((ListenerItem<TimeEvent>) e);
		});
		return l2;
	}

	public String addListener(Listener<TimeEvent> listener) {
		return process.addListener(TimeEvent.class, listener);
	}

	public String addListener(int priority, Listener<TimeEvent> listener) {
		return process.addListener(priority, TimeEvent.class, listener);
	}

	public String addListener(String name, Listener<TimeEvent> listener) {
		return process.addListener(name, TimeEvent.class, listener);
	}

	public String addListener(String name, int priority, Listener<TimeEvent> listener) {
		return process.addListener(name, priority, TimeEvent.class, listener);
	}

	@SuppressWarnings("unchecked")
	public ListenerItem<TimeEvent> getListener(String name) {
		return (ListenerItem<TimeEvent>) process.getListener(name);
	}

	@SuppressWarnings("unchecked")
	public ListenerItem<TimeEvent> getListener(Listener<TimeEvent> listener) {
		return (ListenerItem<TimeEvent>) process.getListener(listener);
	}

	@SuppressWarnings("unchecked")
	public ListenerItem<TimeEvent> removeListener(String name) {
		return (ListenerItem<TimeEvent>) process.removeListener(name);
	}

	@SuppressWarnings("unchecked")
	public ListenerItem<TimeEvent> removeListener(Listener<TimeEvent> listener) {
		return (ListenerItem<TimeEvent>) process.removeListener(listener);
	}
}
