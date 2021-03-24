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

import logging.Logging
import repositories.LockRepository
import repositories.LockResult
import scheduler.jobs.JobFailed
import scheduler.jobs.ScheduleStatus.MongoLockException
import scheduler.jobs.ScheduleStatus.MongoUnlockException
import scheduler.jobs.ScheduleStatus.UnknownExceptionOccurred

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

trait ScheduledTask[A] extends Logging {
  val lockRepository: LockRepository
  implicit val ec: ExecutionContext

  def run(): Future[A]

  // Remove warn loggers here as its done in repository layer
  protected def withLock[T](lock: String)(block: => Future[Either[JobFailed, Option[T]]]): Future[Either[JobFailed, Option[T]]] =
    lockRepository.lock(lock) flatMap {
      case LockResult.LockAcquired =>
        logger.info("Acquired a lock")

        block.flatMap {
          result =>
            lockRepository
              .unlock(lock)
              .map(_ => result)
              .recover {
                case e: Exception =>
                  logger.warn(s"Unable to release lock $lock") // Make the same
                  result
              }
        } recoverWith {
          case e: Exception =>
            logger.error(s"Something went wrong trying to import reference data", e)
            lockRepository
              .unlock(lock)
              .map(_ => Left(UnknownExceptionOccurred(e)))
              .recover {
                case unlockException: Exception =>
                  logger.error(s"Unable to release lock $lock", unlockException)
                  Left(UnknownExceptionOccurred(e))
              }
        }

      case LockResult.AlreadyLocked =>
        logger.info("Could not get a lock - task may have been run on another instance")
        Future.successful(Right(None))
    } recover {
      case e: Exception =>
        logger.warn("Something went wrong trying to get a lock", e)
        Left(MongoLockException(e))
    }
}

// Create object
// Create tags
//
