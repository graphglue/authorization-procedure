FROM neo4j:5.23-community

ENV NEO4J_dbms_memory_transaction_total_max 0

COPY target/authorization-procedure-1.0.0.jar /var/lib/neo4j/plugins/authorization-procedure-1.0.0.jar
