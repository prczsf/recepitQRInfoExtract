
# 功能说明
本程序用来读取exam目录下的所有的数电PDF发票文件，基于数电的PDF发票上的QR二维码，获取到发票的二维码中的数字信息，解析二维码，并全都导出到一个Excel中



# 使用方法
 mvn clean package
在Target目录下找到生成的Jar文件


### 运行JAR文件

您可以使用以下命令运行生成的JAR文件：

```
java -jar XXXXXX
```



# 访问地址

http://localhost:8088/api/extract

等待一会，即可看到本地目录下多了一个output.xlsx文件