# vxProxy

## Source

Estrutura de código inicial gerada a partir do maven (Opcional. Apenas quando for criar novo verticle do zero)
> mvn archetype:generate -Dfilter=io.vertx:

## Building

> mvn clean package

## Executando

> vertx runzip target/vxproxy-<VERSION>.jar

## Executando indicando Arquivo de Configuração

> vertx runzip target/vxproxy-<VERSION>.jar -conf ./src/test/resources/vxproxy.conf

## Rotas de teste

Boa referência para conhecer a API e o formato de dados Json para adição de rotas.
> ./src/test/resources/addroutes.sh

## Debugging
Declare a variável de ambiente VERTX_OPTS:
> export VERTX_OPTS='-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000'

Ajuste a configuração do vertx (conf/logging.properties), alterando o level INFO para FINEST 
> sed -i 's/INFO/FINEST/' conf/logging.properties
