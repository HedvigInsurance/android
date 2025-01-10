package ui
import kotlinx.coroutines.test.runTest
import org.junit.Test


class EnterNewAddressPresenterTest {

    @Test
    fun `movingFlowState displays correctly`() = runTest {

      // Simulate receiving a movingFlowState and verify the content state is updated accordingly
    }

    @Test
    fun `navigating to chose coverage clears navigation flag`() = runTest {

      // Simulate navigation to chose coverage and verify that navigateToChoseCoverage is reset to false
    }

    @Test
    fun `navigating to add house information clears navigation flag`() = runTest {

      // Simulate navigation to add house information and verify that navigateToAddHouseInformation is reset to false
    }

    @Test
    fun `dismiss submission error clears error state`() = runTest {

      // Simulate dismissing a submission error and verify that submittingInfoFailure is set to null
    }

    @Test
    fun `submit with valid content triggers repository update`() = runTest {

      // Simulate a Submit event with valid content and verify that movingFlowRepository is updated with the correct data
    }


  @Test
  fun `submit with not valid content does nothing`() = runTest {

    // Simulate a Submit event with valid content and verify that movingFlowRepository is updated with the correct data
  }

    @Test
    fun `submit navigates to add house information for house property type`() = runTest {

      // Simulate a Submit event with a house property type and verify that navigateToAddHouseInformation is set to true
    }

    @Test
    fun `submit triggers move intent request for apartment property type`() = runTest {

      // Simulate a Submit event with an apartment property type and verify that inputForSubmission is set correctly
    }

    @Test
    fun `if move intent request gets good response navigate to choose coverage`() = runTest {

      // Simulate a scenario where inputForSubmission is set, and verify that the mutation is executed and a successful response is handled correctly
    }

    @Test
    fun `if move intent request gets user error with message show error section with this message`() = runTest {

      // Simulate a scenario where inputForSubmission is set, and verify that the mutation handles network failure correctly
    }

    @Test
    fun `if move intent request gets bad response without specific error show general error section`() = runTest {

      // Simulate a scenario where inputForSubmission is set, and verify that the mutation handles user errors correctly
    }
  }
