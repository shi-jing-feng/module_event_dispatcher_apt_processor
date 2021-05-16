/** 生成的 Java 类名 */
@file:JvmName("Constant")
package com.shijingfeng.module_event_dispatcher.apt_processor.constant

/**
 * Function: 静态常量
 * Date: 2020/12/10 14:15
 * Description:
 * Author: ShiJingFeng
 */
/** NonNull注解(Android模块才会有) 全限定名称 */
internal const val NON_NULL_QUALIFIED_NAME = "androidx.annotation.NonNull"

/**
 * 模块内的 build.gradle 中设置的 模块名
 * android {
 *     defaultConfig {
 *         javaCompileOptions {
 *             annotationProcessorOptions {
 *                 arguments = [MODULE_NAME: project.getName()]
 *             }
 *         }
 *     }
 * };
 */
internal const val KEY_MODULE_NAME = "MODULE_NAME"
