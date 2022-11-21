package com.fileselection.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import com.fileselection.Constants
import com.fileselection.R
import com.fileselection.Utils
import com.fileselection.data.FileViewAttributes
import com.fileselection.view.DirectoryView
import com.fileselection.view.FView
import com.fileselection.view.FileView
import java.io.File
import kotlin.concurrent.thread

class openSelector : Activity(), View.OnClickListener, View.OnLongClickListener {

    private var initPath = "/storage/emulated/0"
    private var kuaijiePath: String? = "/storage/emulated/0/Android/data/com.tencent.mobileqq/Tencent/QQfile_recv/"
    private lateinit var fileScroll: ScrollView
    private lateinit var pathText: TextView
    private var fileListView: LinearLayout? = null
    private var isStop = false //文件获取线程是否结束
    private var fvas: FileViewAttributes? = null

    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                /**
                 * 初始化ScrollView
                 */
                0x001 -> {
                    fileScroll.removeAllViews()
                    fileListView?.let { fileScroll.addView(it) }
                }
                /**
                 * 将View加载到fileListView
                 */
                0x002 -> {
                    fileListView?.let {
                        it.addView(msg.obj?.let { it as View })
                    }
                }
                /**
                 * 清空fileListView
                 */
                0x003 -> {
                    fileListView!!.removeAllViews()
                }
                /**
                 * 设置用于显示路径的TextVew显示内容
                 */
                0x004 ->
                    pathText.text = msg.obj!!.toString()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.file_selector)

        val intent = intent
        intent.getStringExtra("path")?.let {
            val file = File(it)
            if (file.isDirectory) {
                initPath = it
                return@let
            }
            file.parent?.let {
                initPath = it
            }
        }

        fvas = intent.extras?.get("fvas") as FileViewAttributes?

        intent.getStringExtra("kjPath")?.let {
            kuaijiePath = it
        }

        init()
        accessRequest()
        showFile(initPath)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        var result = super.onKeyDown(keyCode, event)
        //返回上一层
        val path = pathText.text.toString()
        var file: File? = null
        //非最上层文件，可以返回
        File(path).parent?.let {
            file = File(it)
        }
        //如果已经是最上层文件夹，则不能返回
        if (file == null) return result
        //返回上一层文件夹
        showFile(file!!.path, File(path).name)
        //不退出activity
        result = false

        return result;
    }

    private fun init() {
        findViewById<ImageView>(R.id.fanhui).setOnClickListener(this)
        pathText = findViewById(R.id.pathText)
        val iv = findViewById<ImageView>(R.id.kuaijie)
        iv.setOnClickListener(this)
        iv.setOnLongClickListener(this)

        fileScroll = findViewById(R.id.FileScroll)
    }

    //申请权限
    private fun accessRequest() {
        //安卓系统版本小于6.0，不需要申请权限
        if (Build.VERSION.SDK_INT < 23) return
        val permissions = arrayOf("android.permission.READ_EXTERNAL_STORAGE")
        if (ActivityCompat.checkSelfPermission(this, permissions[0]) != -1) return
        ActivityCompat.requestPermissions(this, permissions, 0)
    }

    //权限同意 or 拒绝 回调
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions[0].equals("android.permission.READ_EXTERNAL_STORAGE") && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //同意读写储存权限，刷新文件视图
            showFile(initPath)
        }
    }

    private fun showFile(path: String) {
        showFile(path, null)
    }

    /**
     * @param path 要跳转的文件路径
     * @param filename 将ScrollView滑动到该文件名称的FIleView位置，为空则不滑动
     */
    private fun showFile(path: String, filename: String?) {
        if (isStop) {
            Toast.makeText(this, "请等待上一次文件加载完毕", Toast.LENGTH_LONG).show()
            return
        }
        isStop = true
        thread {
            try {
                val file = File(path)
                var files = file.listFiles() //获取当前目录下的所有文件

                val msg = Message()
                msg.what = 0x004
                msg.obj = path
                handler.sendMessage(msg) //设置TextView显示的文件路径

                fileListView?.let {
                    //清空fileListView内的View
                    handler.sendEmptyMessage(0x003)
                }
                if (fileListView == null) {
                    //存放文件集合的线性布局
                    fileListView = LinearLayout(this)
                    fileListView!!.orientation = LinearLayout.VERTICAL
                }
                //初始化ScrollView
                handler.sendEmptyMessage(0x001)

                kotlin.run {
                    //添加返回上一层视图
                    val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.more)
                    val fview = FView(this, bitmap, "...")
                    fview.id = 0xf00001.toInt()
                    fview.setOnClickListener(this)
                    val msg = Message()
                    msg.what = 0x002
                    msg.obj = fview
                    handler.sendMessage(msg) //将View添加到fileListView

                    if (files == null || files.size < 1) {
                        //获取不到目录中的文件。退出
                        return@thread
                    }
                }

                /**
                 * 所有文件类型的图标集合
                 */
                val map = HashMap<String, Bitmap>()
                val resources = resources
                map.put("wenjianjia", BitmapFactory.decodeResource(resources, R.mipmap.wenjianjia))
                map.put("txt", BitmapFactory.decodeResource(resources, R.mipmap.txt))
                map.put("lua", BitmapFactory.decodeResource(resources, R.mipmap.lua))
                map.put("java", BitmapFactory.decodeResource(resources, R.mipmap.java))
                map.put("cpp", BitmapFactory.decodeResource(resources, R.mipmap.cpp))
                map.put("python", BitmapFactory.decodeResource(resources, R.mipmap.python))
                map.put("xml", BitmapFactory.decodeResource(resources, R.mipmap.xml))
                map.put("dex", BitmapFactory.decodeResource(resources, R.mipmap.dex))
                map.put("apk", BitmapFactory.decodeResource(resources, R.mipmap.apk))
                map.put("mp3", BitmapFactory.decodeResource(resources, R.mipmap.mp3))
                map.put("mp4", BitmapFactory.decodeResource(resources, R.mipmap.mp4))
                map.put("yasuobao", BitmapFactory.decodeResource(resources, R.mipmap.yasuobao))
                map.put("image", BitmapFactory.decodeResource(resources, R.mipmap.image))
                map.put("file", BitmapFactory.decodeResource(resources, R.mipmap.file))
                fvas?.let {
                    //加载自定义图标
                    val len = it.length()
                    for (i in 0 until len) {
                        map.put(it.getSuffixs(i)[0], it.getIcon(i))
                    }
                }

                //滑动到view位置y坐标
                var scrollY = 0
                files = Utils.FileStoring(files)
                for (file in files) {
                    if (file == null) continue

                    var fView: FView

                    if (file.isDirectory) {
                        //文件夹
                        fView = DirectoryView(this, map["wenjianjia"]!!, file)

                        /**
                         * 保存当前view的y坐标用于将ScrollView滑动到这个位置
                         */
                        filename?.let {
                            if (file.name.equals(it)) {
                                fView.post {
                                    scrollY = fView.y.toInt()
                                }
                            }
                        }

                    } else {
                        var icon: Bitmap
                        //非文件夹，进行类型判断
                        val type = Utils.getFileType(file.name)
                        when (type) {
                            "txt", "TXT" -> icon = map["txt"]!!
                            "lua", "LUA" -> icon = map["lua"]!!
                            "apk", "APK" -> icon = map["apk"]!!
                            "mp3", "MP3" -> icon = map["mp3"]!!
                            "mp4", "MP4" -> icon = map["mp4"]!!
                            "zip", "ZIP", "rar", "RAR", "jar", "JAR" -> icon = map["yasuobao"]!!
                            "java", "JAVA" -> icon = map["java"]!!
                            "cpp", "CPP" -> icon = map["cpp"]!!
                            "py", "PY" -> icon = map["python"]!!
                            "xml", "XML" -> icon = map["xml"]!!
                            "dex", "DEX" -> icon = map["dex"]!!
                            "png", "PNG", "jpg", "JPEG", "bmp", "BMP", "jpeg", "JPG" -> {
                                //解码这个图片文件
                                val bitmap = BitmapFactory.decodeFile(file.path)
                                if (bitmap != null) {
                                    //解码成功，使用这个图片文件作为图标
                                    icon = bitmap
                                } else {
                                    //解码失败，使用自己的图标
                                    icon = map["image"]!!
                                }
                            }

                            else -> {
                                fvas?.let {
                                    //判断是否为自定义类型
                                    val len = it.length()
                                    for (i in 0 until len) {
                                        val suffixs = it.getSuffixs(i)
                                        for (str in suffixs) {
                                            if (type.equals(str)) {
                                                //当前文件后缀为自定义类型，使用自定义图标
                                                icon = map[suffixs[0]]!!
                                            }
                                        }
                                    }
                                }
                                icon = map["file"]!! //未知类型文件
                            }
                        }

                        fView = FileView(this, icon, file)
                    }

                    fView.setOnClickListener(this)
                    val msg = Message()
                    msg.what = 0x002
                    msg.obj = fView
                    handler.sendMessage(msg) //将View添加到fileListView

                }
                /**
                 * 滑动到该文件视图
                 */
                fileScroll.post {
                    fileListView?.post {
                        fileScroll.scrollTo(0, scrollY)
                    }
                }
                map.clear()
            } finally {
                isStop = false
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.fanhui -> {
                val intent = Intent()
                intent.putExtra("path", null as String?)
                setResult(Constants.FILE_PATH_RESULT, intent)
                finish()
                return
            }
            R.id.kuaijie -> {
                kuaijiePath?.let {
                    showFile(it)
                }
                return
            }

            else -> {
                val path = pathText.text.toString()
                val fView = v as FView
                val filename = fView.filename
                if (fView.id == 0xf00001) {
                    //返回上一层
                    var file: File? = null
                    //非最上层文件，可以返回
                    File(path).parent?.let {
                        file = File(it)
                    }
                    //如果已经是最上层文件夹，则不能返回
                    if (file == null) return
                    //返回上一层文件夹
                    showFile(file!!.path, File(path).name)
                    return
                }
                val file = File(path + "/" + filename)
                if (file.isDirectory) {
                    //点击了文件夹
                    showFile(file.path)
                    return
                }
                //点击了文件
                val intent = Intent()
                intent.putExtra("path", file.path)
                setResult(Constants.FILE_PATH_RESULT, intent)
                finish()
            }
        }

    }

    override fun onLongClick(v: View): Boolean {
        when (v.id) {
            R.id.kuaijie -> {
                return true
            }
        }
        return false
    }

}