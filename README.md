# sfsc-sample-services

To run a core instance you have two options:
1) Use docker-compose and the compose file "core1.yml" for the examples.

Type into your CLI: docker-compose -f core1.yml up (for shut down replace "up" with "down")

2) Assigne the Enviroment variables manually to the docker run command.

Type into your CLI: docker run -e HOST=<IP-OF-HOST> -e BACKEND_PORT=1250 -e CONTROL_PUB_PORT=1251 -e CONTROL_SUB_PORT=1252 -e DATA_PUB_PORT=1253 -e DATA_SUB_PORT=1254 -e BACKEND_HOST=<HOSTNAME-OF-THE-NETWORK> -e HAZELCAST_PORT=5701 -p 1250:1250 -p 1251:1251 -p 1252:1252 -p 1253:1253 -p 1254:1254 -p 5701:5701 nalim2/sfsc-core

Both versions require docker and for 1) docker-compose to be installed.

The Execution of the sample programms require an installed version of the google protobug compiler on your system. The "protoc" binary has to be accessable inside of your path/enviroment variables.
