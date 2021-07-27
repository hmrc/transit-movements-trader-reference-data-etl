package scheduler

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import base.SpecBase
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SchedulerActionsSyncSpec extends SpecBase {

  val mockImportDataService = mock[services.ImportDataService]

  "when the import succeeds, then the service returns true" in {
    when(mockImportDataService.importReferenceData()(any())).thenReturn(Future.successful(true))

    val service = new SchedulerActionsSync(mockImportDataService)

    service.triggerReferenceDataImport().futureValue mustEqual true
  }

  "when the import fails, then the service returns false" in {
    when(mockImportDataService.importReferenceData()(any())).thenReturn(Future.successful(false))

    val service = new SchedulerActionsSync(mockImportDataService)

    service.triggerReferenceDataImport().futureValue mustEqual false
  }
}
