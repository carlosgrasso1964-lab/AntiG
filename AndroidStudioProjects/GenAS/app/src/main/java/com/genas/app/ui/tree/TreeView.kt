package com.genas.app.ui.tree

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.genas.app.data.Person
import com.genas.app.viewmodel.PersonNode

private val LineColor = Color(0xFFBDC3C7)

@Composable
fun FamilyTreeView(
    rootNodes: List<PersonNode>,
    onEdit: (Person) -> Unit,
    onDelete: (Person) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
            rootNodes.forEach { rootNode ->
                TreeNodeView(
                    node = rootNode,
                    onEdit = onEdit,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
private fun TreeNodeView(
    node: PersonNode,
    onEdit: (Person) -> Unit,
    onDelete: (Person) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        CoupleRow(node, onEdit, onDelete)

        if (node.children.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(24.dp)
                    .background(LineColor)
            )

            ChildrenRow(node.children, onEdit, onDelete)
        }
    }
}

@Composable
private fun CoupleRow(
    node: PersonNode,
    onEdit: (Person) -> Unit,
    onDelete: (Person) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        PersonCard(
            person = node.person,
            onEdit = { onEdit(node.person) },
            onDelete = { onDelete(node.person) },
            isSpouse = false
        )

        node.spouses.forEach { spouse ->
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "\u2764\uFE0F",
                fontSize = 18.sp,
                color = Color(0xFFE74C3C)
            )
            Spacer(modifier = Modifier.width(6.dp))

            PersonCard(
                person = spouse,
                onEdit = { onEdit(spouse) },
                onDelete = { onDelete(spouse) },
                isSpouse = true
            )
        }
    }
}

@Composable
private fun ChildrenRow(
    children: List<PersonNode>,
    onEdit: (Person) -> Unit,
    onDelete: (Person) -> Unit
) {
    if (children.isEmpty()) return

    if (children.size == 1) {
        TreeNodeView(children[0], onEdit, onDelete)
        return
    }

    Box(
        modifier = Modifier
            .drawBehind {
                drawLine(
                    color = LineColor,
                    start = Offset.Zero,
                    end = Offset(size.width, 0f),
                    strokeWidth = 2f
                )
            }
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.wrapContentWidth()
        ) {
            children.forEach { child ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(16.dp)
                            .background(LineColor)
                    )
                    TreeNodeView(child, onEdit, onDelete)
                }
            }
        }
    }
}

@Composable
private fun PersonCard(
    person: Person,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isSpouse: Boolean
) {
    val borderColor = if (isSpouse) Color(0xFFE74C3C) else MaterialTheme.colorScheme.primary

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSpouse) Color(0xFFFFF5F5) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(120.dp)
            .clickable { }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(8.dp)
                .then(
                    Modifier.drawBehind {
                        drawRect(
                            color = borderColor,
                            size = size,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 2.dp.toPx()
                            )
                        )
                    }
                )
        ) {
            Text(
                text = person.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
            Text(
                text = person.role,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color(0xFFF39C12),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = Color(0xFFE74C3C),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
