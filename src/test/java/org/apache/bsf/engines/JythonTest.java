/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Apache BSF", "Apache", and "Apache Software Foundation"
 *    must not be used to endorse or promote products derived from
 *    this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * Sanjiva Weerawarana and others at International Business Machines
 * Corporation. For more information on the Apache Software Foundation,
 * please see <http://www.apache.org/>.
 */

package org.apache.bsf.engines;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFEngineTestTmpl;
import org.apache.bsf.BSFException;

/**
 * Test class for the jython language engine.
 * @author   Victor J. Orlikowski <vjo@us.ibm.com>
 */
public class JythonTest extends BSFEngineTestTmpl {
    private BSFEngine jythonEngine;

    public JythonTest(final String name) {
        super(name);
    }

    public void setUp() {
        super.setUp();
        
        try {
            jythonEngine = bsfManager.loadScriptingEngine("jython");
        }
        catch (final Exception e) {
            fail(failMessage("Failure attempting to load jython", e));
        }
    }

    public void testExec() {
        try {
            jythonEngine.exec("Test.py", 0, 0,
                              "print \"PASSED\",");
        }
        catch (final Exception e) {
            fail(failMessage("exec() test failed", e));
        }

        assertEquals("PASSED", getTmpOutStr());
    }

    public void testEval() {
        Integer retval = null;

        try {
            retval = new Integer((jythonEngine.eval("Test.py", 0, 0,
                                                    "1 + 1")).toString());
        }
        catch (final Exception e) {
            fail(failMessage("eval() test failed", e));
        }

        assertEquals(new Integer(2), retval);
    }

    public void testCall() {
        final Object[] args = { new Integer(1) };
        Integer retval = null;

        try {
            jythonEngine.exec("Test.py", 0, 0,
                              "def addOne(f):\n\t return f + 1\n");
            retval = 
                new Integer((jythonEngine.call(null, "addOne",
                                               args).toString()));
        }
        catch (final Exception e) {
            fail(failMessage("call() test failed", e));
        }

        assertEquals(new Integer(2), retval);
    }

    public void testIexec() {
        // iexec() differs from exec() in this engine, primarily
        // in that it only executes up to the first newline.
        try {
            jythonEngine.iexec("Test.py", 0, 0,
                               "print \"PASSED\"," + "\n" + "print \"FAILED\",");
        }
        catch (final Exception e) {
            fail(failMessage("iexec() test failed", e));
        }
        
        assertEquals("PASSED", getTmpOutStr());
    } 

    public void testBSFManagerEval() {
        Integer retval = null;

        try {
            retval = new Integer((bsfManager.eval("jython", "Test.py", 0, 0,
                                                  "1 + 1")).toString());
        }
        catch (final Exception e) {
            fail(failMessage("BSFManager eval() test failed", e));
        }

        assertEquals(new Integer(2), retval);
    }

    public void testBSFManagerAvailability() {
        Object retval = null;

        try {
            retval = jythonEngine.eval("Test.py", 0, 0,
                                       "bsf.lookupBean(\"foo\")");
        }
        catch (final Exception e) {
            fail(failMessage("Test of BSFManager availability failed", e));
        }

        assertEquals("None", retval.toString());
    }

    public void testRegisterBean() {
        final Integer foo = new Integer(1);
        Integer bar = null;

        try {
            bsfManager.registerBean("foo", foo);
            bar = new Integer((jythonEngine.eval("Test.py", 0, 0,
                                                 "bsf.lookupBean(\"foo\")"))
                              .toString());
        }
        catch (final Exception e) {
            fail(failMessage("registerBean() test failed", e));
        }

        assertEquals(foo, bar);
    }

    public void testUnregisterBean() {
        final Integer foo = new Integer(1);
        Object bar = null;

        try {
            bsfManager.registerBean("foo", foo);
            bsfManager.unregisterBean("foo");
            bar = jythonEngine.eval("Test.py", 0, 0,
                                    "bsf.lookupBean(\"foo\")");
        }
        catch (final Exception e) {
            fail(failMessage("unregisterBean() test failed", e));
        }

        assertEquals("None", bar.toString());
    }

    public void testDeclareBean() {
        final Integer foo = new Integer(1);
        Integer bar = null;

        try {
            bsfManager.declareBean("foo", foo, Integer.class);
            bar = new Integer((jythonEngine.eval("Test.py", 0, 0,
                                                 "foo + 1")).toString());
        }
        catch (final Exception e) {
            fail(failMessage("declareBean() test failed", e));
        }

        assertEquals(new Integer(2), bar);
    }

    public void testUndeclareBean() {
        final Integer foo = new Integer(1);
        Integer bar = null;

        try {
            bsfManager.declareBean("foo", foo, Integer.class);
            bsfManager.undeclareBean("foo");
            bar = new Integer((jythonEngine.eval("Test.py", 0, 0,
                                                 "foo + 1")).toString());
        }
        catch (final BSFException bsfE) {
            // Do nothing. This is the expected case.
        }
        catch (final Exception e) {
            fail(failMessage("undeclareBean() test failed", e));
        }

        assertNull(bar);
    }
}
