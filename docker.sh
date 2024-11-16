#docker rm teq
docker run -it \
    -v $PWD:/code \
    --privileged=true \
    --name teq \
    teq:1.1 \
    bash

#docker start teq
#docker exec -it teq /bin/bash

docker run -it --privileged=true --name teqtest teq:test bash