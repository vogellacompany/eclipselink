/*
 * Copyright (c) 1998, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     tware - initial implementation as part of JPA 2.0 RI
package org.eclipse.persistence.internal.jpa.parsing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.queries.ObjectLevelReadQuery;
import org.eclipse.persistence.queries.ReportQuery;

/**
 * INTERNAL
 * <p><b>Purpose</b>: Represent an COALESCE in EJBQL
 * <p><b>Responsibilities</b>:<ul>
 * <li> Generate the correct expression for an COALESCE in EJBQL
 * </ul>
 *    @author tware
 *    @since EclipseLink 1.2
 */
public class CoalesceNode extends Node implements AliasableNode {

    private List<Node> clauses = null;

    public CoalesceNode(){
        super();
    }

    /**
     * INTERNAL
     * Apply this node to the passed query
     */
    @Override
    public void applyToQuery(ObjectLevelReadQuery theQuery, GenerationContext generationContext) {
        if (theQuery instanceof ReportQuery) {
            ReportQuery reportQuery = (ReportQuery)theQuery;
            Expression expression = generateExpression(generationContext);
            reportQuery.addItem("Coalesce", expression);
        }
    }

    /**
     * INTERNAL
     * Generate the a new EclipseLink Coalesce expression for this node.
     */
    @Override
    public Expression generateExpression(GenerationContext context) {
        List<Expression> expressions = new ArrayList<>();
        Iterator<Node> i = clauses.iterator();
        while (i.hasNext()){
            expressions.add(i.next().generateExpression(context));
        }

        return context.getBaseExpression().coalesce(expressions);
    }

    @Override
    public void validate(ParseTreeContext context) {
        TypeHelper typeHelper = context.getTypeHelper();
        Iterator<Node> i = clauses.iterator();
        Object type = null;
        while (i.hasNext()){
            Node node = i.next();
            node.validate(context);
            if (type == null){
                type = node.getType();
            } else if (!type.equals(node.getType())){
                type = typeHelper.getObjectType();
            }
        }
        setType(clauses.get(0).getType());
    }

    public List<Node> getClauses() {
        return clauses;
    }

    public void setClauses(List<Node> clauses) {
        this.clauses = clauses;
    }

    @Override
    public boolean isAliasableNode(){
        return true;
    }

    @Override
    public String toString(int indent) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("COALESCE");
        buffer.append("(");

        Iterator<Node> i = clauses.iterator();
        while (i.hasNext()) {
            Node n = i.next();
            buffer.append(n.toString(indent));
            buffer.append("\r\n");
        }
        toStringIndent(indent, buffer);
        buffer.append(")");
        return buffer.toString();
    }
}
