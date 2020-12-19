package repositories

import java.time.{Clock, Instant, ZoneId}

import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running

import scala.concurrent.ExecutionContext.Implicits.global

class LockRepositorySpec
  extends AnyFreeSpec
    with Matchers
    with MongoSuite
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    database.flatMap(_.drop).futureValue
    super.beforeEach()
  }

  private val instant = Instant.now
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  private val appBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(bind[Clock].toInstance(stubClock))

  "Lock Repository" - {

    "must create and release a lock" in {

      val app = appBuilder.build()

      running(app) {

        val key = "foo"
        val repo = app.injector.instanceOf[LockRepository]

        val lockResult = repo.lock(key).futureValue

        lockResult mustEqual LockResult.LockAcquired

        val unlockResult = repo.unlock(key).futureValue
        unlockResult mustEqual true
      }
    }

    "must return AlreadyLocked when trying to create a lock that already exists" in {

      val app = appBuilder.build()

      running(app) {

        val key = "foo"
        val repo = app.injector.instanceOf[LockRepository]

        val lockResult = repo.lock(key).futureValue
        val secondLockResult = repo.lock(key).futureValue

        lockResult mustEqual LockResult.LockAcquired
        secondLockResult mustEqual LockResult.AlreadyLocked
      }
    }
  }
}
