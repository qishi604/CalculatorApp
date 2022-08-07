package com.lanayru.caculator

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.math.BigDecimal
import java.util.*


// region: UI

@Preview
@Composable
fun Calculator() {
    val width = LocalConfiguration.current.screenWidthDp
    val height = LocalConfiguration.current.screenHeightDp

    val landscape = width * 1f / height > 1.4

    if (landscape) {
        Row(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            Panel(modifier = Modifier.weight(3f))
            Screen(
                modifier = Modifier
                    .weight(2f)
            )
        }

    } else {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
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
            .background(MaterialTheme.colorScheme.onBackground)
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState(), reverseScrolling = true)
    ) {
        Text(
            text = text.value ?: "",
            modifier = Modifier.align(Alignment.CenterEnd),
            style = TextStyle(
                MaterialTheme.colorScheme.onPrimary, fontSize = 48.sp, fontWeight = FontWeight.Bold,
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
fun EachRow(step: Int, content: @Composable (List<Key>) -> Unit) {
    for (i in mKeys.indices step step) {
        val list = mKeys.subList(i, i + step)
        Log.d(TAG, "EachRow: $list")
        content.invoke(list)
    }
}

@Composable
fun OneRow(
    symbols: List<Key>,
    modifier: Modifier,
    viewModel: CalculatorViewModel = viewModel()
) {
    Row(modifier = modifier) {
        symbols.forEach { key ->
            val fontSize: TextUnit
            val itemColor : Color
            val fontColor : Color
            if (key is Num || key is Key_Rev) {
                fontSize = 24.sp
                itemColor = MaterialTheme.colorScheme.secondary
                fontColor = MaterialTheme.colorScheme.onPrimary
            } else {
                fontSize = 20.sp
                itemColor = MaterialTheme.colorScheme.tertiary
                fontColor = MaterialTheme.colorScheme.onTertiary
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(1.dp)
                    .background(itemColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple()
                    ) {
                        viewModel.onClick(key)
                    },


                ) {
                Text(
                    text = key.value, modifier = Modifier
                        .align(Alignment.Center),

                    style = TextStyle(
                        fontColor, fontSize = fontSize,
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

    /** Is Stack top Number */
    private var _isNumber = true

    private val _textState: MutableLiveData<String> = MutableLiveData()
    val textState: LiveData<String> = _textState


    private val _stack = Stack<String>()

    init {
        reset()
    }

    fun onClick(key: Key) {
        VibrateUtils.vibrate()
        when (key) {
            is key_Clear -> {
                reset()
                return
            }

            is Key_Del -> {
                val old = _stack.pop()
                var newValue = old.dropLast(1)
                if (newValue.isEmpty()) {
                    newValue = if (_stack.size <= 0) {
                        "0"
                    } else {
                        ""
                    }
                }

                setNewValue(newValue)
                return
            }
            else -> {
                // ignore
            }
        }

        if (_isNumber) {
            when (key) {
                is Num -> {
                    val old = _stack.pop()
                    val isDot = key is Num_Dot
                    val newValue = when {
                        old == "0" && !isDot -> key.value

                        old.contains(".") && isDot -> old

                        else -> old + key.value

                    }

                    setNewValue(newValue)
                }

                is Key_Rev -> {
                    val old = _stack.pop()
                    val newValue = if (old != "0") {
                        if (old.startsWith("-")) {
                            old.substring(1)
                        } else {
                            "-$old"
                        }
                    } else {
                        "0"
                    }
                    setNewValue(newValue)
                }

                is Key_Result -> {
                    calResult()
                }

                is Op -> {
                    if (_stack.size == 3) {
                        calResult()
                    }
                    setNewValue(key.value, false)
                }

                else -> {
                    // ignore
                }
            }

        } else { // is Op
            when (key) {
                is Num -> {
                    setNewValue(key.value)
                }
                is Op -> {
                    _stack.pop()
                    setNewValue(key.value, false)
                }
                else -> {
                    // ignore
                }
            }
        }

    }

    private fun calResult() {
        if (_stack.size != 3) {
            return
        }

        val right = BigDecimal(_stack.pop())
        val op = _stack.pop()
        val left = BigDecimal(_stack.pop())

        val newValue = try {
            when (op) {
                Op_Plus.value -> left + right
                Op_Minus.value -> left - right
                Op_Times.value -> left * right
                Op_Div.value -> left / right
                Op_Mod.value -> left % right
                else -> 0
            }
        } catch (e: Exception) { // if catch Exception, return 0
            0
        }

        setNewValue(newValue.toString())
    }

    private fun setNewValue(newValue: String?, isNumber: Boolean = true) {
        if (!newValue.isNullOrEmpty()) {
            _stack.push(newValue)
        }
        _isNumber = isNumber

        _textState.value = _stack.joinToString(" ")
    }

    private fun reset() {
        _isNumber = true
        _stack.clear()
        _stack.push(Num_0.value)
        _textState.postValue(Num_0.value)
    }
}

// endregion

// region: Data
sealed class Key(
    val value: String,
) {
    override fun toString(): String {
        return value
    }
}

open class Num(value: String) : Key(value)

private object Num_0 : Num("0")
private object Num_Dot : Num(".")

open class Op(value: String) : Key(value)

private object Op_Plus : Op("+")
private object Op_Minus : Op("-")
private object Op_Times : Op("*")
private object Op_Div : Op("/")
private object Op_Mod : Op("%")

private object key_Clear : Key("C")
private object Key_Del : Key("Del")
private object Key_Rev : Key("+/-")
private object Key_Result : Key("=")

private val mKeys: List<Key> = listOf(
    Op_Mod,
    key_Clear,
    Key_Del,
    Op_Div,

    Num("7"),
    Num("8"),
    Num("9"),
    Op_Times,

    Num("4"),
    Num("5"),
    Num("6"),
    Op_Minus,

    Num("1"),
    Num("2"),
    Num("3"),
    Op_Plus,

    Key_Rev,
    Num_0,
    Num_Dot,
    Key_Result,
)
// endregion: Data

private const val TAG = "Calculator"