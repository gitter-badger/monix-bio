/*
 * Copyright (c) 2019-2019 by The Monix Project Developers.
 * See the project homepage at: https://monix.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package monix.bio.internal

import monix.bio.WRYYY
import monix.bio.WRYYY.{Context, ContextSwitch, Options}

private[bio] object TaskExecuteWithOptions {
  /**
    * Implementation for `Task.executeWithOptions`
    */
  def apply[E, A](self: WRYYY[E, A], f: Options => Options): WRYYY[E, A] =
    ContextSwitch(self, enable(f), disable)

  private[this] def enable(f: Options => Options): Context => Context =
    ctx => {
      val opts2 = f(ctx.options)
      if (opts2 != ctx.options) ctx.withOptions(opts2)
      else ctx
    }

  private[this] val disable: (Any, Any, Context, Context) => Context =
    (_, _, old, current) => current.withOptions(old.options)
}