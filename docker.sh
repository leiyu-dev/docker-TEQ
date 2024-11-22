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
apt install iputils-ping
apt install iproute2
apt install kmod

tc qdisc add dev eth0 handle ffff: ingress
tc filter add dev eth0 parent ffff: protocol ip u32 match u32 0 0 police rate 10kbit burst 10kbit drop