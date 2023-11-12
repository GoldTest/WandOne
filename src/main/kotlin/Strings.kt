import androidx.compose.ui.unit.dp

//APP
const val APP_WINDOW_TITLE = "一号魔杖"
const val APP_PATH = "WandOne.exe"

//Window
val APP_WINDOW_WIDTH = 500.dp
val APP_WINDOW_HEIGHT = 900.dp
val PAGE_START = 16.dp
val PAGE_END = 16.dp
val PAGE_TOP = 16.dp
val SPACER_HEIGHT_8 = 8.dp
val SPACER_HEIGHT_12 = 12.dp


//文件迁移
const val DEFAULT_DOUYIN_SOURCE_FOLDER = "D:/STOP"
const val DEFAULT_DOUYIN_DEST_FOLDER = "E:/抖音归档"

//Tab
const val FILE_MIGRATE = "文件迁移"
const val MEDIA_MERGE = "媒体合并"
const val SETTINGS = "设置"

//File subs
const val FILE_MIGRATE_MANUAL = "手动迁移"
const val FILE_MIGRATE_START_SERVICE = "启动迁移服务"

//Tray


//Button(
//                    modifier = Modifier.padding(end = 8.dp).width(windowWidth).height(windowHeight),
//                    onClick = {
//                        javaClass.classLoader.getResource("icon.png")
//                    },
//                ) {
//                    val imageResId = "icon.png" // 资源文件的名称
//                    val painter = painterResource(imageResId)
//
//                    Image(
//                        painter = painter,
//                        contentDescription = "Icon",
//                        modifier = Modifier.fillMaxSize()
//                    )
//                }