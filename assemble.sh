# mvn compile
cp -r Computing/target/classes/* docker
cp -r Computing/target/test-classes/* docker
mvn dependency:copy-dependencies -DoutputDirectory=../docker/lib