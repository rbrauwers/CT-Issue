package com.rbrauwers.ctissue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appmattus.certificatetransparency.installCertificateTransparencyProvider
import com.rbrauwers.ctissue.ui.theme.CTIssueTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CTIssueTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CTSample()
                }
            }
        }
    }
}

@Composable
fun CTSample() {
    val uiState = remember {
        mutableStateOf(UIState())
    }

    LaunchedEffect(true) {
        installAppCT(uiState)
        delay(2000)

        installSDKCT(uiState)
        delay(2000)

        callApi(uiState)
    }

    Column(modifier = Modifier.padding(24.dp)) {
        uiState.value.appCTState?.let { appCTState ->
            Text(text = appCTState)
            Spacer(modifier = Modifier.height(20.dp))
        }

        uiState.value.sdkCTState?.let { sdkCTState ->
            Text(text = sdkCTState)
            Spacer(modifier = Modifier.height(20.dp))
        }

        uiState.value.apiState?.let { apiState ->
            Text(text = apiState)
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

private suspend fun installAppCT(uiState: MutableState<UIState>) {
    uiState.value = uiState.value.copy(
        appCTState = "App CT: installing"
    )

    delay(2000)

    installCertificateTransparencyProvider {  }

    uiState.value = uiState.value.copy(
        appCTState = "App CT: installed"
    )
}

private suspend fun installSDKCT(uiState: MutableState<UIState>) {
    uiState.value = uiState.value.copy(
        sdkCTState = "SDK CT: installing"
    )

    delay(2000)

    installCertificateTransparencyProvider {  }

    uiState.value = uiState.value.copy(
        sdkCTState = "SDK CT: installed"
    )
}

private suspend fun callApi(uiState: MutableState<UIState>) {
    uiState.value = uiState.value.copy(
        apiState = "Will crash on http request"
    )

    delay(2000)

    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.publicapis.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(PublicApi::class.java)

    runCatching {
        withContext(Dispatchers.IO) {
            api.getEntries()
        }
    }.onSuccess {
        withContext(Dispatchers.Default) {
            uiState.value = uiState.value.copy(
                apiState = "Api response: $it"
            )
        }
    }.onFailure {
        withContext(Dispatchers.Default) {
            uiState.value = uiState.value.copy(
                apiState = "Api error: $it"
            )
        }
    }
}

private data class UIState(
    val appCTState: String? = null,
    val sdkCTState: String? = null,
    val apiState: String? = null
)

private interface PublicApi {
    @GET("entries")
    suspend fun getEntries(): Any
}