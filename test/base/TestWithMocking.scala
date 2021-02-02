/*
 * Copyright 2021 HM Revenue & Customs
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

package base

import org.mockito.Mockito
import org.scalatest.BeforeAndAfterEach
import org.scalatest.TestSuite
import org.scalatestplus.mockito.MockitoSugar

private[base] trait TestWithMocking extends MockitoSugar with BeforeAndAfterEach {
  this: TestSuite =>

  final override def beforeEach(): Unit = {
    if (mocks.nonEmpty) Mockito.reset(mocks: _*)
    if (beforeEachBlocks.nonEmpty) beforeEachBlocks.foreach(_.apply())
  }

  /**
    * An overrideable hook that allows the user to specify additional
    * blocks of code that will be run in addition to mocks being reset.
    *
    * @note If there are blocks of code in the parent of the test sute,
    *       then this should be used like: `super.beforeEachBlocks ++ Seq(() => println("hook1") , () => println("hook2"))`
    */
  def beforeEachBlocks: Seq[() => Unit] = Seq.empty

  /**
    * An overrideable hook that allows the user to specify all the mocks
    * and they will be reset by Mockito automatically.
    *
    * @note If there are mocks in the parent of the test sute, then this
    *       should be used like: `super.mocks ++ Seq(newMock1, newMock2)`
    */
  def mocks: Seq[_] = Seq.empty
}
