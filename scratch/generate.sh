#!/bin/sh
set -e

echo "Begin counting"

for i1 in {1..100}
  do
    for i2 in {1..10}
      do
        cat scratch.cr.temp.yaml | sed  "s/{{number}}/$1-$i2-$i1/g" | oc apply -f - &
	sleep .25
    done
    echo "Batch $i1 done"
    wait
done
wait
