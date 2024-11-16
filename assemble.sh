mvn compile
cp -r Computing/target/classes/* docker
mvn dependency:copy-dependencies -DoutputDirectory=../docker/lib