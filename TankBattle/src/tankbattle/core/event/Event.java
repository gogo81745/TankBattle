package tankbattle.core.event;

import tankbattle.core.others.Extra;
import tankbattle.core.others.Extrable;

/**
 * 事件是事件驱动的核心<br>
 * 游戏中每发生一个动作都会有对应的事件产生<br>
 * 事件可以被取消，完成后可以设置为已完成<br>
 * 事件中有个{@link #code}属性，用于对该事件进行的操作的判断<br>
 * 事件是可扩展的，你可以在事件中添加一些属性<br>
 *
 * @author Gogo
 */
public class Event implements Extrable {

	private Extra extra = new Extra();

	protected boolean canceled;
	protected boolean executed;

	protected int code;

	public Event() {
		super();
	}

	public Event(int code) {
		super();
		this.code = code;
	}

	public boolean canceled() {
		return canceled;
	}

	public Event setcanceled(boolean cancel) {
		this.canceled = cancel;
		return this;
	}

	public boolean executed() {
		return executed;
	}

	public Event setExecuted(boolean executed) {
		this.executed = executed;
		return this;
	}

	public int code() {
		return code;
	}

	public Event setCode(int code) {
		this.code = code;
		return this;
	}

	@Override
	public Extrable extra() {
		return extra;
	}

}
