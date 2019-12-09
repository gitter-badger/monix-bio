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

package monix.bio

import cats.effect.{Effect, IO}
import monix.bio.WRYYY.Options
import monix.execution.schedulers.TracingScheduler

import scala.concurrent.duration._

object TaskEffectInstanceSuite extends BaseTestSuite {
  val readOptions: UIO[WRYYY.Options] =
    WRYYY.Async[Nothing, Options] { (ctx, cb) =>
      cb.onSuccess(ctx.options)
    }

  test("Effect instance should make use of implicit TaskOptions") { implicit sc =>
    implicit val customOptions: WRYYY.Options = WRYYY.Options(
      autoCancelableRunLoops = true,
      localContextPropagation = true
    )

    var received: WRYYY.Options = null

    val io = Effect[Task].runAsync(readOptions) {
      case Right(opts) =>
        received = opts
        IO.unit
      case _ =>
        fail()
        IO.unit
    }

    io.unsafeRunSync()
    sc.tick(1.day)
    assertEquals(received, customOptions)
  }

  test("Effect instance should use Task.defaultOptions with default TestScheduler") { implicit sc =>
    var received: WRYYY.Options = null
    val io = Effect[Task].runAsync(readOptions) {
      case Right(opts) =>
        received = opts
        IO.unit
      case _ =>
        fail()
        IO.unit
    }

    io.unsafeRunSync()
    sc.tick(1.day)
    assertEquals(received, WRYYY.defaultOptions)
    assert(!received.localContextPropagation)
  }

  test("Effect instance should use Task.defaultOptions.withSchedulerFeatures") { sc =>
    implicit val tracing = TracingScheduler(sc)

    var received: WRYYY.Options = null
    val io = Effect[Task].runAsync(readOptions) {
      case Right(opts) =>
        received = opts
        IO.unit
      case _ =>
        fail()
        IO.unit
    }

    io.unsafeRunSync()
    sc.tick(1.day)
    assertEquals(received, WRYYY.defaultOptions.withSchedulerFeatures)
    assert(received.localContextPropagation)
  }
}
