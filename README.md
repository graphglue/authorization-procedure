# Graphglue Neo4j authorization plugin

## Content

Provides the `io.github.graphglue.authorizationPath` procedure to get all paths from a start node via outgoing relationships with permission property set to true.
Uses BFS to traverse the graph.

## Building

This project uses maven, to build a jar-file with the procedure in this
project, simply package the project with maven:

```sh
./mvnw clean package
```

This will produce a jar-file,`target/authorization-procedure-1.0.0.jar`,
that can be deployed in the `plugin` directory of your Neo4j instance.

## License

Apache License V2, see LICENSE
