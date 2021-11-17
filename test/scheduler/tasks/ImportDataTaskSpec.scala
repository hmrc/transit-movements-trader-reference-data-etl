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

package scheduler.tasks

import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.EitherValues
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import repositories.LockRepository
import repositories.LockResult
import scheduler.jobs.JobName
import scheduler.jobs.ScheduleStatus.MongoLockException
import scheduler.jobs.ScheduleStatus.UnknownExceptionOccurred
import scheduler.services.ImportDataService

import scala.concurrent.Future

class ImportDataTaskSpec extends AnyFreeSpec with Matchers with MockitoSugar with ScalaFutures with BeforeAndAfterEach with EitherValues with OptionValues {

  private val mockLockRepo          = mock[LockRepository]
  private val mockImportDataService = mock[ImportDataService]

  private val appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[LockRepository].toInstance(mockLockRepo),
        bind[ImportDataService].toInstance(mockImportDataService)
      )

  override def beforeEach(): Unit = {
    reset(mockLockRepo)
    reset(mockImportDataService)
    super.beforeEach()
  }

  ".run" - {

    "when a lock can be acquired" - {

      "and importing data is successful" - {

        "must return true and release the lock" in {

          val lock = JobName.ImportData

          when(mockLockRepo.lock(eqTo(lock))) thenReturn Future.successful(LockResult.LockAcquired)
          when(mockLockRepo.unlock(eqTo(lock))) thenReturn Future.successful(true)
          when(mockImportDataService.importReferenceData()(any())) thenReturn Future.successful(true)

          val app = appBuilder.build()

          running(app) {

            val task = app.injector.instanceOf[ImportDataTask]

            val result = task.run().futureValue

            result.value.value mustEqual true
            verify(mockLockRepo, times(1)).unlock(eqTo(lock))
          }
        }

        "and releasing the lock fails" - {

          "must return the overall result" in {

            val lock      = JobName.ImportData
            val exception = new Exception("foo")

            when(mockLockRepo.lock(eqTo(lock))) thenReturn Future.successful(LockResult.LockAcquired)
            when(mockLockRepo.unlock(eqTo(lock))) thenReturn Future.failed(exception)
            when(mockImportDataService.importReferenceData()(any())) thenReturn Future.successful(true)

            val app = appBuilder.build()

            running(app) {

              val task = app.injector.instanceOf[ImportDataTask]

              val result = task.run().futureValue

              result.value.value mustEqual true
            }
          }
        }
      }

      "and importing data returns a failed future" - {

        "and the lock can be released" - {

          "must return an exception and release the lock" in {

            val lock      = JobName.ImportData
            val exception = new Exception("foo")

            when(mockLockRepo.lock(eqTo(lock))) thenReturn Future.successful(LockResult.LockAcquired)
            when(mockLockRepo.unlock(eqTo(lock))) thenReturn Future.successful(true)
            when(mockImportDataService.importReferenceData()(any())) thenReturn Future.failed(exception)

            val app = appBuilder.build()

            running(app) {

              val task = app.injector.instanceOf[ImportDataTask]

              val result = task.run().futureValue

              result.left.value mustEqual UnknownExceptionOccurred(exception)
              verify(mockLockRepo, times(1)).unlock(eqTo(lock))
            }
          }
        }

        "and trying to release the lock returns a failed future" - {

          "must return the exception the occurred trying to import the data" in {

            val lock            = JobName.ImportData
            val exception       = new Exception("foo")
            val unlockException = new Exception("bar")

            when(mockLockRepo.lock(eqTo(lock))) thenReturn Future.successful(LockResult.LockAcquired)
            when(mockLockRepo.unlock(eqTo(lock))) thenReturn Future.failed(unlockException)
            when(mockImportDataService.importReferenceData()(any())) thenReturn Future.failed(exception)

            val app = appBuilder.build()

            running(app) {

              val task = app.injector.instanceOf[ImportDataTask]

              val result = task.run().futureValue

              result.left.value mustEqual UnknownExceptionOccurred(exception)
              verify(mockLockRepo, times(1)).unlock(eqTo(lock))
            }
          }
        }
      }
    }

    "when a lock cannot be acquired" - {

      "must not attempt to import data" in {

        val lock = JobName.ImportData

        when(mockLockRepo.lock(eqTo(lock))) thenReturn Future.successful(LockResult.AlreadyLocked)

        val app = appBuilder.build()

        running(app) {

          val task = app.injector.instanceOf[ImportDataTask]

          val result = task.run().futureValue

          result.value must not be defined
          verify(mockImportDataService, times(0)).importReferenceData()(any())
          verify(mockLockRepo, times(0)).unlock(eqTo(lock))
        }
      }
    }

    "when trying to get a lock returns a failed future" - {

      "must return a Mongo lock exception and not attempt to import data" in {

        val lock      = JobName.ImportData
        val exception = new Exception("foo")

        when(mockLockRepo.lock(eqTo(lock))) thenReturn Future.failed(exception)

        val app = appBuilder.build()

        running(app) {

          val task = app.injector.instanceOf[ImportDataTask]

          val result = task.run().futureValue

          result.left.value mustEqual MongoLockException(exception)
          verify(mockImportDataService, times(0)).importReferenceData()(any())
          verify(mockLockRepo, times(0)).unlock(eqTo(lock))
        }
      }
    }
  }
}
