#!/bin/bash
# From a blank Ubuntu18.04 to a running public api on Neo4j
# Boise State University World Museum GraphDB JSON API
# Add OpenJDK-8-JRE, Neo4j-Enterprise-3.5.8, Nginx
# 2019-08-04 Debian(8+) and Ubuntu (18.04+)
# todo: wget config files and index (instead of echo from here)
#
# Linode stackscript UI provided variables
# If you're not running this as a Linode stackscript: provide these as environment variables
# ... and make sure the hostname and dnsdomainname are properly set
#
#<UDF name="fqdn" label="The new Linode's Fully Qualified Domain Name" example="bsu.downstreamlabs.com">
# FQDN=
#
#<UDF name="neoplugin" label="Neo4j API Plugin jar (see: https://downstream.github.io/bsuapi/releases)" example="bsuapi-0.0.1.jar">
# NEOPLUGIN=
#
#<UDF name="apipath" label="Path for accessing the api." example="/bsuapi">
# APIPATH=
#
#<UDF name="neopassword" label="Neo4j initial password" example="neo4j">
# NEOPASSWORD=

# from  https://www.linode.com/stackscripts/view/401712
# source <ssinclude StackScriptID="401712">
function apt_setup_update {
  # Force IPv4 and noninteractive update
  echo 'Acquire::ForceIPv4 "true";' > /etc/apt/apt.conf.d/99force-ipv4
  export DEBIAN_FRONTEND=noninteractive
  apt-get update -y
}

function set_hostname {
  IP=$(hostname -I | awk '{print$1}')
  HOSTNAME=$(dnsdomainname -A)
  hostnamectl set-hostname "$HOSTNAME"
  echo "$IP $HOSTNAME"  >> /etc/hosts
}

function msg(){
  printf "\n---\n        %s\n---\n" "$1"
}

exec 1> >(tee -a "/var/log/stackscript.log") 2>&1
ln -s /var/log/stackscript.log /root/stackscript.log

msg "  - - - - - STARTING STACKSCRIPT - - - - - "

set_hostname
apt_setup_update

msg "hostname $HOSTNAME"
msg "fqdn $FQDN"
msg "neoplugin $NEOPLUGIN"
msg "apipath $APIPATH"

# INITIALIZE
apt-get install -y wget git vim
msg "installed wget git vim"

echo "neo4j-enterprise neo4j/question select I ACCEPT" | sudo debconf-set-selections
echo "neo4j-enterprise neo4j/license note" | sudo debconf-set-selections
wget -O - https://debian.neo4j.org/neotechnology.gpg.key | sudo apt-key add -
echo 'deb https://debian.neo4j.org/repo stable/' | sudo tee /etc/apt/sources.list.d/neo4j.list

apt-get update -y
apt-get install -y openjdk-8-jre
apt-get install -y neo4j-enterprise=1:3.5.8
apt-get install -y nginx

msg "neo4j and nginx installed"

# NEO4J PLUGINS
wget -O /var/lib/neo4j/plugins/$NEOPLUGIN https://downstream.github.io/bsuapi/releases/$NEOPLUGIN
chown neo4j:adm /var/lib/neo4j/plugins/$NEOPLUGIN
# todo: apoc

cp /lib/systemd/system/neo4j.service /root/neo4j.service.orig
sed -E 's|^(Environment=".*)$|# \1|g' /root/neo4j.service.orig > /lib/systemd/system/neo4j.service.tmp # NEO4j SERVICE DEF - comment out the bad Env line
sed -E 's|^ExecStart=.*$|ExecStart=/usr/bin/neo4j console|g' /lib/systemd/system/neo4j.service.tmp > /lib/systemd/system/neo4j.service # NEO4j SERVICE DEF - change exec from /usr/share
rm /lib/systemd/system/neo4j.service.tmp
systemctl daemon-reload

# NEO4J CONF
mv /etc/neo4j/neo4j.conf /etc/neo4j/neo4j.conf.default
echo "# MODIFIED
dbms.unmanaged_extension_classes=bsuapi.resource=$APIPATH
dbms.active_database=boise.db
dbms.memory.heap.max_size=2G
dbms.connectors.default_listen_address=127.0.0.1
dbms.connectors.default_advertised_address=$FQDN

# APOC
dbms.security.procedures.unrestricted=apoc.*

# ORIGINAL
" > /etc/neo4j/neo4j.conf
sed -e '/^\s*#.*$/d' -e '/^\s*$/d' /etc/neo4j/neo4j.conf.default >> /etc/neo4j/neo4j.conf # remove empty line and comments
chown neo4j:adm /etc/neo4j/*
bin/neo4j-admin set-initial-password "$NEOPASSWORD"

msg "neo4j configured"

# NGINX CONF
echo "server {

    listen 80;

    root /var/www/public;

    server_name $FQDN;

    client_max_body_size 500m;

    add_header Strict-Transport-Security \"max-age=31557600; includeSubDomains\";
    add_header X-Content-Type-Options \"nosniff\" always;
    add_header X-Frame-Options \"SAMEORIGIN\" always;
    add_header X-Xss-Protection \"1\";

    sendfile off;

    error_log /var/log/error.log;
    access_log /var/log/access.log;

    location /bsuapi/releases {
        proxy_set_header Host downstream.github.io;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_pass https://downstream.github.io/bsuapi/releases;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    }

    location /bsuapi/assets {
        proxy_set_header Host downstream.github.io;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_pass https://downstream.github.io/bsuapi/assets;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    }

    location $APIPATH {
        proxy_redirect off;
        proxy_connect_timeout 30s;
        proxy_set_header Accept-Encoding \"\";
        proxy_set_header Host \$host;
        proxy_pass http://127.0.0.1:7474$APIPATH;
    }

    location / {
        proxy_set_header Host downstream.github.io;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_pass https://downstream.github.io/bsuapi/;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    }
}
" > /etc/nginx/conf.d/bsuapi.conf

msg "nginx configured"

msg "   - - - -   "
msg "stackscript complete"

systemctl restart neo4j
systemctl restart nginx

msg "services restarted"
PATH="http://$FQDN"+"$APIPATH"
msg "API available at: $PATH"
