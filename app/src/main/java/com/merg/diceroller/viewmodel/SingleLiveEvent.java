package com.merg.diceroller.viewmodel;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SingleLiveEvent<T> extends MutableLiveData<T> {
    private final AtomicBoolean pending = new AtomicBoolean(false);
    @Override public void setValue(@Nullable T value) {
        pending.set(true);
        super.setValue(value);
    }
    public void observe(androidx.lifecycle.LifecycleOwner owner, androidx.lifecycle.Observer<? super T> observer) {
        super.observe(owner, value -> {
            if (pending.compareAndSet(true, false)) observer.onChanged(value);
        });
    }
}
