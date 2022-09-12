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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appmattus.certificatetransparency.certificateTransparencyInterceptor
import com.rbrauwers.ctissue.ui.theme.CTIssueTheme
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
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

    val appHttpClient =  OkHttpClient.Builder().apply {
        addNetworkInterceptor(certificateTransparencyInterceptor())
    }.build()

    val sdkHttpClient =  OkHttpClient.Builder().apply {
        addNetworkInterceptor(certificateTransparencyInterceptor())
    }.build()

    val appRetrofit = Retrofit.Builder()
        .baseUrl("https://api.publicapis.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(appHttpClient)
        .build()

    val sdkRetrofit = Retrofit.Builder()
        .baseUrl("https://api.publicapis.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(sdkHttpClient)
        .build()

    val appApi = appRetrofit.create(PublicApi::class.java)
    val sdkApi = sdkRetrofit.create(PublicApi::class.java)

    LaunchedEffect(true) {
        runCatching {
            appApi.getEntries()
        }.onSuccess {
            uiState.value = uiState.value.copy(
                appApiState = "App API response: ${it.toString().take(40)}"
            )
        }.onFailure {
            uiState.value = uiState.value.copy(
                appApiState = "App API error: $it"
            )
        }

        delay(2000)

        runCatching {
            sdkApi.getCategories()
        }.onSuccess {
            uiState.value = uiState.value.copy(
                sdkApiState = "SDK API response: ${it.toString().take(40)}"
            )
        }.onFailure {
            uiState.value = uiState.value.copy(
                sdkApiState = "SDK API error: $it"
            )
        }
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

        uiState.value.appApiState?.let { state ->
            Text(text = state)
            Spacer(modifier = Modifier.height(20.dp))
        }

        uiState.value.sdkApiState?.let { state ->
            Text(text = state)
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

private data class UIState(
    val appCTState: String? = null,
    val sdkCTState: String? = null,
    val appApiState: String? = null,
    val sdkApiState: String? = null
)

private interface PublicApi {
    @GET("entries")
    suspend fun getEntries(): Any

    @GET("categories")
    suspend fun getCategories(): Any
}