/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.sdc.toscaparser.api;

import org.onap.sdc.toscaparser.api.elements.RelationshipType;

import java.util.ArrayList;
import java.util.LinkedHashMap;

//import java.util.Iterator;

public class ToscaGraph {
    // Graph of Tosca Node Templates

    private ArrayList<NodeTemplate> nodeTemplates;
    private LinkedHashMap<String, NodeTemplate> vertices;

    public ToscaGraph(ArrayList<NodeTemplate> inodeTemplates) {
        nodeTemplates = inodeTemplates;
        vertices = new LinkedHashMap<String, NodeTemplate>();
        create();
    }

    private void createVertex(NodeTemplate node) {
        if (vertices.get(node.getName()) == null) {
            vertices.put(node.getName(), node);
        }
    }

    private void createEdge(NodeTemplate node1,
                            NodeTemplate node2,
                            RelationshipType relation) {
        if (vertices.get(node1.getName()) == null) {
            createVertex(node1);
            vertices.get(node1.name)._addNext(node2, relation);
        }
    }

    public NodeTemplate vertex(String name) {
        if (vertices.get(name) != null) {
            return vertices.get(name);
        }
        return null;
    }

//  public Iterator getIter() {
//		return vertices.values().iterator();
//	}

    private void create() {
        for (NodeTemplate node : nodeTemplates) {
            LinkedHashMap<RelationshipType, NodeTemplate> relation = node.getRelationships();
            if (relation != null) {
                for (RelationshipType rel : relation.keySet()) {
                    NodeTemplate nodeTpls = relation.get(rel);
                    for (NodeTemplate tpl : nodeTemplates) {
                        if (tpl.getName().equals(nodeTpls.getName())) {
                            createEdge(node, tpl, rel);
                        }
                    }
                }
            }
            createVertex(node);
        }
    }

    @Override
    public String toString() {
        return "ToscaGraph{"
                + "nodeTemplates=" + nodeTemplates
                + ", vertices=" + vertices
                + '}';
    }
}

/*python

class ToscaGraph(object):
    '''Graph of Tosca Node Templates.'''
    def __init__(self, nodetemplates):
        self.nodetemplates = nodetemplates
        self.vertices = {}
        self._create()

    def _create_vertex(self, node):
        if node not in self.vertices:
            self.vertices[node.name] = node

    def _create_edge(self, node1, node2, relationship):
        if node1 not in self.vertices:
            self._create_vertex(node1)
        self.vertices[node1.name]._add_next(node2,
                                            relationship)

    def vertex(self, node):
        if node in self.vertices:
            return self.vertices[node]

    def __iter__(self):
        return iter(self.vertices.values())

    def _create(self):
        for node in self.nodetemplates:
            relation = node.relationships
            if relation:
                for rel, nodetpls in relation.items():
                    for tpl in self.nodetemplates:
                        if tpl.name == nodetpls.name:
                            self._create_edge(node, tpl, rel)
            self._create_vertex(node)
*/
