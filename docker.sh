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

docker exec -it teq_node_EndDeviceLayer-0 /bin/bash
tc qdisc del dev eth0 root
    ip link add ifb0 type ifb &&
    ip link set dev ifb0 up &&
    tc qdisc replace dev eth0 handle ffff: ingress &&
    tc filter replace dev eth0 parent ffff: protocol ip u32 match u32 0 0 action mirred egress redirect dev ifb0 &&
    tc qdisc replace dev ifb0 root handle 2: htb default 22 &&
    tc class replace dev ifb0 parent 2: classid 2:22 htb rate 1000kbps ceil 1000kbps &&
    tc qdisc replace dev ifb0 parent 2:22 handle 20: netem delay 500ms &&
    tc class replace dev eth0 parent 1: classid 1:1 htb rate 1000kbps ceil 1000kbps &&
    tc qdisc replace dev eth0 parent 1:1 handle 10: netem delay   1000ms loss 5% &&
    tc class replace dev eth0 parent 1: classid 1:2 htb rate 100mbit ceil 100mbit &&
    tc filter replace dev eth0 protocol ip parent 1:0 prio 1 u32 match ip dst 10.0.0.1/16 flowid 1:2
ping teq_node_EndDeviceLayer-2
ping teq_node_NetworkHost
