/*
 * Copyright 2016 SmartDengg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.smartdengg.rxgallery.core;

import android.util.Log;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.TimeInterval;

/**
 * Created by Joker on 2016/6/28.
 */
class TimeTransformer<T> implements Observable.Transformer<T, T> {

  @Override public Observable<T> call(Observable<T> observable) {
    return observable.timeInterval().map(new Func1<TimeInterval<T>, T>() {
      @Override public T call(TimeInterval<T> tTimeInterval) {
        long milliseconds = tTimeInterval.getIntervalInMilliseconds();
        Log.d("RxGallery", " \u21E2\u21E2\u21E2 RxGallery casts : " + milliseconds);
        return tTimeInterval.getValue();
      }
    });
  }
}
