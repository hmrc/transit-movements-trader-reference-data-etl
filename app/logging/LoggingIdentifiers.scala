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

package logging

object LoggingIdentifiers {

  // Alerting on these identifiers
  object ImportException                          extends WithName("ImportException")
  object ImportFailure                            extends WithName("ImportFailure")
  object UNEXPECTED_LIST_ITEM_FILTERING_EXCEPTION extends WithName("UNEXPECTED_LIST_ITEM_FILTERING_EXCEPTION")
  object DataTransformationFailure                extends WithName("DataTransformationFailure")
  object LockException                            extends WithName("LockException")
  object UnlockException                          extends WithName("UnlockException")

  // No alerting on these identifiers
  object AcquiredLock     extends WithName("AcquiredLock")
  object AlreadyLocked    extends WithName("AlreadyLocked")
  object ImportSuccessful extends WithName("ImportSuccessful")
  object ReceivedMessage  extends WithName("ReceivedMessage")

}
