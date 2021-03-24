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

package repositories

import java.time.Clock
import java.time.Instant

import config.AppConfig
import javax.inject.Inject
import logging.Logging
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.LastError
import reactivemongo.api.indexes.IndexType
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import reactivemongo.play.json.collection.JSONCollection
import logging.TagUtil._

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class LockRepository @Inject() (
  appConfig: AppConfig,
  mongo: ReactiveMongoApi,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends Logging {
  import MongoInstantFormats._

  private val alreadyLocked = 11000

  val collectionName: String = "locks"

  private val ttl = appConfig.mongoLockTtlInSeconds

  private val createdIndex =
    IndexUtils.index(
      key = Seq("created" -> IndexType.Ascending),
      name = Some("created-index"),
      unique = true,
      expireAfterSeconds = Some(ttl)
    )

  private lazy val ensureIndexes: Future[Boolean] = {
    logger.info("Ensuring indexes")
    for {
      coll <- mongo.database.map(_.collection[JSONCollection](collectionName))
      _    <- coll.indexesManager.ensure(createdIndex)
    } yield true
  }

  def collection: Future[JSONCollection] =
    for {
      _    <- ensureIndexes
      coll <- mongo.database.map(_.collection[JSONCollection](collectionName))
    } yield coll

  def lock(key: String): Future[LockResult] = {

    val lock = Json.obj(
      "_id"     -> key,
      "created" -> Instant.now(clock)
    )

    collection.flatMap {
      _.insert(ordered = false)
        .one(lock)
        .map(_ => LockResult.LockAcquired)
    } recover {
      case e: LastError if e.code contains alreadyLocked =>
        LockResult.AlreadyLocked
      case e: Throwable =>
        logger.error(s"${LockException.toString} Error trying to get lock $key", e)
        throw e
    }
  }

  def unlock(key: String): Future[Boolean] =
    collection.flatMap {
      _.delete(ordered = false)
        .one(Json.obj("_id" -> key))
        .map(_ => true)
    } recover {
      case e: Throwable =>
        logger.error(s"${UnlockException.toString} Error trying to remove lock $key", e)
        false
    }
}
