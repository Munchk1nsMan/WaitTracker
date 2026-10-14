package com.example.trialbuild.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.trialbuild.GraphDataPoint
import com.example.trialbuild.WeightViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.core.cartesian.data.AxisValueOverrider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.Fill
import java.util.Locale

@Composable
fun GraphScreen(
    viewModel: WeightViewModel,
    onBack: () -> Unit
) {
    val graphData by viewModel.graphData.collectAsState()
    val modelProducer = remember { CartesianChartModelProducer() }

    val dateFormatter = remember(graphData) {
        CartesianValueFormatter { value, _, _ ->
            graphData.getOrNull(value.toInt())?.dateLabel.orEmpty()
        }
    }

    val yAxisValueFormatter = remember {
        CartesianValueFormatter { value, _, _ ->
            String.format(Locale.getDefault(), "%.1f kg", value)
        }
    }

    val axisLabelComponent = rememberTextComponent(
        color = MaterialTheme.colorScheme.onSurface,
        margins = Dimensions.of(4.dp)
    )

    LaunchedEffect(graphData) {
        if (graphData.isNotEmpty()) {
            val rawWeights = graphData.map { it.rawWeight }
            val rollingAvgs = graphData.map { it.rollingAvg ?: it.rawWeight }

            modelProducer.runTransaction {
                lineSeries {
                    series(rawWeights)
                    series(rollingAvgs)
                }
            }
        }
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        val (backBtn, title, chart, emptyText, legendCard) = createRefs()

        Button(
            onClick = onBack,
            modifier = Modifier.constrainAs(backBtn) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            }
        ) {
            Text("← Back to Inputs")
        }

        Text(
            text = "Weight & 7-Day Rolling Average",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.constrainAs(title) {
                top.linkTo(backBtn.bottom, margin = 16.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )

        if (graphData.isEmpty()) {
            Text(
                text = "No data available yet. Please add logs on the main screen.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.constrainAs(emptyText) {
                    top.linkTo(title.bottom, margin = 24.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
            )
        } else {
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberCustomLineLayer(graphData),
                    startAxis = rememberStartAxis(
                        label = axisLabelComponent,
                        valueFormatter = yAxisValueFormatter
                    ),
                    bottomAxis = rememberBottomAxis(
                        label = axisLabelComponent,
                        valueFormatter = dateFormatter
                    )
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .height(300.dp)
                    .constrainAs(chart) {
                        top.linkTo(title.bottom, margin = 16.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
            )

            ChartLegendCard(
                modifier = Modifier.constrainAs(legendCard) {
                    top.linkTo(chart.bottom, margin = 16.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
            )
        }
    }
}

@Composable
fun rememberCustomLineLayer(graphData: List<GraphDataPoint>): LineCartesianLayer {
    val rawWeightLine = rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(Color(0xFF2196F3).toArgb()))
    )
    val rollingAvgLine = rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(Color(0xFFFF9800).toArgb()))
    )

    val minY = remember(graphData) {
        val min = graphData.minOfOrNull { it.rawWeight } ?: 0f
        (min - 2f).coerceAtLeast(0f).toDouble()
    }

    return rememberLineCartesianLayer(
        lineProvider = LineCartesianLayer.LineProvider.series(
            rawWeightLine,
            rollingAvgLine
        ),
        axisValueOverrider = AxisValueOverrider.fixed(minY = minY)
    )
}

@Composable
fun ChartLegendCard(modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = Color(0xFF2196F3), label = "Daily Weight")
            LegendItem(color = Color(0xFFFF9800), label = "7-Day Avg")
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}