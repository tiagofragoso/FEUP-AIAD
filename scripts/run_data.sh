#!/usr/bin/env bash

i=1
j=1
k=1
l=5
while [[ "$i" -le 1 ]]; do
    j=$((1))
    while [[ "$j" -le 10 ]]; do
        k=$((1))
        while [[ "$k" -le 5 ]]; do
            l=$((5))
            while [[ "$l" -le 25 ]]; do
                java -cp ../jade/lib/jade.jar:../out/production/FEUP-AIAD DataProducer rapidData.csv $i $j $k $l
                rm ../logs/*.log
                l=$(($l+1))
                done
            k=$(($k+1))
            done
        j=$(($j+1))
    done
    i=$(($i+1))
done