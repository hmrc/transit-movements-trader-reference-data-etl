package logging

import models.WithName

object TagUtil {

  case object ImportException       extends WithName("ImportException")
  case object ImportFailure         extends WithName("ImportFailure")

  case object FilterException       extends WithName("FilterException")

  case object JsonValidationFailure extends WithName("JsonValidationFailure")

  case object LockException         extends WithName("LockException")
  case object UnlockException       extends WithName("UnlockException")

}