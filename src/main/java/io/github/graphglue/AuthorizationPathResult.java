package io.github.graphglue;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;

public class AuthorizationPathResult {
    public Path path;
    public Node node;

    public AuthorizationPathResult(final Path path, final Node node) {
        this.path = path;
        this.node = node;
    }
}
