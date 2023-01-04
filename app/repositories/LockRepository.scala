/*
 * Copyright 2023 HM Revenue & Customs
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

package repositories

import com.mongodb.MongoWriteException
import config.AppConfig
import logging.Logging
import logging.LoggingIdentifiers.LockException
import logging.LoggingIdentifiers.UnlockException
import org.mongodb.scala.model.Filters
import org.mongodb.scala.model.IndexModel
import org.mongodb.scala.model.IndexOptions
import org.mongodb.scala.model.Indexes
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.MongoUtils.DuplicateKey
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Clock
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class LockRepository @Inject() (
  mongoComponent: MongoComponent,
  appConfig: AppConfig,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[JsObject](
      mongoComponent = mongoComponent,
      collectionName = LockRepository.collectionName,
      domainFormat = implicitly,
      indexes = LockRepository.indexes(appConfig),
      replaceIndexes = appConfig.replaceIndexes
    )
    with Logging {

  def lock(key: String): Future[LockResult] = {
    val lock = Json.obj(
      "_id"     -> key,
      "created" -> Json.toJson(Instant.now(clock))(MongoJavatimeFormats.instantWrites)
    )

    collection
      .insertOne(lock)
      .toFuture()
      .map {
        _ => LockResult.LockAcquired
      }
      .recover {
        case e: MongoWriteException if e.getCode == DuplicateKey.Code =>
          LockResult.AlreadyLocked
        case e: Throwable =>
          logger.error(s"${LockException.toString} Error trying to get lock $key", e)
          throw e
      }
  }

  def unlock(key: String): Future[Boolean] =
    collection
      .deleteOne(Filters.eq("_id", key))
      .toFuture()
      .map {
        _ => true
      }
      .recover {
        case e: Throwable =>
          logger.error(s"${UnlockException.toString} Error trying to remove lock $key", e)
          false
      }

}

object LockRepository {
  val collectionName: String = "locks"

  def indexes(appConfig: AppConfig): Seq[IndexModel] = {
    val createdIndex: IndexModel = IndexModel(
      keys = Indexes.ascending("created"),
      indexOptions = IndexOptions()
        .name("created-index")
        .unique(true)
        .expireAfter(appConfig.mongoLockTtlInSeconds, TimeUnit.SECONDS)
    )

    Seq(createdIndex)
  }
}
