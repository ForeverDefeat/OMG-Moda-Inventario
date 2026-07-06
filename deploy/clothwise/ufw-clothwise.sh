#!/usr/bin/env sh
set -eu

ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw deny 8081/tcp
ufw deny 3306/tcp
ufw enable
ufw status verbose
