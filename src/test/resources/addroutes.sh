#!/bin/bash

proxy='http://127.0.0.1:9000/routez'
curl -s $proxy -d '{"action":"add", "vhost": "lol.localdomain", "host":"127.0.0.1", "port": 8081}'
#curl -s $proxy -d '{"action":"ADD", "vhost": "lol.localdomain", "host":"127.0.0.1", "port": 8082}'
#curl -s $proxy -d '{"action":"Add", "vhost": "lol.localdomain", "host":"127.0.0.1", "port": 8083}'
#curl -s $proxy -d '{"action":"aDD", "vhost": "lol.localdomain", "host":"127.0.0.1", "port": 8084}'
#curl -s $proxy -d '{"action":"AdD", "vhost": "lol2.localdomain", "host":"127.0.0.1", "port": 8081}'
#curl -s $proxy -d '{"action":"aDd", "vhost": "lol2.localdomain", "host":"127.0.0.1", "port": 8082, "version": 28031974}'
