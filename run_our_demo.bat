javac -encoding UTF-8 -d bin java_sdk/src/com/huawei/codecraft/*.java 

jar cvfm OurDemo.jar META-INF/MANIFEST.MF -C bin . 



.\PreliminaryJudge.exe -m maps\map-3.11.txt -d out\out.txt "java -jar OurDemo.jar"

