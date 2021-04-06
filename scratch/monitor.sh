while true
do
    echo "$(oc exec rhoas-operator-c5dfcd4b6-72jzx -- pmap -x 1  | grep total)" | tee -a logfile
    sleep 2
done
