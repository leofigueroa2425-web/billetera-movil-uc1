package com.tuapp.billetera

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun launchShowsOnlyLoginWithBlankCredentials() {
        compose.onNodeWithText("Bienvenido a tu billetera").assertIsDisplayed()
        compose.onNodeWithText("Saldo disponible").assertDoesNotExist()
        compose.onNodeWithText("84100001").assertDoesNotExist()
        compose.onNodeWithText("123456").assertDoesNotExist()
        compose.onAllNodes(hasSetTextAction()).assertCountEquals(2)
        compose.onNodeWithText("Iniciar sesión").performScrollTo().assertIsNotEnabled()
    }
}
