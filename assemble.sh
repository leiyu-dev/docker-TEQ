# this script is automatically executed by the java program to assemble the backend project
# it will copy the compiled classes and dependencies to the docker folder
# don't run this script manually
# mvn compile
cp -r Computing/target/classes/* docker
cp -r Computing/target/test-classes/* docker
mkdir -p ../docker/lib
mvn dependency:copy-dependencies -DoutputDirectory=../docker/lib