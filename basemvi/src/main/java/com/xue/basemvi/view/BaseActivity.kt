package com.xue.basemvi.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.*
import androidx.viewbinding.ViewBinding
import com.xue.basemvi.R
import com.xue.basemvi.viewmodel.BaseViewModel
import kotlinx.coroutines.launch
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.ParameterizedType

/**
 * Created by chaofei-xue
 * on 2024/10/10
 *
 * Description:
 * The base class of Activity implements the binding of View Binding and Base View Model,
 * error status handling, etc.
 *
 * Activity的基类，实现了ViewBinding与BaseViewModel的绑定，错误状态处理等。
 */
abstract class BaseActivity<V : ViewBinding, VM : BaseViewModel> : AppCompatActivity() {

    private var logTag = javaClass.simpleName

    /**
     * Error view id
     * 错误视图id
     */
    private val errorLayoutId by lazy { getErrorLayout() }

    /**
     * Empty view id
     * 空视图id
     */
    private val emptyLayoutId by lazy { getEmptyLayout() }

    /**
     * Root view
     * 根布局
     */
    protected lateinit var rootView: V

    /**
     * ViewModel instance
     * viewModel实例
     */
    protected lateinit var viewModel: VM

    /**
     * Context
     * 上下文
     */
    protected lateinit var context: Context

    /**
     * Last show view
     * 最后展示的视图
     */
    private var lastShowView: View? = null

    /**
     * Main view
     * 主视图
     */
    private var mainView: View? = null
    private var childViewIndex = 0

    /****************************
     **** Lifecycle methods ****
     ******** 生命周期方法 ********
     **************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        bindView()
        viewModel = createViewModel()
        initData()
        registerEvent()
    }

    /***************************
     ***** Private methods *****
     ********  私有方法  ********
     **************************/
    /**
     * Bind viewBinding
     * 绑定 视图
     */
    private fun bindView() {
        var javaCls: Class<*> = javaClass
        val viewBindingCls: Class<*>
        while (true) {
            if (javaCls.genericSuperclass is ParameterizedType) {
                val vbClass = (javaCls.genericSuperclass as ParameterizedType)
                    .actualTypeArguments.filterIsInstance<Class<*>>()
                val viewBinding =
                    vbClass.find { clazz -> ViewBinding::class.java.isAssignableFrom(clazz) }
                if (viewBinding != null) {
                    viewBindingCls = viewBinding
                    break
                }
            }
            javaCls = javaCls.superclass
        }

        try {
            val method = viewBindingCls.getDeclaredMethod("inflate", LayoutInflater::class.java)
            rootView = method.invoke(viewBindingCls, layoutInflater) as V
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        setContentView(rootView.root)
        mainView = findViewById(android.R.id.content)
        childViewIndex = mainView?.let { getParentView(it)?.indexOfChild(it) } ?: 0
        showMainView()
    }

    /**
     * Bind viewModel
     * 绑定 viewModel
     */
    private fun <VM : ViewModel> ComponentActivity.createViewModel(): VM {
        var javaCls: Class<*> = javaClass
        while (true) {
            if (javaCls.genericSuperclass is ParameterizedType) {
                val vbClass = (javaCls.genericSuperclass as ParameterizedType)
                    .actualTypeArguments.filterIsInstance<Class<*>>()
                val viewModel =
                    vbClass.find { clazz -> BaseViewModel::class.java.isAssignableFrom(clazz) }
                if (viewModel != null) {
                    return ViewModelProvider(this).get(viewModel as Class<VM>)
                }
            }
            javaCls = javaCls.superclass
        }
    }

    /**
     * Get parent view
     * 获取父View
     */
    private fun getParentView(childView: View): ViewGroup? {
        return if (childView.parent != null) childView.parent as ViewGroup else null
    }

    /**
     * If the current child View has a parent View, unbind it
     * 如果当前子View有父View，对其进行解绑
     */
    private fun checkChildView(childView: View) {
        if (childView.parent != null) removeView(childView)
    }

    /**
     * Show main view
     * 显示主视图
     */
    private fun showMainView() {
        if (lastShowView != null) {
            removeView(lastShowView)
            lastShowView = null
        }
        mainView?.visibility = View.VISIBLE
    }

    /**
     * Register viewModel status
     * 注册viewModel状态
     */
    private fun registerEvent() {

        /**
         * Normal situation
         * 正常情况
         */
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.normalFlow.collect {
                    showMainView()
                }
            }
        }

        /**
         * Abnormal situation
         * 异常情况
         */
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorFlow.collect {
                    if (it.message.isNotEmpty()) {
                        showErrorView(it.message)
                    }
                }
            }
        }

        /**
         * Loading status
         * 加载状态
         */
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadingFlow.collect {
                    if (it) showLoading() else dismissLoading()
                }
            }
        }
    }

    /**
     * Delete the specified child View from the parent View
     * 从父View删除指定子View
     */
    private fun removeView(childView: View?) {
        if (childView != null) {
            val viewParent = childView.parent
            if (viewParent != null && viewParent is ViewGroup) {
                viewParent.removeView(childView)
            }
        }
    }

    private fun showCustomView(incomeView: View) {
        if (incomeView == lastShowView) return
        if (lastShowView != null && lastShowView != mainView) {
            removeView(lastShowView)
            lastShowView = null
        }
        if (incomeView != mainView) {
            mainView?.visibility = View.GONE
            val parentView = mainView?.let { getParentView(it) }
            if (parentView != null && mainView != null) {
                checkChildView(incomeView)
                var clParams: ConstraintLayout.LayoutParams? = null
                var isClLayout = false
                if (mainView?.layoutParams is ConstraintLayout.LayoutParams) {
                    isClLayout = true
                    clParams = ConstraintLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    clParams.bottomToBottom = ConstraintSet.PARENT_ID
                    clParams.leftToLeft = ConstraintSet.PARENT_ID
                    clParams.rightToRight = ConstraintSet.PARENT_ID
                    clParams.topToTop = ConstraintSet.PARENT_ID
                }
                parentView.addView(
                    incomeView, childViewIndex,
                    if (isClLayout) clParams else mainView?.layoutParams
                )
            }
        } else {
            mainView?.visibility = View.VISIBLE
        }
        lastShowView = incomeView
    }

    /**
     * Display error view page (such as: network error, etc.)
     * 显示错误视图页（如：网络错误等）
     */
    private fun showErrorView(msg: String) {
        val errorView = LayoutInflater.from(this).inflate(errorLayoutId, null) ?: return
        showCustomView(errorView)
    }

    /**
     * Empty data default page
     * 空数据 缺省页
     */
    private fun showEmptyView() {
        if (emptyLayoutId == -1) {
            return
        }
        val emptyView = LayoutInflater.from(this).inflate(emptyLayoutId, null) ?: return
        showCustomView(emptyView)
    }

    /****************************
     ***** Protected methods *****
     ********  被保护方法  ********
     **************************/

    /**
     * Exit activity
     * 退出activity
     */
    protected fun finishActivity(view: View?) {
        view?.run {
            setOnClickListener {
                finish()
            }
        }
    }

    /**
     * info log
     * info 日志
     */
    protected fun logInfo(text: String) {
        Log.i(logTag, text)
    }

    /**
     * error log
     * error 日志
     */
    protected fun logError(text: String) {
        Log.e(logTag, text)
    }

    /************************************
     * Methods allowed to be overridden *
     **********  允许重写的方法  **********
     ***********************************/

    open fun dismissLoading() {

    }

    open fun showLoading() {

    }

    open fun getErrorLayout(): Int {
        return R.layout.layout_base_error
    }

    open fun getEmptyLayout(): Int {
        return R.layout.layout_base_empty
    }


    /****************************
     ***** Abstract method *****
     ********  抽象方法  ********
     **************************/

    /**
     * Subclass initialization entry
     * 子类初始化入口
     */
    abstract fun initData()
}