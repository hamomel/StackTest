package com.hamom.stacktest

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.RadioGroup
import kotlinx.android.synthetic.main.activity.*

/**
 * Created by Roman_Zotov on 05-Apr-18.
 */

const val TAG_PREFIX = "Stack "
private const val INTENT_EXTRA = "intentExtra"

open class BaseActivity : AppCompatActivity() {

    private val TAG = "$TAG_PREFIX ${this::class.java.simpleName} :"

    companion object {
        var intentData = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity)
        setOnClicks()
        setCheckListeners()
        nameTv.text = this::class.java.simpleName
        logIntent(intent)
        showTasks()
    }

    private fun showTasks() {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val tasks = manager.appTasks
        val builder = StringBuilder()
        tasks.forEach {
            val className = it?.taskInfo?.baseActivity?.className?.replaceBeforeLast('.', "") ?: "null"

            if (builder.isNotEmpty()) builder.append("\n")
            builder.append("Activities in stack: ${it.taskInfo.numActivities}\n")
            builder.append("Top activity: $className\n")
            builder.append("Base activity: $className")
        }

        taskInfoTv.text = builder.toString()
    }

    private fun logIntent(intent: Intent?) {
        intentDataTv.text = "Intent data: ${intent?.getIntExtra(INTENT_EXTRA, -1)}"
        if (BuildConfig.DEBUG) Log.d(TAG, "logIntent: $intent ${intent?.flags}")
    }


    override fun onNewIntent(intent: Intent?) {
        logIntent(intent)
        super.onNewIntent(intent)
    }

    private fun setCheckListeners() {
        clearTask.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                newTask.setOnCheckedChangeListener(null)
            } else {
                newTask.setHighLiteListener(listOf(multipleTask))
            }
            newTask.isChecked = isChecked
            newTask.isEnabled = !isChecked
        }

        clearTop.setHighLiteListener(listOf(singleTop, newTask))
        multipleTask.setHighLiteListener(listOf(newTask, newDocument))
        newDocument.setHighLiteListener(listOf(multipleTask))
        newTask.setHighLiteListener(listOf(multipleTask))
    }

    private fun CheckBox.setHighLiteListener(others: List<CheckBox>) {
        setOnCheckedChangeListener {buttonView, isChecked ->
            others.forEach {
                if (isChecked){
                    it.setTextColor(getColor(R.color.colorAccent))
                } else{
                    it.setTextColor(buttonView.textColors)
                }
            }
        }
    }

    private fun setOnClicks() {
        var clazz: Class<*>?

        val listener = View.OnClickListener { v ->
            clazz = when (v.id) {
                R.id.mainBtn -> MainActivity::class.java
                R.id.defaultBtn -> DefaultActivity::class.java
                R.id.singleTopActivityBtn -> SingleTopActivity::class.java
                R.id.singleTaskActivityBtn -> SingleTaskActivity::class.java
                R.id.singleInstanceActivityBtn -> SingleInstanceActivity::class.java
                else -> null
            }

            startActivityFrom(clazz)
        }

        listOf(mainBtn,
                defaultBtn,
                singleTopActivityBtn,
                singleTaskActivityBtn,
                singleInstanceActivityBtn).forEach { it.setOnClickListener(listener) }
    }

    private fun startActivityFrom(clazz: Class<*>?) {
        val intent = Intent(this, clazz)
        intent.putExtra(INTENT_EXTRA, intentData++)
        intent.flags = getFlags()
        startActivity(intent)
    }

    private fun getFlags(): Int {
        var flags = 0
        listOf(singleTop, newTask, clearTop, clearTask, excludeFromRecents, multipleTask, newDocument).forEach {
            if (it.isChecked) {
                flags = flags or when (it.id) {
                    R.id.singleTop -> Intent.FLAG_ACTIVITY_SINGLE_TOP
                    R.id.newTask -> Intent.FLAG_ACTIVITY_NEW_TASK
                    R.id.clearTop -> Intent.FLAG_ACTIVITY_CLEAR_TOP
                    R.id.clearTask -> Intent.FLAG_ACTIVITY_CLEAR_TASK
                    R.id.excludeFromRecents -> Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    R.id.multipleTask -> Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                    R.id.newDocument -> Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                    else -> 0
                }
            }
        }
        return flags
    }
}
