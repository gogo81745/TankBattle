package tankbattle.core.time;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import tankbattle.core.event.Listener;
import tankbattle.core.event.ListenerItem;

public class TimerGroup {

	private Map<String, TimerGroup> timers = Collections.synchronizedMap(new HashMap<>());

	private Set<ListenerItem<TimeEvent>> listeners = Collections
			.synchronizedSet(new HashSet<ListenerItem<TimeEvent>>());

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

	private class TimeThread extends Thread {

		ReentrantLock lock = new ReentrantLock();

		Condition con = lock.newCondition();

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
						con.await();
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
		class ExecuteThread extends Thread {
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
			thread.con.signal();
			thread.lock.unlock();
		}
		return this;
	}

	public TimerGroup stop() {
		running = false;
		return this;
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

	public List<ListenerItem<TimeEvent>> listeners() {
		return listeners.stream().sorted((a, b) -> {
			int x = a.getPriority() - b.getPriority();
			return x == 0 ? a.getName().compareTo(b.getName()) : x;
		}).collect(Collectors.toList());
	}

	public String addListener(Listener<TimeEvent> listener) {
		return addListener(listener.getClass().getName(), Listener.NORMAL, listener);
	}

	public String addListener(int priority, Listener<TimeEvent> listener) {
		return addListener(listener.getClass().getName(), priority, listener);
	}

	public String addListener(String name, Listener<TimeEvent> listener) {
		return addListener(name, Listener.NORMAL, listener);
	}

	public String addListener(String name, int priority, Listener<TimeEvent> listener) {
		if (name == null || listener == null) {
			throw new NullPointerException("addListener传入null");
		}
		listeners.add(new ListenerItem<>(name, priority, TimeEvent.class, listener));
		return name;
	}

	public ListenerItem<TimeEvent> getListener(String name) {
		if (name == null) {
			return null;
		}
		Optional<ListenerItem<TimeEvent>> o = listeners.stream().filter(e -> e.getName().equals(name)).findFirst();
		if (o.isPresent()) {
			return o.get();
		}
		return null;
	}

	public ListenerItem<TimeEvent> getListener(Listener<TimeEvent> listener) {
		if (listener == null) {
			return null;
		}
		Optional<ListenerItem<TimeEvent>> o = listeners.stream().filter(e -> e.getListener().equals(listener))
				.findFirst();

		if (o.isPresent()) {
			return o.get();
		}
		return null;
	}

	public ListenerItem<TimeEvent> removeListener(String name) {
		ListenerItem<TimeEvent> l = getListener(name);
		listeners.remove(l);
		return l;
	}

	public ListenerItem<TimeEvent> removeListener(Listener<TimeEvent> listener) {
		ListenerItem<TimeEvent> l = getListener(listener);
		listeners.remove(l);
		return l;
	}
}