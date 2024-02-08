package io.github.graphglue;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;

/**
 * A result of an authorization path.
 */
public class AuthorizationPathResult {
    /**
     * The path from the start node to node only via authorized outgoing relations.
     */
    public Path path;

    /**
     * The node at the end of the path.
     */
    public Node node;

    /**
     * Create a new instance of this authorization path result.
     *
     * @param path The path from the start node to node only via authorized outgoing relations.
     * @param node The node at the end of the path.
     */
    public AuthorizationPathResult(final Path path, final Node node) {
        this.path = path;
        this.node = node;
    }
}
