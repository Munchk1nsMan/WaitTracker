package com.example.trialbuild.ui // Replace with your actual package name

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.trialbuild.WeightViewModel
import com.example.trialbuild.data.WeightEntry // Replace with your package name
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InputScreen(viewModel: WeightViewModel, onNavigateToGraph: () -> Unit) {
    val logs by viewModel.logs.collectAsState()
    var inputWeight by remember { mutableStateOf("") }
    var editingEntry by remember { mutableStateOf<WeightEntry?>(null) }
    var editWeightText by remember { mutableStateOf("") }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        val (title, graphBtn, inputField, saveBtn, pastLogsTitle, logsList) = createRefs()

        Text(
            text = "Weight Tracker",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.constrainAs(title) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            }
        )

        Button(
            onClick = onNavigateToGraph,
            modifier = Modifier.constrainAs(graphBtn) {
                top.linkTo(parent.top)
                end.linkTo(parent.end)
                bottom.linkTo(title.bottom)
            }
        ) {
            Text("View Graph")
        }

        OutlinedTextField(
            value = inputWeight,
            onValueChange = { inputWeight = it },
            label = { Text("Enter Weight (kg)") },
            modifier = Modifier.constrainAs(inputField) {
                top.linkTo(title.bottom, margin = 16.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )

        Button(
            onClick = {
                val weight = inputWeight.toFloatOrNull()
                if (weight != null) {
                    viewModel.addWeight(weight)
                    inputWeight = ""
                }
            },
            modifier = Modifier.constrainAs(saveBtn) {
                top.linkTo(inputField.bottom, margin = 8.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        ) {
            Text("Save Log")
        }

        Text(
            text = "Past Logs",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.constrainAs(pastLogsTitle) {
                top.linkTo(saveBtn.bottom, margin = 24.dp)
                start.linkTo(parent.start)
            }
        )

        LazyColumn(
            modifier = Modifier.constrainAs(logsList) {
                top.linkTo(pastLogsTitle.bottom, margin = 8.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
            }
        ) {
            items(logs) { entry ->
                val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    .format(Date(entry.timestamp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${entry.weightKg} kg", style = MaterialTheme.typography.titleMedium)
                            Text(dateStr, style = MaterialTheme.typography.bodySmall)
                        }
                        Row {
                            TextButton(onClick = {
                                editingEntry = entry
                                editWeightText = entry.weightKg.toString()
                            }) {
                                Text("Edit")
                            }
                            IconButton(onClick = { viewModel.deleteWeight(entry) }) {
                                Text("✕")
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Dialog
    editingEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { editingEntry = null },
            title = { Text("Edit Entry") },
            text = {
                OutlinedTextField(
                    value = editWeightText,
                    onValueChange = { editWeightText = it },
                    label = { Text("Weight (kg)") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    val newW = editWeightText.toFloatOrNull()
                    if (newW != null) {
                        viewModel.updateWeight(entry, newW)
                        editingEntry = null
                    }
                }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingEntry = null }) { Text("Cancel") }
            }
        )
    }
}