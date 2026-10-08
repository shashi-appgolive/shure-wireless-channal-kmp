package com.shure.wireless.channels

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun InventorySearchBar(query: String, onQueryChange: (String) -> Unit) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
        cursorBrush = SolidColor(Color.White),
        modifier = Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 8.dp)
            .semantics { contentDescription = "Find in inventory" },
        decorationBox = { innerTextField ->
            Row(
                Modifier.fillMaxWidth().height(40.dp).background(Color(0xFF161D20), RoundedCornerShape(12.dp))
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Canvas(Modifier.size(20.dp)) {
                    drawCircle(Color(0xFF878D93), radius = 6.dp.toPx(), center = Offset(8.dp.toPx(), 8.dp.toPx()), style = Stroke(2.dp.toPx()))
                    drawLine(Color(0xFF878D93), Offset(13.dp.toPx(), 13.dp.toPx()), Offset(19.dp.toPx(), 19.dp.toPx()), strokeWidth = 2.dp.toPx())
                }
                Box(Modifier.weight(1f).padding(start = 14.dp), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) Text("Find in inventory", color = Color(0xFF858A90), fontSize = 16.sp)
                    innerTextField()
                }
            }
        },
    )
}
