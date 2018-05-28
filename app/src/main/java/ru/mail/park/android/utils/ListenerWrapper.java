package ru.mail.park.android.utils;


import android.support.annotation.Nullable;

public class ListenerWrapper<T> {

	private T listener;

	public ListenerWrapper(@Nullable T listener) {
		this.listener = listener;
	}

	public T getListener() {
		return listener;
	}

	public void unregister() {
		listener = null;
	}
}
