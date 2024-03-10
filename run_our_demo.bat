javac -d bin java_SDK/com/huawei/codecraft/*.java 

jar cvfm OurDemo.jar META-INF/MANIFEST.MF -C bin . 



.\PreliminaryJudge.exe -m maps\map1.txt -d output\out.txt "java -jar OurDemo.jar"
