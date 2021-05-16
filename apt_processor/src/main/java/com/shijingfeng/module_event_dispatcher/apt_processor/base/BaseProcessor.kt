package com.shijingfeng.module_event_dispatcher.apt_processor.base

import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

/**
 * 行分隔符: 系统不一样, 行分隔符也不一样
 * Dos: \r\n
 * Windows: \r\n
 * Mac: \r
 * Unix(Linux属于Unix): \n
 */
internal val LINE_SEPARATOR by lazy { System.getProperty("line.separator") }

/**
 * Function: 注解处理器 基类
 * Date: 2020/11/30 11:00
 * Description:
 * Author: ShiJingFeng
 */
internal abstract class BaseProcessor : AbstractProcessor() {

    /** 用于报告错误，警告和其他通知的消息 工具 */
    protected var mMessager: Messager? = null
    /** 用于创建新的源，类或辅助文件的文件管理器 工具 */
    protected var mFiler: Filer? = null
    /** 一些用于操作元素的实用方法的实现 工具 */
    protected var mElements: Elements? = null
    /** 一些用于对类型进行操作的实用程序方法的实现 工具 */
    protected var mTypes: Types? = null
    /** 在应用模块或库模块中的build.gradle中设置的键值对数据 */
    protected var mOptions: Map<String, String>? = null

    /**
     * 应用该注解处理器的每个模块都会调用此方法
     */
    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        processingEnv?.run {
            mMessager = messager
            mFiler = filer
            mElements = elementUtils
            mTypes = typeUtils
            mOptions = options
        }
    }

    /**
     * 执行回调
     * 应用该注解处理器的每个模块都会调用此方法
     *
     * @return true: 后续的注解执行器 就不会处理 当前注解执行器中指定的这些注解
     */
    final override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        return onProcess(annotations, roundEnv)
    }

    /**
     * 执行回调 (子类实现)
     * 应用该注解处理器的每个模块都会调用此方法
     *
     * @return true: 后续的注解执行器 就不会处理 当前注解执行器中指定的这些注解
     */
    protected abstract fun onProcess(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean

    /**
     * 设置支持的Java版本名称
     */
    override fun getSupportedSourceVersion() = SourceVersion.RELEASE_8

    /**
     * 打印信息性消息, Build Output 控制台 会输出信息性消息
     *
     * @param noteStr 信息性消息
     * @param element 信息性消息定位 Element
     * @param autoNewLine 是否自动换行  true: 自动换行
     */
    protected fun printNote(
        noteStr: String,
        element: Element? = null,
        autoNewLine: Boolean = true
    ) {
        if (autoNewLine) {
            mMessager?.printMessage(Diagnostic.Kind.NOTE, if (noteStr.endsWith(LINE_SEPARATOR)) noteStr else (noteStr + LINE_SEPARATOR), element)
        } else {
            mMessager?.printMessage(Diagnostic.Kind.NOTE, noteStr, element)
        }
    }

    /**
     * 打印警告消息, Build Output 控制台 会输出警告消息
     *
     * @param warningStr 警告信息
     * @param element 警告信息定位 Element
     * @param autoNewLine 是否自动换行  true: 自动换行
     */
    protected fun printWarning(
        warningStr: String,
        element: Element? = null,
        autoNewLine: Boolean = true
    ) {
        if (autoNewLine) {
            mMessager?.printMessage(Diagnostic.Kind.WARNING, if (warningStr.endsWith(LINE_SEPARATOR)) warningStr else (warningStr + LINE_SEPARATOR), element)
        } else {
            mMessager?.printMessage(Diagnostic.Kind.WARNING, warningStr, element)
        }
    }

    /**
     * 打印错误消息, Build Output 控制台 会输出错误消息
     *
     * @param errorStr 错误信息
     * @param element 错误信息定位 Element
     * @param autoNewLine 是否自动换行  true: 自动换行
     */
    protected fun printError(
        errorStr: String,
        element: Element? = null,
        autoNewLine: Boolean = true
    ) {
        if (autoNewLine) {
            mMessager?.printMessage(Diagnostic.Kind.ERROR, if (errorStr.endsWith(LINE_SEPARATOR)) errorStr else (errorStr + LINE_SEPARATOR), element)
        } else {
            mMessager?.printMessage(Diagnostic.Kind.ERROR, errorStr, element)
        }
    }

}