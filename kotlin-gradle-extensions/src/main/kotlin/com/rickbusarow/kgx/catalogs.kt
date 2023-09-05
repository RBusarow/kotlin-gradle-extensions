/*
 * Copyright (C) 2023 Rick Busarow
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rickbusarow.kgx

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import kotlin.LazyThreadSafetyMode.NONE

/** "1.6", "1.7", "1.8", etc. */
val Project.KOTLIN_API: String
  get() = libsCatalog.version("kotlinApi")

/**
 * the jdk used in packaging
 *
 * "1.6", "1.8", "11", etc.
 */
val Project.JVM_TARGET: String
  get() = libsCatalog.version("jvmTarget")

/**
 * the jdk used to build the project
 *
 * "1.6", "1.8", "11", etc.
 */
val Project.JDK: String
  get() = libsCatalog.version("jdk")

/** `6`, `8`, `11`, etc. */
val Project.JVM_TARGET_INT: Int
  get() = JVM_TARGET.substringAfterLast('.').toInt()

private val Project.catalogs: VersionCatalogsExtension
  get() = extensions.getByType(VersionCatalogsExtension::class.java)

/**
 * non-dsl version of `libs`
 *
 * ex:
 *
 * ```
 * val myCatalog = project.libsCatalog
 * ```
 */
val Project.libsCatalog: VersionCatalog
  get() = catalogs.named("libs")

/**
 * non-dsl version of `libs._____`
 *
 * ex:
 *
 * ```
 * "api"(project.libsCatalog.dependency("square-anvil-annotations"))
 * ```
 */
fun VersionCatalog.dependency(alias: String): Provider<MinimalExternalModuleDependency> {
  return findLibrary(alias)
    .orElseThrow {
      GradleException("No dependency was found in the catalog for the alias '$alias'.")
    }
}

/**
 * non-dsl version of `libs.versions._____.get()`
 *
 * ex:
 *
 * ```
 * val anvilVersion = project.libsCatalog.version("square-anvil")
 * ```
 */
fun VersionCatalog.version(alias: String): String {
  return findVersion(alias)
    .orElseThrow {
      GradleException("No version was found in the catalog for the alias '$alias'.")
    }
    .requiredVersion
}

/**
 * non-dsl version of `libs.versions._____.get().pluginId`
 *
 * ex:
 *
 * ```
 * val anvilId = project.libsCatalog.pluginId("square-anvil")
 * ```
 */
fun VersionCatalog.pluginId(alias: String): String {
  val errorMessage by lazy(NONE) {
    "No plugin ID was found in the catalog for the alias '$alias'."
  }
  return findPlugin(alias)
    .orElseThrow { GradleException(errorMessage) }
    .orNull
    ?.pluginId
    ?: throw GradleException(errorMessage)
}
