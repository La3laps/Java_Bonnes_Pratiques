#!/bin/bash

javac -d ./Client/bin/main/org/example ./Client/src/main/java/org/example/*.java
javac -d ./Server/bin/main/org/example ./Server/src/main/java/org/example/*.java

# I'm using kitty terminal
kitty -e bash -c "cd ./Server/bin/main/ && java org.example.ServerMain; exec bash" &

sleep 1

# Start first client in a new terminal
kitty -e bash -c "cd ./Client/bin/main/ && java org.example.ClientMain; exec bash" &

# Start second client in a new terminal
kitty -e bash -c "cd ./Client/bin/main/ && java org.example.ClientMain; exec bash" &