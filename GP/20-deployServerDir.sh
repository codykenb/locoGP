
#echo bork!
#exit

deployFile=$1
server=$2
newDir=$3
numRuns=$4

ssh $server "mkdir -p $newDir"
scp $deployFile $server:$newDir/
ssh $server "screen -d -m bash -i -c 'cd $newDir; .././88-repeatRun.sh $numRuns'"
#ssh $server "screen -d -m bash -i -c 'cd $newDir; java -server -XX:PermSize=1024M -XX:MaxPermSize=1048M -XX:+CMSClassUnloadingEnabled -XX:+CMSPermGenSweepingEnabled -jar $deployFile > totalCrap.txt 2> errorCrap.txt'"
