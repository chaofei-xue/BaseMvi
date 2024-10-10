package com.xue.basemvi.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.xue.basemvi.viewmodel.BaseViewModel
import kotlinx.coroutines.launch
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.ParameterizedType

/**
 * Created by chaofei-xue
 * on 2024/10/10
 *
 * Description:
 * The base class of Fragment implements the binding of View Binding and Base View Model,
 * error status handling, etc.
 *
 * Fragment的基类，实现了ViewBinding与BaseViewModel的绑定，错误状态处理等。
 */
abstract class BaseFragment<V : ViewBinding, VM : BaseViewModel> : Fragment() {

    private var viewTag = javaClass.simpleName

    protected lateinit var rootView: V
    protected lateinit var viewModel: VM

    /****************************
     **** Lifecycle methods ****
     ******** 生命周期方法 ********
     **************************/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindView()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return rootView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel()
        registerErrorEvent()
        initData(view)
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
        val type = javaClass.genericSuperclass as ParameterizedType
        val cls = type.actualTypeArguments[0] as Class<*>
        try {
            val method = cls.getDeclaredMethod("inflate", LayoutInflater::class.java)
            rootView = method.invoke(cls, layoutInflater) as V
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }

    /**
     * Bind viewModel
     * 绑定 viewModel
     */
    private fun <VM : ViewModel> createViewModel(): VM {
        var javaCls: Class<*> = javaClass
        while (true) {
            val vbClass = (javaCls.genericSuperclass as ParameterizedType)
                .actualTypeArguments.filterIsInstance<Class<*>>()
            val viewModel =
                vbClass.find { clazz -> BaseViewModel::class.java.isAssignableFrom(clazz) }
            if (viewModel != null) {
                return ViewModelProvider(this).get(viewModel as Class<VM>)
            }
            javaCls = javaCls.superclass
        }
    }


    /**
     * Handle UI error status
     * 处理UI错误状态
     */
    private fun registerErrorEvent() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorFlow.collect {
                    showErrorView(it.message)
                }
            }
        }
    }

    /****************************
     ***** Protected methods *****
     ********  被保护方法  ********
     **************************/

    /**
     * info log
     * info 日志
     */
    protected fun logInfo(text: String) {
        Log.i(viewTag, text)
    }

    /**
     * error log
     * error 日志
     */
    protected fun logError(text: String) {
        Log.e(viewTag, text)
    }

    /****************************
     ***** Abstract method *****
     ********  抽象方法  ********
     **************************/

    /**
     * Subclass initialization entry
     * 子类初始化入口
     */
    abstract fun initData(view: View)

    /**
     * Default error view handling, needs to be overridden by subclasses.
     * 默认错误视图处理，需要子类重写。
     */
    open fun showErrorView(errMsg: String) {}
}