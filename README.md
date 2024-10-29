# czdb-search 使用指南

czdb-search 是一个用于在纯真(CZ88)IP库中搜索数据的类。它支持三种类型的搜索算法：内存搜索（MEMORY）和B树搜索（BTREE）。数据库类型（IPv4或IPv6）和查询类型（MEMORY、BTREE）在运行时确定。

## Maven 依赖

如果你想在你的Maven项目中使用`czdb-search`，请在你的`pom.xml`中添加以下依赖：

```xml
<dependency>
    <groupId>net.cz88</groupId>
    <artifactId>czdb-search</artifactId>
    <version>${cz88db.version}</version>
</dependency>
```
请替换`${cz88db.version}`为你想要使用的版本。目前最新的版本是`1.0.2.5`。

## 支持 IPv4 和 IPv6

czdb-search 支持 IPv4 和 IPv6 地址的查询。在创建 DbSearcher 实例时，你需要提供相应的数据库文件和密钥。

数据库文件和密钥可以从 [www.cz88.net](https://cz88.net/geo-public) 获取。

## 如何使用

首先，你需要创建一个 DbSearcher 的实例。在创建实例时，你需要提供数据库文件的路径、查询类型和用于解密数据库的密钥。

```java
DbSearcher searcher = new DbSearcher("数据库文件路径", QueryType.MEMORY, "密钥");
```

然后，你可以使用 `search` 方法来根据提供的 IP 地址在数据库中搜索数据。

```java
String region = searcher.search("IP地址");
```

如果搜索成功，`search` 方法将返回找到的数据块的区域。如果搜索失败，它将返回 null。

返回的字符串格式为 "国家–省份–城市–区域 ISP"。例如，对于一个位于中国上海市虹口区的IP地址，返回的字符串可能是 "中国–上海–上海–虹口区 电信"。如果搜索失败，它将返回 null。

## 查询类型

DbSearcher 支持2种查询类型：MEMORY 和 BTREE。

- MEMORY：此模式是线程安全的，将数据存储在内存中。
- BTREE：此模式使用 B-tree 数据结构进行查询。它不是线程安全的。不同的线程可以使用不同的查询对象。

你可以在创建 DbSearcher 实例时选择查询类型。

```java
DbSearcher searcher = new DbSearcher("数据库文件路径", QueryType.BTREE, "密钥");
```

## 线程安全

请注意，只有 MEMORY 查询模式是线程安全的。如果你在高并发环境下使用 BTREE 查询模式，可能会导致打开的文件过多的错误。在这种情况下，你可以增加内核中允许打开的最大文件数（fs.file-max），或者使用 MEMORY 查询模式。当然更合理的一个方式是为线程池中的每一个线程只创建一个DbSearcher实例。

## 关闭数据库

当查询结束时，你应该关闭数据库。注意**并不是说**每次查询都需要创建DbSearcher实例查完后关闭，如果是为每个线程创建一个DbSearcher实例，那么只有在线程结束时才需要关闭数据库。

```java
searcher.close();
```

这将释放所有使用的资源，并关闭对数据库文件的访问。

## 如何进行查询测试

我们提供了一个名为 `SearcherTest` 的类，你可以通过命令行来使用它进行查询测试。以下是如何使用的说明：

首先，你需要从 GitHub 的 release 中下载 `czdb-search-1.0.jar` 文件。你可以点击[这里](https://github.com/tagphi/czdb-search-java/releases)下载。

然后，你可以运行 `SearcherTest`。它需要三个参数：

- `-d` 或 `--dbFilePath`：数据库文件的路径。
- `-t` 或 `--queryType`：查询类型。有效的类型有 `MEMORY` 和 `BTREE`。
- `-k` 或 `--key`：用于解密数据库文件的密钥。

以下是一个运行 `SearcherTest` 的示例命令：

```bash
java -cp czdb-search-1.0.2.3.jar net.cz88.czdb.SearcherTest -d your-db-file-path -t MEMORY -k your-key
```

请将 your-db-file-path 和 your-key 替换为实际的值。  在运行 SearcherTest 后，你可以输入 IP 地址进行查询，或输入 'q' 退出程序。

## 性能测试
做性能测试，需要额外提供两个参数:

- `-b` 表示进行性能测试
- `-i` 待查询的IP地址文件路径。文件格式为
```text
开始ip\t结束ip
```

程序会每行抽取一个ip查询，查询完后打印查询时间，查询次数和QPS。