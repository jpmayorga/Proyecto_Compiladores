/*
 * @(#)SequentialProcFunc.java                2.1 2003/10/07
 *
 * Copyright (C) 1999, 2003 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 */

package Triangle.AbstractSyntaxTrees;

import Triangle.SyntacticAnalyzer.SourcePosition;

public class SequentialProcFunc extends ProcFunc {

  public SequentialProcFunc (ProcFunc pf1AST, ProcFunc pf2AST,
                       SourcePosition thePosition) {
    super (thePosition);
    PF1 = pf1AST;
    PF2 = pf2AST;
  }

  public Object visit(Visitor v, Object o) {
    return v.visitSequentialProcFunc(this, o);
  }

  public ProcFunc PF1, PF2;
}
