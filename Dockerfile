FROM neo4j:5.18-community

COPY target/authorization-procedure-1.0.0.jar /var/lib/neo4j/plugins/authorization-procedure-1.0.0.jar