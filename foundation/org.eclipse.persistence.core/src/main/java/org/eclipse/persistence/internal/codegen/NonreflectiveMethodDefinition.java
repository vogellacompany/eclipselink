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
//     Oracle - initial API and implementation from Oracle TopLink
package org.eclipse.persistence.internal.codegen;

import java.util.*;

/**
 * INTERNAL:
 * <p><b>Purpose</b>: Model a method for code generation purposes,
 * using java.lang.String for the parameter types.
 *
 * @since TopLink 5.0
 * @author Paul Fullbright
 */
public class NonreflectiveMethodDefinition extends MethodDefinition {
    protected Vector<String> argumentTypeNames;

    public NonreflectiveMethodDefinition() {
        super();
        this.argumentTypeNames = new Vector<>(5);
    }

    public void addArgument(String argumentType, String argumentName) {
        getArgumentNames().addElement(argumentName);
        getArgumentTypes().addElement(argumentType);
    }

    private void adjustArgumentTypeNames(Map<String, Set<String>> typeNameMap) {
        for (Iterator<String> i = new Vector<>(getArgumentTypeNames()).iterator(); i.hasNext();) {
            String argumentTypeName = i.next();
            String adjustedArgumentTypeName = adjustTypeName(argumentTypeName, typeNameMap);

            if (!argumentTypeName.equals(adjustedArgumentTypeName)) {
                replaceArgumentTypeName(argumentTypeName, adjustedArgumentTypeName);
            }
        }
    }

    @Override
    protected void adjustTypeNames(Map<String, Set<String>> typeNameMap) {
        super.adjustTypeNames(typeNameMap);
        adjustArgumentTypeNames(typeNameMap);
    }

    @Override
    protected boolean argumentsEqual(MethodDefinition methodDefinition) {
        Object[] args1 = this.getArgumentTypes().toArray();
        Object[] args2 = methodDefinition.getArgumentTypes().toArray();
        if (args1.length == args2.length) {
            for (int i = 0; i < args1.length; i++) {
                if (((args1[i] == null) && (args1[i] != args2[i])) || (!args1[i].equals(args2[i]))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected Vector<String> getArgumentTypeNames() {
        return getArgumentTypes();
    }

    @Override
    public Vector<String> getArgumentTypes() {
        return this.argumentTypeNames;
    }

    protected void replaceArgumentTypeName(String oldArgumentTypeName, String newArgumentTypeName) {
        int index = getArgumentTypeNames().indexOf(oldArgumentTypeName);
        getArgumentTypeNames().remove(oldArgumentTypeName);
        getArgumentTypeNames().insertElementAt(newArgumentTypeName, index);
    }

    @Override
    protected void writeArguments(CodeGenerator generator) {
        boolean isFirst = true;
        for (int index = 0; index < getArgumentTypes().size(); ++index) {
            String argument = getArgumentTypes().elementAt(index);
            if (isFirst) {
                isFirst = false;
            } else {
                generator.write(", ");
            }
            generator.write(argument);
            generator.write(" ");
            generator.write(getArgumentNames().elementAt(index));
        }
    }
}
