# czdb-search 使用指南

czdb-search 是一个用于在数据库中搜索数据的类。它支持三种类型的搜索算法：内存搜索（MEMORY）、二进制搜索（BINARY）和B树搜索（BTREE）。数据库类型（IPv4或IPv6）和查询类型（MEMORY、BINARY、BTREE）在运行时确定。

## 支持 IPv4 和 IPv6

czdb-search 支持 IPv4 和 IPv6 地址的查询。在创建 DbSearcher 实例时，你需要提供相应的数据库文件和密钥。

数据库文件和密钥可以从 [www.cz88.net](http://www.cz88.net) 获取。

## 如何使用

首先，你需要创建一个 DbSearcher 的实例。在创建实例时，你需要提供数据库文件的路径、查询类型和用于解密数据库文件头块的密钥。

```java
DbSearcher searcher = new DbSearcher("数据库文件路径", QueryType.MEMORY, "密钥");
```

然后，你可以使用 `search` 方法来根据提供的 IP 地址在数据库中搜索数据。

```java
String region = searcher.search("IP地址");
```

如果搜索成功，`search` 方法将返回找到的数据块的区域。如果搜索失败，它将返回 null。

## 查询类型

DbSearcher 支持三种查询类型：MEMORY、BINARY 和 BTREE。

- MEMORY：此模式是线程安全的，将数据存储在内存中。
- BINARY：此模式使用二进制搜索算法进行查询。它不是线程安全的。不同的线程可以使用不同的查询对象。
- BTREE：此模式使用 B-tree 数据结构进行查询。它不是线程安全的。不同的线程可以使用不同的查询对象。

你可以在创建 DbSearcher 实例时选择查询类型。

```java
DbSearcher searcher = new DbSearcher("数据库文件路径", QueryType.BINARY, "密钥");
```

## 线程安全

请注意，只有 MEMORY 查询模式是线程安全的。如果你在高并发环境下使用 BINARY 或 BTREE 查询模式，可能会导致打开的文件过多的错误。在这种情况下，你可以增加内核中允许打开的最大文件数（fs.file-max），或者使用 MEMORY 查询模式。当然更合理的一个方式是为线程池中的每一个线程只创建一个DbSearcher实例。

## 关闭数据库

在完成所有查询后，你应该关闭数据库。

```java
searcher.close();
```

这将释放所有使用的资源，并关闭对数据库文件的访问。