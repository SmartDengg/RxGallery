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

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * 创建时间:  2017/01/30 16:26 <br>
 * 作者:  dengwei <br>
 * 描述:
 */
class IoScheduler {

  private IoScheduler() {
    throw new IllegalStateException("No instance");
  }

  private static final Observable.Transformer ioTransformer = new Observable.Transformer() {
    @Override public Object call(Object observable) {
      return ((Observable) observable).subscribeOn(Schedulers.io());
    }
  };

  /**
   * Don't break the chain: use RxJava's compose() operator
   */
  static <T> Observable.Transformer<T, T> apply() {
    //noinspection unchecked
    return (Observable.Transformer<T, T>) ioTransformer;
  }
}
