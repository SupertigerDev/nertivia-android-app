package com.supertiger.nertivia

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject


object RxBus {
    private val publisher = PublishSubject.create<Any>()

    fun publish(event: Any) {
        publisher.onNext(event)
    }
    fun  <T> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)
}