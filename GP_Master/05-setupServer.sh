#!/bin/bash

# sets up a machine to run experiments on

# This section requires root to install required packages, standard stuff
sudo su - 
# export http_proxy=http://aproxy.ie:8080
apt-get install ssh openjdk-6-jre openjdk-6-jdk screen rsync

adduser bck

exit

# set up dirs and keyless login, run as the local user (bck or brendan usually)
mkdir .ssh

echo "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCwVAs3cQ2jdNV/PkHubGUdJIRSsx0lVtDz2zmFtl5yvyUF1k6kBQGMUjzM8PoJih20woKDhQrEilxyWQe/LPzdCIT4mn4V0zCntXE6ESRU2cs/Ao4lVv6/JlCqjWApMO8Ipb+ChmFgBOpTEpSUzS4sdXP2D2fDPkkKSJEb6ZFQ7/j46PJgFsMsYtoEfeL1anJO6al1ibLs2Us5mMqdVrBJFS61AoHDjJ1p8j3XY/J6nB10BNiOWzbMapsrYNPE4IwykTRaVMQd8hbO0l2Jl0RTCe7ApFh7z7BF967nzeXBZ/R/QDTFqH1P4TYEhn0CoSp4zXA2scvXlTH/9qzbDco9 bck@Bettsy" >> .ssh/authorized_keys

mkdir GP
