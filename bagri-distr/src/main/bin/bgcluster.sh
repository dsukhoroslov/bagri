idx=0
profile=fourth
mem=96g
for ip in 10 13 14 15 16 17 18 19 20
do
  ssh zika@172.20.3.$ip " \
  cd /mnt/denis_stand/bagri-2.0.0-SNAPSHOT/bin; \
  ./bgcache.sh start $profile $idx $mem; \
  idx=$((idx++)); \
##  ./bgcache.sh start $profile $idx $mem; \
##  idx=$((idx++)); \
##  ./bgcache.sh start $profile $idx $mem; \
##  idx=$((idx++)); \
  exit"
done
