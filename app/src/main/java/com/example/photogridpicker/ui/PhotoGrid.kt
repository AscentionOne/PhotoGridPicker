package com.example.photogridpicker.ui

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.photogridpicker.model.Photo
import com.example.photogridpicker.ui.theme.photoGridDragHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun PhotoGrid(
    modifier: Modifier = Modifier,
    photos: List<Photo> = List(100) { Photo(it) },
) {
    val selectedIds = rememberSaveable {
        mutableStateOf(emptySet<Int>())
    }

    val inSelectionMode by remember {
        derivedStateOf { selectedIds.value.isNotEmpty() }
    }

    val state = rememberLazyGridState()

    val autoScrollSpeed = remember {
        mutableStateOf(0f)
    }

    LaunchedEffect(autoScrollSpeed.value) {
        if (autoScrollSpeed.value != 0f) {
            while (isActive) {
                state.scrollBy(autoScrollSpeed.value)
                delay(10)
            }
        }
    }



    LazyVerticalGrid(
        state = state,
        columns = GridCells.Adaptive(minSize = 128.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier.photoGridDragHandler(
            lazyGridState = state,
            haptics = LocalHapticFeedback.current,
            selectedIds = selectedIds,
            autoScrollSpeed = autoScrollSpeed,
            autoScrollThreshold = with(LocalDensity.current) { 40.dp.toPx() },
        ),
    ) {
        items(photos, key = { it.id }) { photo ->
            val selected by
            remember {
                // use derivedStateOf here for performance optimization
                // since we do not want recomposition (state change) more than our UI update
                // only the selected image should update the UI and recompose
                derivedStateOf { selectedIds.value.contains(photo.id) }
            } // check if image is selected

//            val selected = selectedIds.value.contains(photo.id) // check if image is selected
            ImageItem(
                photo = photo,
                selected = selected,
                inSelectionMode = inSelectionMode,
                modifier = Modifier
                    .semantics {
                        if (!inSelectionMode) {
                            onLongClick("Select") {
                                selectedIds.value += photo.id
                                true
                            }
                        }
                    }
                    .then(if (inSelectionMode) {
                        Modifier
                            .toggleable(
                                value = selected,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null, // do not show a ripple
                                onValueChange = {
                                    if (it) {
                                        selectedIds.value += photo.id
                                    } else {
                                        selectedIds.value -= photo.id
                                    }
                                }
                            )
                    } else Modifier)
            )
        }
    }

}


@Preview
@Composable
fun PhotoGridPreview() {
    PhotoGrid()
}
