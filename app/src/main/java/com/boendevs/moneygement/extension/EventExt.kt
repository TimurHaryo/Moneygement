package com.boendevs.moneygement.extension

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

typealias MutableLiveEvent<T> = MutableLiveData<Event<T>>

typealias LiveEvent<T> = LiveData<Event<T>>

inline fun <T> AppCompatActivity.observeLiveEvent(source: LiveEvent<T>, crossinline lambda: (T) -> Unit) {
    source.observe(this) { event ->
        event?.getContentIfNotHandled()?.let(lambda)
    }
}

fun <T> T.toEvent() where T : Any = Event(this)
