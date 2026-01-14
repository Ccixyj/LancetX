package com.knightboost.lancet.internal.util

import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.jar.JarArchiveEntry
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.junit.Test
import java.io.File

class JarTest {

    @Test
    fun testCreateValidJar() {
        // 创建有效的 JAR 文件
        val jarFile = File("test.jar")

        jarFile.outputStream().buffered().use { outputStream ->
            JarArchiveOutputStream(outputStream).use { jarOutput ->
                // 配置 JAR
                jarOutput.encoding = "UTF-8"

                // 1. 先添加 Manifest（可选）
                addManifest(jarOutput)

                // 2. 添加多个文本文件
                repeat(10) { index ->
                    val fileName = "test$index.txt"
                    val content = "这是第 $index 个测试文件的内容\n" +
                            "创建时间: ${java.time.LocalDateTime.now()}\n" +
                            "文件大小: ${10} 字符"

                    addTextFile(jarOutput, fileName, content)
                }

                // 3. 添加二进制文件（示例）
                addBinaryFile(jarOutput, "data.bin", ByteArray(100) { it.toByte() })

                println("JAR 文件创建成功: ${jarFile.absolutePath}")
            }
        }

        // 验证 JAR 文件
        verifyJarFile(jarFile)
    }

    private fun addManifest(jarOutput: JarArchiveOutputStream) {
        val manifest = """
            Manifest-Version: 1.0
            Created-By: JarTest
            Main-Class: TestMain
            Implementation-Version: 1.0.0
            Created-Date: ${java.time.LocalDateTime.now()}
            
            """.trimIndent()

        val entry = JarArchiveEntry("META-INF/MANIFEST.MF")
        jarOutput.putArchiveEntry(entry)
        jarOutput.write(manifest.toByteArray(Charsets.UTF_8))
        jarOutput.closeArchiveEntry()
    }

    private fun addTextFile(
        jarOutput: JarArchiveOutputStream,
        fileName: String,
        content: String
    ) {
        try {
            val bytes = content.toByteArray(Charsets.UTF_8)

            // 创建条目
            val entry = JarArchiveEntry(fileName)
            entry.size = bytes.size.toLong()

            // 设置时间戳
            entry.time = System.currentTimeMillis()

            // 对于文本文件，通常使用默认压缩（DEFLATED）
            // entry.method = ZipArchiveEntry.DEFLATED // 默认就是

            jarOutput.putArchiveEntry(entry)
            jarOutput.write(bytes)
            jarOutput.closeArchiveEntry()

            println("添加文件: $fileName (${bytes.size} 字节)")

        } catch (e: Exception) {
            System.err.println("添加文件 $fileName 失败: ${e.message}")
        }
    }

    private fun addBinaryFile(
        jarOutput: JarArchiveOutputStream,
        fileName: String,
        data: ByteArray
    ) {
        try {
            val entry = JarArchiveEntry(fileName)
            entry.size = data.size.toLong()
            entry.time = System.currentTimeMillis()

            jarOutput.putArchiveEntry(entry)
            jarOutput.write(data)
            jarOutput.closeArchiveEntry()

            println("添加二进制文件: $fileName (${data.size} 字节)")

        } catch (e: Exception) {
            System.err.println("添加二进制文件 $fileName 失败: ${e.message}")
        }
    }

    private fun verifyJarFile(jarFile: File) {
        if (!jarFile.exists()) {
            println("错误: JAR 文件不存在")
            return
        }

        println("\n验证 JAR 文件:")
        println("文件大小: ${jarFile.length()} 字节")

        // 尝试读取 JAR 文件内容
        try {
            JarArchiveInputStream(jarFile.inputStream().buffered()).use { jarInput ->
                var entryCount = 0
                var entry: ArchiveEntry?

                while (jarInput.nextEntry.also { entry = it } != null) {
                    entryCount++
                    val name = entry!!.name
                    val size = entry!!.size
                    val method = if (entry is ZipArchiveEntry) {
                        (entry as ZipArchiveEntry).method
                    } else "N/A"

                    println("  [$entryCount] $name - 大小: $size, 方法: $method")

                    // 读取内容验证（可选）
                    if (name.endsWith(".txt")) {
                        val content = jarInput.readAllBytes()
                        println("    内容预览: ${String(content).take(50)}...")
                    }
                }

                println("总计: $entryCount 个条目")
            }
        } catch (e: Exception) {
            println("验证失败: ${e.message}")
            e.printStackTrace()
        }
    }
}
