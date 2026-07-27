package io.github.drumber.kitsune.ui.search.categories

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.media.category.CategoryNode
import io.github.drumber.kitsune.preference.CategoryPrefWrapper
import io.github.drumber.kitsune.ui.component.compose.list.KitsuneCollapsingTopAppBar

data class CategoryRow(
    val node: CategoryNode,
    val level: Int,
    val isExpanded: Boolean,
    val isSelected: Boolean,
    val selectedChildCount: Int,
    val wrapper: CategoryPrefWrapper
)

fun buildCategoryRows(
    rootNodes: List<CategoryNode>,
    expandedIds: Set<String>,
    selected: Set<CategoryPrefWrapper>
): List<CategoryRow> {
    val selectedIds = selected.mapNotNull { it.categoryId }.toSet()
    val rows = mutableListOf<CategoryRow>()
    fun visit(nodes: List<CategoryNode>, level: Int, ancestorIds: List<String>) {
        nodes.forEach { node ->
            val category = node.category
            val id = category.id
            val expanded = id in expandedIds
            rows += CategoryRow(
                node = node,
                level = level,
                isExpanded = expanded,
                isSelected = id in selectedIds,
                selectedChildCount = selected.count { it.parentIds?.contains(id) == true },
                wrapper = CategoryPrefWrapper(id, category.title, category.slug, ancestorIds)
            )
            if (expanded && node.childCategories.isNotEmpty()) {
                visit(node.childCategories, level + 1, listOf(id) + ancestorIds)
            }
        }
    }

    visit(rootNodes, 1, emptyList())
    return rows
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    rows: List<CategoryRow>,
    isLoading: Boolean,
    hasError: Boolean,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onUnselectAll: () -> Unit,
    onToggleExpand: (CategoryRow) -> Unit,
    onToggleSelection: (CategoryRow, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            KitsuneCollapsingTopAppBar(
                title = { Text(stringResource(R.string.title_categories)) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.action_close)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onUnselectAll) {
                        Icon(
                            Icons.Filled.Deselect,
                            contentDescription = stringResource(R.string.action_unselect_all)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + 16.dp
                )
            ) {
                items(rows, key = { it.node.category.id }) { row ->
                    CategoryRowItem(
                        row = row,
                        onToggleExpand = { onToggleExpand(row) },
                        onToggleSelection = { onToggleSelection(row, it) }
                    )
                }
            }

            if (isLoading && rows.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            if (hasError && rows.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.error_resource_loading))
                    TextButton(onClick = onRetry) {
                        Text(stringResource(R.string.action_retry))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryRowItem(
    row: CategoryRow,
    onToggleExpand: () -> Unit,
    onToggleSelection: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasChildren = row.node.hasChildren()
    val arrowRotation by animateFloatAsState(
        targetValue = if (row.isExpanded) 180f else 0f,
        label = "categoryArrowRotation"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        if (row.level > 1) {
            HorizontalDivider(
                modifier = Modifier.padding(start = (16 + (row.level - 1) * 16).dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (hasChildren) onToggleExpand() else onToggleSelection(!row.isSelected)
                }
                .padding(
                    start = (16 + (row.level - 1) * 16).dp,
                    end = 8.dp,
                    top = 4.dp,
                    bottom = 4.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(arrowRotation)
                    .graphicsLayer { alpha = if (hasChildren) 1f else 0f }
            )
            Text(
                text = row.node.category.title.orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (row.selectedChildCount > 0) {
                SelectionCounter(count = row.selectedChildCount)
            }
            if (row.level > 1) {
                Checkbox(
                    checked = row.isSelected,
                    onCheckedChange = onToggleSelection
                )
            }
        }
    }
}

@Composable
private fun SelectionCounter(count: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text = if (count < 100) count.toString() else "99+",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun rememberCategoryRows(
    rootNodes: List<CategoryNode>,
    revision: Int,
    expandedIds: Set<String>,
    selected: Set<CategoryPrefWrapper>
): List<CategoryRow> = remember(rootNodes, revision, expandedIds, selected) {
    buildCategoryRows(rootNodes, expandedIds, selected)
}
