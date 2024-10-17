# 确保脚本发生错误时退出
$ErrorActionPreference = "Stop"

# 第一步：编译 Maven 项目
Write-Host "Compiling Maven project..."
mvn compile

# 第二步：将编译后的 class 文件复制到 docker 目录
Write-Host "Copying compiled class files to docker directory..."
Copy-Item -Recurse -Force "Computing\target\classes\*" "docker\"

# 第三步：将 Maven 依赖库复制到 docker\lib 目录
Write-Host "Copying Maven dependencies to docker/lib directory..."
mvn dependency:copy-dependencies -DoutputDirectory="../docker/lib"

# # 第四步：切换到 docker 目录
# Write-Host "Changing directory to docker..."
# Set-Location -Path "docker"
#
# # 第五步：编译 Java 文件
# Write-Host "Compiling Java source files..."
# javac example\Runnode.java
#
# # 第六步：运行 Java 程序
# Write-Host "Running Java program..."
# java --add-opens "java.base/java.lang=ALL-UNNAMED" -cp ".\lib\*;." example.Runnode
