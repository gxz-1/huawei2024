//----------------------------python版
//直接运行
 .\PreliminaryJudge.exe -m maps\map1.txt "python sdk\python\main.py"

//-----------------------------java版 
//先编译打包
javac -d bin java_SDK/com/huawei/codecraft/*.java (源代码编译为字节码，才能打包为jar包。-d xx 表示编译后文件存放在哪里)
jar cvfm MyApp.jar META-INF/MANIFEST.MF -C bin . （把字节码根据清单文件打包为可运行的jar包）

//运行
.\PreliminaryJudge.exe -m maps\map1.txt "java -jar MyApp.jar" （使用判题器运行jar包）


//运行后生成replay/*.rep文件，使用replayer/CodeCraft_2024_Replay.exe查看图形化回放

