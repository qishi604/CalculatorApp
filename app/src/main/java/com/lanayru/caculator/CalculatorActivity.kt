package com.lanayru.caculator

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lanayru.caculator.ui.theme.CaculatorTheme

class CalculatorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.setDecorFitsSystemWindows(false)
        }
        super.onCreate(savedInstanceState)
        setContent {
            CaculatorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Calculator()
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}

// region: UI

@Preview
@Composable
fun Calculator() {
    val width = LocalConfiguration.current.screenWidthDp
    val height = LocalConfiguration.current.screenHeightDp

    val landscape = width * 1f / height > 1.4

    if (landscape) {
        Row {
            Panel(modifier = Modifier.weight(3f))
            Screen(
                modifier = Modifier
                    .weight(2f)
            )
        }

    } else {
        Column {
            Screen(
                modifier = Modifier
                    .weight(2f)
            )
            Panel(modifier = Modifier.weight(3f))
        }
    }
}

@Composable
fun Screen(modifier: Modifier, viewModel: CalculatorViewModel = viewModel()) {
    val text = viewModel.textState.observeAsState()
    Box(
        modifier =
        modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = text.value ?: "",
            modifier = Modifier.align(Alignment.CenterEnd),
            style = TextStyle(
                MaterialTheme.colorScheme.primary, fontSize = 48.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
        )
    }

}

@Composable
fun Panel(modifier: Modifier) {
    Column(modifier = modifier) {
        EachRow(step = 4) { list ->
            OneRow(list, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun EachRow(step: Int, content: @Composable (List<Symbol>) -> Unit) {
    for (i in mSymbols.indices step step) {
        val list = mSymbols.subList(i, i + step)
        Log.d(TAG, "EachRow: $list")
        content.invoke(list)
    }
}

@Composable
fun OneRow(
    symbols: List<Symbol>,
    modifier: Modifier,
    viewModel: CalculatorViewModel = viewModel()
) {
    Row(modifier = modifier) {
        symbols.forEach { data ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(1.dp)
                    .background(MaterialTheme.colorScheme.onPrimary)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple()
                    ) {
                        viewModel.onClick(data)
                    },


                ) {
                Text(
                    text = data.text, modifier = Modifier
                        .align(Alignment.Center),

                    style = TextStyle(
                        MaterialTheme.colorScheme.primary, fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}

// endregion: UI

// region: ViewModel

class CalculatorViewModel : ViewModel() {

    private val _textState = MutableLiveData("0")
    val textState: LiveData<String> = _textState

    fun onClick(symbol: Symbol) {
        _textState.value = symbol.text
    }
}

// endregion


data class Symbol(
    val text: String,
    val value: String,
)

private val mSymbols = listOf(
    Symbol("%", "%"),
    Symbol("C", "C"),
    Symbol("Del", "D"),
    Symbol("/", "/"),

    Symbol("7", "7"),
    Symbol("8", "8"),
    Symbol("9", "9"),
    Symbol("*", "*"),

    Symbol("4", "4"),
    Symbol("5", "5"),
    Symbol("6", "6"),
    Symbol("-", "-"),

    Symbol("1", "1"),
    Symbol("2", "2"),
    Symbol("3", "3"),
    Symbol("+", "+"),

    Symbol("+/-", "+/-"),
    Symbol("0", "0"),
    Symbol(".", "."),
    Symbol("=", "="),
)

private const val TAG = "Calculator"