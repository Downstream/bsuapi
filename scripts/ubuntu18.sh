#!/bin/bash
# From a blank Ubuntu18.04 to a running public api on Neo4j
# Boise State University World Museum GraphDB JSON API
# Add OpenJDK-8-JRE, Neo4j-Enterprise-3.5.8, Nginx
# todo: Configure neo4j service definition for Ubutnu18
# todo: Install busapi neo4j plugin, and adjust neo4j config
# 2019-08-04 Debian(8+) and Ubuntu (18.04+)

sudo apt-get update
sudo apt-get install -y wget git vim

wget -O - https://debian.neo4j.org/neotechnology.gpg.key | sudo apt-key add -
echo 'deb https://debian.neo4j.org/repo stable/' | sudo tee /etc/apt/sources.list.d/neo4j.list

sudo apt-get update

sudo apt-get install -y openjdk-8-jre
sudo apt-get install -y neo4j-enterprise=3.5.8
sudo apt-get install -y nginx

# NEO4J PLUGINS

wget https://debian.neo4j.org/neotechnology.gpg.key

# NEO4J CONF
sudo cp /etc/neo4j/neo4j.conf /etc/neo4j/neo4j.conf.default
sudo sed -e '/^\s*#.*$/d' -e '/^\s*$/d' /etc/neo4j/neo4j.conf # remove empty line and comments
sudo mv /etc/neo4j/neo4j.conf /etc/neo4j/neo4j.tmp
sudo echo "# MODIFIED
dbms.unmanaged_extension_classes=bsuapi.resource=/bsuapi
dbms.active_database=boise.db
dbms.memory.heap.max_size=2048m
dbms.connectors.default_listen_address=127.0.0.1
dbms.connectors.default_advertised_address=boise.local

# APOC
dbms.security.procedures.whitelist=apoc.*

# ORIGINAL
" > /etc/neo4j/neo4j.conf
sudo cat /etc/neo4j/neo4j.tmp >> /etc/neo4j/neo4j.conf
sudo rm /etc/neo4j/neo4j.tmp


# NGINX CONF


