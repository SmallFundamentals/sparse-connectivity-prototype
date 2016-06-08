#!/bin/bash
adler="adler_"
py="_python"
java="_java"

for i in {0..18}
do
   diff $adler$i$py $adler$i$java
done
