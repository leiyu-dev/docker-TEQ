mvn compile
cp -r Computing/target/classes/* docker
mvn dependency:copy-dependencies -DoutputDirectory=../docker/lib
#cd docker
#javac example/Runnode.java
#java -cp ./lib/*:. example.Runnode