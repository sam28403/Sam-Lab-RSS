package cc.samlab.rss.ui.page.home.feeds

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cc.samlab.rss.domain.model.general.Filter
import cc.samlab.rss.ui.component.base.Banner

@Composable
fun FeedsBanner(
    modifier: Modifier = Modifier,
    filter: Filter,
    desc: String? = null,
    onClick: () -> Unit = {},
) {
    Banner(
        modifier = modifier,
        title = filter.toName(),
        desc = desc,
        icon = filter.iconOutline,
        action = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
            )
        },
        onClick = onClick
    )
}