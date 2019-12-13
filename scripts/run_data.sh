#!/usr/bin/env bash

i=$MIN_MACH
j=1
k=1
l=5
while [[ "$i" -le $MAX_MACH ]]; do
    j=$((1))
    while [[ "$j" -le 10 ]]; do
        k=$((1))
        while [[ "$k" -le 5 ]]; do
            l=$((5))
            while [[ "$l" -le 25 ]]; do
                java -cp ../jade/lib/jade.jar:../out DataProducer $GENERATED_CSV $i $j $k $l
                rm ../logs/*.log
                l=$(($l+1))
                done
            k=$(($k+1))
            done
        j=$(($j+1))
    done
    i=$(($i+1))
done