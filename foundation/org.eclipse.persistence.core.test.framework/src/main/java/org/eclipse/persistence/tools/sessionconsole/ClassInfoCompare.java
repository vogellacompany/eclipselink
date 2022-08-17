/*
 * Copyright (c) 1998, 2022 Oracle and/or its affiliates. All rights reserved.
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
package org.eclipse.persistence.tools.sessionconsole;

import java.util.Comparator;

/**
 * Use to sort vectors of descriptors.
 */
public class ClassInfoCompare implements Comparator<ClassInfo> {
    @Override
    public int compare(ClassInfo arg1, ClassInfo arg2) {
        return arg1.descriptor.getJavaClass().getName().compareTo(arg2.descriptor.getJavaClass().getName());
    }
}
