package com.shijingfeng.module_event_dispatcher.apt_processor.processor

import com.google.auto.service.AutoService
import com.shijingfeng.module_event_dispatcher.data.annotations.ModuleEventReceiver
import com.shijingfeng.module_event_dispatcher.data.entity.ModuleEventReceiverData
import com.shijingfeng.module_event_dispatcher.data.interfaces.IModuleEventDataLoader
import com.shijingfeng.module_event_dispatcher.data.interfaces.ModuleEventListener
import com.shijingfeng.module_event_dispatcher.apt_processor.base.BaseProcessor
import com.shijingfeng.module_event_dispatcher.apt_processor.base.LINE_SEPARATOR
import com.shijingfeng.module_event_dispatcher.apt_processor.constant.KEY_MODULE_NAME
import com.shijingfeng.module_event_dispatcher.apt_processor.constant.NON_NULL_QUALIFIED_NAME
import com.shijingfeng.module_event_dispatcher.apt_processor.util.TimeUtils
import com.shijingfeng.module_event_dispatcher.data.constant.Constant
import com.squareup.javapoet.*
import java.io.IOException
import javax.annotation.processing.*
import javax.annotation.processing.Processor
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * Function: 注解执行器
 * Date: 2020/11/30 10:11
 * Description:
 * Author: ShiJingFeng
 */
@AutoService(Processor::class)
internal class ModuleEventReceiverProcessor : BaseProcessor() {

    /** 模块名 */
    private var mModuleName = ""

    /** ApplicationListener TypeMirror */
    private val mModuleEventListener by lazy {
        mElements!!.getTypeElement(ModuleEventListener::class.qualifiedName)!!.asType()
    }

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        val moduleName = mOptions?.get(KEY_MODULE_NAME)

        if (moduleName.isNullOrEmpty()) {
            printError(
                """
                   |These no module name, at 'build.gradle', like :
                   |android {
                   |    defaultConfig {
                   |        javaCompileOptions {
                   |             annotationProcessorOptions {
                   |                 arguments = [MODULE_NAME: project.getName()]
                   |             }
                   |        }
                   |    }
                   |};
                """.trimMargin()
            )
        } else {
            mModuleName = moduleName
        }
    }

    /**
     * 执行回调 (子类实现)
     *
     * @return true: 后续的注解执行器 就不会处理 当前注解执行器中指定的这些注解
     */
    override fun onProcess(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        if (annotations.isNullOrEmpty()) {
            return false
        }
        generateTargetFile(roundEnv)
        return true
    }

    /**
     * 设置支持的 注解类型全限定名称 集合
     */
    override fun getSupportedAnnotationTypes() = setOf(
        ModuleEventReceiver::class.qualifiedName
    )

    /**
     * 设置 build.gradle中配置的Map数据 Key集合
     */
    override fun getSupportedOptions() = setOf(
        KEY_MODULE_NAME
    )

    /**
     * 生成目标文件
     */
    private fun generateTargetFile(roundEnv: RoundEnvironment?) {
        roundEnv?.getElementsAnnotatedWith(ModuleEventReceiver::class.java)?.let { elementSet ->
            if (elementSet.isEmpty()) {
                return
            }
            elementSet.forEach { element ->
                if (mTypes?.isSubtype(element.asType(), mModuleEventListener) != true) {
                    // 目标类没有实现 APPLICATION_LISTENER_QUALIFIED_NAME 接口
                    printError("目标类必须实现 ${ModuleEventListener::class.qualifiedName} 接口")
                    return
                }
            }
            val classFile = TypeSpec.classBuilder(Constant.MODULE_DATA_LOADER_PREFIX + mModuleName)
                .addJavadoc(
                    """
                    Function: 用于加载当前模块中所有的 Application生命周期监听类 数据
                    Date: ${TimeUtils.millis2String(System.currentTimeMillis(), "yyyy/MM/dd HH:mm")}
                    Description:

                    @author ShiJingFeng
                    """.trimIndent()
                )
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(IModuleEventDataLoader::class.java)
                .addMethod(
                    MethodSpec.methodBuilder("load")
                        .addAnnotation(Override::class.java)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.VOID)
                        .addParameter(
                            ParameterSpec.builder(
                                ParameterizedTypeName.get(
                                    List::class.java,
                                    ModuleEventReceiverData::class.java
                                ), "dataList"
                            )
                            .addAnnotation(Class.forName(NON_NULL_QUALIFIED_NAME))
                            .build()
                        )
                        .addStatement(
                            CodeBlock.join(
                                elementSet.map { element ->
                                    val typeElement = element as TypeElement
                                    val receiver = typeElement.getAnnotation(ModuleEventReceiver::class.java)
                                    val classQualifiedName = typeElement.qualifiedName.toString()
                                    val group = receiver.group
                                    val priority = receiver.priority
                                    val flag = receiver.flag

                                    return@map CodeBlock.of(
                                        "dataList.add(new \$T(\"$mModuleName\", \"$classQualifiedName\", \"$group\", $priority, $flag))",
                                        ModuleEventReceiverData::class.java
                                    )
                                },
                                ";$LINE_SEPARATOR"
                            )
                        )
                        .build()
                )
                .build()

            val javaFile = JavaFile
                .builder(Constant.AUTO_GENERATE_FILE_PACKAGE_NAME, classFile)
                .build()

            try {
                javaFile.writeTo(mFiler)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}