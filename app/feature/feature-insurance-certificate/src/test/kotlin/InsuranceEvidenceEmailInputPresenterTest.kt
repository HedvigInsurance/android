package com.hedvig.android.feature.insurance.certificate.ui.email

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.insurance.certificate.data.GenerateInsuranceEvidenceUseCase
import com.hedvig.android.feature.insurance.certificate.data.GetInsuranceEvidenceInitialEmailUseCase
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

class InsuranceEvidenceEmailInputPresenterTest {
    private fun createPresenterWithFakes(): Triple<InsuranceEvidenceEmailInputPresenter, FakeGetInsuranceEvidenceInitialEmailUseCase, FakeGenerateInsuranceEvidenceUseCase> {
        val getEmailUseCase = FakeGetInsuranceEvidenceInitialEmailUseCase()
        val generateUseCase = FakeGenerateInsuranceEvidenceUseCase()
        val presenter = InsuranceEvidenceEmailInputPresenter(
            generateInsuranceEvidenceUseCase = generateUseCase,
            getEmailUseCase = getEmailUseCase,
        )
        return Triple(presenter, getEmailUseCase, generateUseCase)
    }

    @Test
    fun `when loading email fails show failure state`() = runTest {
        val (presenter, getEmailUseCase, _) = createPresenterWithFakes()
        presenter.test(InsuranceEvidenceEmailInputState.Loading) {
            skipItems(1)
            getEmailUseCase.emailTurbine.add(ErrorMessage().left())
            assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Failure::class)
        }
    }

    @Test
    fun `when loading email succeeds show success state with email`() = runTest {
        val (presenter, getEmailUseCase, _) = createPresenterWithFakes()
        presenter.test(InsuranceEvidenceEmailInputState.Loading) {
            skipItems(1)
            getEmailUseCase.emailTurbine.add("test@example.com".right())
            assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Success::class)
                .prop(InsuranceEvidenceEmailInputState.Success::email)
                .isEqualTo("test@example.com")
        }
    }

    @Test
    fun `when loading email succeeds with empty email show success state with empty email`() =
        runTest {
            val (presenter, getEmailUseCase, _) = createPresenterWithFakes()
            presenter.test(InsuranceEvidenceEmailInputState.Loading) {
                skipItems(1)
                getEmailUseCase.emailTurbine.add("".right())
                assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Success::class)
                    .prop(InsuranceEvidenceEmailInputState.Success::email)
                    .isEqualTo("")
            }
        }

    @Test
    fun `when retry is clicked reload email`() = runTest {
        val (presenter, getEmailUseCase, _) = createPresenterWithFakes()
        presenter.test(InsuranceEvidenceEmailInputState.Loading) {
            getEmailUseCase.emailTurbine.add(ErrorMessage().left())
            skipItems(1)
            assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Failure::class)
            sendEvent(InsuranceEvidenceEmailInputEvent.RetryLoadData)
            getEmailUseCase.emailTurbine.add("retry@example.com".right())
            assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Success::class)
                .prop(InsuranceEvidenceEmailInputState.Success::email)
                .isEqualTo("retry@example.com")
        }
    }

    @Test
    fun `when email input is changed update email in state`() = runTest {
        val (presenter, getEmailUseCase, _) = createPresenterWithFakes()
        presenter.test(InsuranceEvidenceEmailInputState.Loading) {
            getEmailUseCase.emailTurbine.add("initial@example.com".right())
            skipItems(1)
            assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Success::class)
                .prop(InsuranceEvidenceEmailInputState.Success::email)
                .isEqualTo("initial@example.com")
            sendEvent(InsuranceEvidenceEmailInputEvent.ChangeEmailInput("new@example.com"))
            assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Success::class)
                .prop(InsuranceEvidenceEmailInputState.Success::email)
                .isEqualTo("new@example.com")
        }
    }

    @Test
    fun `when submit is clicked with valid email generate certificate`() = runTest {
        val (presenter, getEmailUseCase, generateUseCase) = createPresenterWithFakes()
        presenter.test(InsuranceEvidenceEmailInputState.Loading) {
            getEmailUseCase.emailTurbine.add("valid@example.com".right())
            sendEvent(InsuranceEvidenceEmailInputEvent.Submit)
            skipItems(2)
            assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Success::class)
                .prop(InsuranceEvidenceEmailInputState.Success::buttonLoading)
                .isTrue()

            assertThat(generateUseCase.generateTurbine.awaitItem()).isEqualTo("valid@example.com")
        }
    }

    @Test
    fun `when submit is clicked with invalid email show validation error`() = runTest {
        val (presenter, _, _) = createPresenterWithFakes()
        presenter.test(InsuranceEvidenceEmailInputState.Success(email = "invalid-email")) {
            assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Success::class)
                .prop(InsuranceEvidenceEmailInputState.Success::emailValidationErrorMessage)
                .isNull()
            sendEvent(InsuranceEvidenceEmailInputEvent.Submit)
            assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Success::class)
                .prop(InsuranceEvidenceEmailInputState.Success::emailValidationErrorMessage)
                .isNotNull()
        }
    }

    @Test
    fun `when submit is clicked with empty email show empty email error`() = runTest {
        val (presenter, _, _) = createPresenterWithFakes()
        presenter.test(InsuranceEvidenceEmailInputState.Success(email = "")) {
            assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Success::class)
                .prop(InsuranceEvidenceEmailInputState.Success::emailValidationErrorMessage)
                .isNull()
            sendEvent(InsuranceEvidenceEmailInputEvent.Submit)
            assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Success::class)
                .prop(InsuranceEvidenceEmailInputState.Success::emailValidationErrorMessage)
                .isNotNull()
        }
    }

    @Test
    fun `when generating certificate succeeds update state with certificate url`() = runTest {
        val (presenter, _, generateUseCase) = createPresenterWithFakes()
        presenter.test(InsuranceEvidenceEmailInputState.Success(email = "valid@example.com")) {
            sendEvent(InsuranceEvidenceEmailInputEvent.Submit)
            skipItems(1)
            assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Success::class)
                .prop(InsuranceEvidenceEmailInputState.Success::buttonLoading)
                .isTrue()
            generateUseCase.resultTurbine.add("https://certificate.url".right())
            assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Success::class)
                .all {
                    prop(InsuranceEvidenceEmailInputState.Success::fetchedCertificateUrl)
                        .isEqualTo("https://certificate.url")
                    prop(InsuranceEvidenceEmailInputState.Success::buttonLoading)
                        .isFalse()
                }
        }
    }

    @Test
    fun `when generating certificate fails show error message`() = runTest {
        val (presenter, _, generateUseCase) = createPresenterWithFakes()
        presenter.test(InsuranceEvidenceEmailInputState.Success(email = "valid@example.com")) {
            sendEvent(InsuranceEvidenceEmailInputEvent.Submit)
            skipItems(2)
            generateUseCase.generateTurbine.awaitItem()
            generateUseCase.resultTurbine.add(ErrorMessage().left())
            assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Success::class)
                .all {
                    prop(InsuranceEvidenceEmailInputState.Success::generatingErrorMessage)
                        .isNotNull()
                    prop(InsuranceEvidenceEmailInputState.Success::buttonLoading)
                        .isFalse()
                }
        }
    }

    @Test
    fun `when clear navigation is clicked remove certificate url from state`() = runTest {
        val (presenter, _, _) = createPresenterWithFakes()
        presenter.test(InsuranceEvidenceEmailInputState.Success(
            "test@example.com",
            fetchedCertificateUrl = "https://certificate.url")) {
            assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Success::class)
                .prop(InsuranceEvidenceEmailInputState.Success::fetchedCertificateUrl)
                .isNotNull()
            sendEvent(InsuranceEvidenceEmailInputEvent.ClearNavigation)
            assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Success::class)
                .prop(InsuranceEvidenceEmailInputState.Success::fetchedCertificateUrl)
                .isNull()
        }
    }

    @Test
    fun `when clear error message is clicked remove error message from state`() = runTest {
        val (presenter, _, _) = createPresenterWithFakes()
        presenter.test(
            InsuranceEvidenceEmailInputState.Success(
                email = "",
                generatingErrorMessage = 123,
            ),
        ) {
            assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Success::class)
                .prop(InsuranceEvidenceEmailInputState.Success::generatingErrorMessage)
                .isNotNull()
            sendEvent(InsuranceEvidenceEmailInputEvent.ClearErrorMessage)
            assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Success::class)
                .prop(InsuranceEvidenceEmailInputState.Success::generatingErrorMessage)
                .isNull()
        }
    }

    @Test
    fun `when submitting show loading state on button`() = runTest {
        val (presenter, getEmailUseCase, _) = createPresenterWithFakes()
        presenter.test(InsuranceEvidenceEmailInputState.Loading) {
            getEmailUseCase.emailTurbine.add("valid@example.com".right())
            skipItems(1)
            assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Success::class)
                .prop(InsuranceEvidenceEmailInputState.Success::buttonLoading)
                .isFalse()
            sendEvent(InsuranceEvidenceEmailInputEvent.Submit)
            assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Success::class)
                .prop(InsuranceEvidenceEmailInputState.Success::buttonLoading)
                .isTrue()
        }
    }

    @Test
    fun `when changing email after validation error clear the error`() = runTest {
        val (presenter, _, _) = createPresenterWithFakes()
        presenter.test(
            InsuranceEvidenceEmailInputState.Success(
                email = "invalid-email",
                emailValidationErrorMessage = 123,
            ),
        ) {
            assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Success::class)
                .prop(InsuranceEvidenceEmailInputState.Success::emailValidationErrorMessage)
                .isNotNull()
            sendEvent(InsuranceEvidenceEmailInputEvent.ChangeEmailInput("new@example.com"))
            assertThat(awaitItem()).isInstanceOf(InsuranceEvidenceEmailInputState.Success::class)
                .prop(InsuranceEvidenceEmailInputState.Success::emailValidationErrorMessage)
                .isNull()
        }
    }
}

private class FakeGetInsuranceEvidenceInitialEmailUseCase :
    GetInsuranceEvidenceInitialEmailUseCase {
    val emailTurbine = Turbine<Either<ErrorMessage, String>>()

    override suspend fun invoke(): Either<ErrorMessage, String> {
        return emailTurbine.awaitItem()
    }
}

private class FakeGenerateInsuranceEvidenceUseCase : GenerateInsuranceEvidenceUseCase {
    val generateTurbine = Turbine<String>()
    val resultTurbine = Turbine<Either<ErrorMessage, String>>()

    override suspend fun invoke(email: String): Either<ErrorMessage, String> {
        generateTurbine.add(email)
        return resultTurbine.awaitItem()
    }
}
