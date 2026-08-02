package io.github.graphglue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

/**
 * Tests {@link AuthorizationProcedure} against an embedded Neo4j instance with
 * the procedure registered, i.e. against the same procedure API a real
 * deployment provides.
 */
class AuthorizationProcedureTest {

    /**
     * The embedded Neo4j instance the procedure is registered on.
     */
    private static Neo4j neo4j;

    /**
     * The driver used to talk to {@link #neo4j}.
     */
    private static Driver driver;

    /**
     * Starts the embedded Neo4j instance and builds the test graph.
     */
    @BeforeAll
    static void startNeo4j() {
        neo4j = Neo4jBuilders.newInProcessBuilder()
                .withDisabledServer()
                .withProcedure(AuthorizationProcedure.class)
                .build();
        driver = GraphDatabase.driver(neo4j.boltURI(), AuthTokens.none());

        // a -[read]-> b -[read]-> c
        //  \-[write]-> d
        // plus a back edge c -[read]-> a to check relationship-path uniqueness
        try (Session session = driver.session()) {
            session.run("""
                    CREATE (a:Node {name: 'a'})
                    CREATE (b:Node {name: 'b'})
                    CREATE (c:Node {name: 'c'})
                    CREATE (d:Node {name: 'd'})
                    CREATE (a)-[:REL {read: true}]->(b)
                    CREATE (b)-[:REL {read: true}]->(c)
                    CREATE (a)-[:REL {write: true}]->(d)
                    CREATE (c)-[:REL {read: true}]->(a)
                    """).consume();
        }
    }

    /**
     * Stops the embedded Neo4j instance.
     */
    @AfterAll
    static void stopNeo4j() {
        if (driver != null) {
            driver.close();
        }
        if (neo4j != null) {
            neo4j.close();
        }
    }

    /**
     * Runs the procedure for the given permission, starting at the node with the
     * given name, and returns the names of the reachable nodes.
     *
     * @param startName  the name of the start node
     * @param permission the permission to traverse
     * @return the names of all reached nodes, in traversal order
     */
    private static List<String> authorizationPath(final String startName, final String permission) {
        try (Session session = driver.session()) {
            return session.run("""
                    MATCH (start:Node {name: $startName})
                    CALL io.github.graphglue.authorizationPath(start, $permission) YIELD node, path
                    RETURN node.name AS name, length(path) AS length
                    """, Map.of("startName", startName, "permission", permission))
                    .list(record -> record.get("name").asString());
        }
    }

    /**
     * The traversal only follows outgoing relationships carrying the requested
     * permission. Uniqueness is per relationship path, so {@code a} is reported
     * a second time for the path {@code a->b->c->a}.
     */
    @Test
    void followsOnlyRelationshipsWithThePermission() {
        assertThat(authorizationPath("a", "read")).containsExactlyInAnyOrder("a", "b", "c", "a");
        assertThat(authorizationPath("a", "write")).containsExactlyInAnyOrder("a", "d");
    }

    /**
     * A permission no relationship carries only yields the start node.
     */
    @Test
    void unknownPermissionYieldsOnlyTheStartNode() {
        assertThat(authorizationPath("a", "admin")).containsExactly("a");
    }

    /**
     * The traversal is directed, so it does not walk relationships backwards:
     * {@code d} has only an incoming {@code write} relationship and therefore
     * reaches nothing, while {@code b} follows the cycle forwards.
     */
    @Test
    void doesNotTraverseIncomingRelationships() {
        assertThat(authorizationPath("d", "write")).containsExactly("d");
        assertThat(authorizationPath("b", "read")).containsExactlyInAnyOrder("b", "c", "a", "b");
    }

    /**
     * {@link PermissionFieldPathExpander} is only usable in one direction.
     */
    @Test
    void expanderCannotBeReversed() {
        assertThatThrownBy(() -> new PermissionFieldPathExpander("read").reverse())
                .isInstanceOf(RuntimeException.class);
    }
}
