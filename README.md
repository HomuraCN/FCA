# Formal Concept Analysis (FCA) Toolkit

这是一个使用 Java 实现的形式概念分析（FCA）工具集。它提供了一系列命令行工具来处理数据、生成概念格以及执行相关的分析任务。

## 功能特性

- **数据处理**:
    - 从不同格式（如 CSV）读取数据。
    - 将原始数据转换为二元上下文（Binarization）。
    - 对二元上下文进行净化和去重。
- **概念格构建**:
    - 从二元上下文生成概念格。
    - 实现了 `In-Close3` 等概念生成算法。
- **上下文约简**:
    - 提供上下文约简功能。
- **分析与工具**:
    - 计算上下文密度。
    - 支持多种数据输入和输出格式。

## 环境要求

- [Java Development Kit (JDK) 22](https://www.oracle.com/java/technologies/downloads/#jdk22) 或更高版本。
- [Apache Maven](https://maven.apache.org/) 3.6.0 或更高版本。

## 安装与构建

1.  克隆项目仓库：
    ```bash
    git clone <您的仓库地址>
    cd FCA
    ```

2.  使用 Maven 构建项目。这会编译代码、运行测试并打包成一个 JAR 文件：
    ```bash
    mvn clean install
    ```
    构建成功后，您会在 `target/` 目录下找到 `FCA-1.0-SNAPSHOT.jar` 文件。

## 使用方法

该项目包含多个可执行的工具类，您可以使用 `java` 命令来运行它们。通用命令格式如下：

```bash
java -cp target/FCA-1.0-SNAPSHOT.jar [完整的类名] [参数]
```

以下是一些主要的工具类及其功能：

-   `fca.utils.dataProcess.DataToBinaryContext`
    -   功能：将数据文件转换为二元上下文。
-   `fca.utils.dataProcess.BinaryContextToLattice`
    -   功能：从一个二元上下文文件生成概念格。
-   `fca.utils.dataProcess.BinaryContextToReduction`
    -   功能：对二元上下文进行约简。
-   `fca.utils.dataProcess.ReductionToPermutations`
    -   功能：处理约简结果。
-   `fca.utils.readFile.GetDensity`
    -   功能：计算给定上下文文件的密度。

**示例:**
要运行密度计算工具，您可以使用类似以下的命令 (具体参数需根据代码确定):
```bash
java -cp target/FCA-1.0-SNAPSHOT.jar fca.utils.readFile.GetDensity path/to/your/context.txt
```

## 项目结构

-   `src/main/java/fca`: 包含 FCA 核心算法、分析和工具类。
-   `src/main/java/data`: 包含用于数据存储、处理和分析的各种数据结构。
-   `src/test/java/fca`: 包含项目的单元测试。