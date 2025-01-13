package ui

import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class AddHouseInformationPresenterTest {
  @Test
  fun `dismiss submission error clears the error state`() = runTest {
    // todo()
  }

  @Test
  fun `submit event with valid content updates repository with house input`() = runTest {
// todo()
  }

  @Test
  fun `submit event with invalid content does nothing`() = runTest {
// todo()
  }

  @Test
  fun `show error section when repository update fails`() = runTest {
// todo()
  }

  @Test
  fun `when repository update is successful move intent request is triggered`() = runTest {
// todo()
  }

  @Test
  fun `if move intent request gets good response update repo moveIntentQuotes and navigate to choose coverage`() =
    runTest {
// todo()
    }

  @Test
  fun `mutation request with user error sets submission info failure`() = runTest {
// todo()
  }

  @Test
  fun `mutation request success navigates to chose coverage`() = runTest {
// todo()
  }

  @Test
  fun `initial state is loading when last state is loading`() = runTest {
// todo()
  }

  @Test
  fun `initial state is missing ongoing moving flow when last state is missing`() = runTest {
// todo()
  }

  @Test
  fun `initial state is content when last state is content`() = runTest {
    // todo()
  }
}
