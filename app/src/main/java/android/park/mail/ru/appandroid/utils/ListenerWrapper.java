package android.park.mail.ru.appandroid.utils;


public class ListenerWrapper<T> {

	private T listener;

	public ListenerWrapper(T listener) {
		this.listener = listener;
	}

	public T getListener() {
		return listener;
	}

	public void unregister() {
		listener = null;
	}
}
