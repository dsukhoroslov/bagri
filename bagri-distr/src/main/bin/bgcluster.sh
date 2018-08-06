if [ "$1" == "" ]; then
  cmd=status
else 
  cmd=$1
fi

idx=0
profile=fourth
mem=16g
for ip in 10 13 14 15 16 17 18 19 20
do
  for id in {1..6};
  do
    ssh zika@172.20.3.$ip " \
    cd /mnt/denis_stand/bagri-2.0.0-SNAPSHOT/bin; \
    ./bgcache.sh $cmd $profile $idx $mem; \
    sleep 3; \
    idx=$((idx++)); \
    exit"
  done
  sleep 5
done

