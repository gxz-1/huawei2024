javac -encoding UTF-8 -d bin java_sdk3/src/com/huawei/codecraft/*.java 

jar cvfm OurDemo.jar META-INF/MANIFEST.MF -C bin . 


.\PreliminaryJudge.exe -m maps\map2.txt -d out\out.txt "java -jar OurDemo.jar"

