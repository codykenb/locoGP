#!/bin/bash

# sets up a machine to run experiments on

# This section requires root to install required packages, standard stuff
sudo su - 
export http_proxy=http://proxy.yourplace.com
apt-get install ssh openjdk-6-jre openjdk-6-jdk screen rsync

adduser bck

exit

# set up dirs and keyless login, run as the local user (bck or brendan usually)
mkdir .ssh

echo "ssh-rsa yousshekey user@mastercomputer" >> .ssh/authorized_keys

mkdir GP
